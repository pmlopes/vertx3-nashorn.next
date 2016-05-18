package io.vertx.polyglot.blockchain;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.WebSocket;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket client for the blockchain.info service. It allows developers to receive Real-Time notifications about new
 * transactions and blocks.
 */
public class Blockchain {

  private final Vertx vertx;

  private HttpClient client;
  private WebSocket ws;

  private final Map<String, Handler<Map<String, Object>>> addresses = new ConcurrentHashMap<>();

  private Handler<String> status;
  private Handler<Map<String, Object>> unconfirmed;
  private Handler<Map<String, Object>> blocks;

  private Handler<Throwable> errorHandler;

  public Blockchain(Vertx vertx) {
    this.vertx = vertx;
  }

  /**
   * Receive new transactions for a specific bitcoin address
   */
  public void subscribe(String address, Handler<Map<String, Object>> handler) {
    addresses.put(address, handler);
    ws.writeFinalBinaryFrame(Buffer.buffer(new JsonObject().put("op", "addr_sub").put("addr", address).encode()));
  }

  /**
   * Receive notifications when a new block is found. Note: if the chain splits you will receive more than one
   * notification for a specific block height
   */
  public void subscribeBlocks(Handler<Map<String, Object>> handler) {
    blocks = handler;
    ws.writeFinalTextFrame(new JsonObject().put("op", "blocks_sub").encode());
  }

  /**
   * Subscribe to notifications for all new bitcoin transactions.
   */
  public void subscribeUnconfirmed(Handler<Map<String, Object>> handler) {
    unconfirmed = handler;
    ws.writeFinalTextFrame(new JsonObject().put("op", "unconfirmed_sub").encode());
  }

  /**
   * Regardless of channel subscription you may receive status messages. This should be displayed to the user.
   */
  public void subscribeStatus(Handler<String> handler) {
    status = handler;
  }

  /**
   * Register a exception handler
   *
   * @param handler the handler for runtime exceptions
   */
  public void exceptionHandler(Handler<Throwable> handler) {
    errorHandler = handler;
  }

  /**
   * Connect to the service usually running at <a href="wss://ws.blockchain.info/inv">wss://ws.blockchain.info/inv</a>
   *
   * @param url     the address of the service
   * @param handler A callback handler informing the connection is complete.
   */
  public void connect(String url, Handler<Void> handler) {

    final URI uri;

    try {
      uri = new URI(url);
      if (!"ws".equals(uri.getScheme()) && !"wss".equals(uri.getScheme())) {
        throw new URISyntaxException(url, "Invalid scheme");
      }
    } catch (URISyntaxException e) {
      if (errorHandler != null) {
        errorHandler.handle(e);
      }
      return;
    }

    final boolean ssl = "wss".equals(uri.getScheme());
    final int port = uri.getPort() == -1 ? (ssl ? 443 : 80) : uri.getPort();
    final String host = uri.getHost();
    final String resource = uri.getPath() + (uri.getQuery() != null ? "?" + uri.getQuery() : "");

    client = vertx.createHttpClient(new HttpClientOptions().setSsl(ssl));

    client.websocket(port, host, resource, ws -> {
      this.ws = ws;

      ws.frameHandler(frame -> {
        if (frame.isText() && frame.isFinal()) {
          JsonObject jsonFrame = new JsonObject(frame.textData());
          JsonObject msg = jsonFrame.getJsonObject("x");
          String op = jsonFrame.getString("op");

          if (status != null && "status".equals(op)) {
            status.handle(msg.getString("msg"));
            return;
          }

          Set<String> addresses = new HashSet<>();

          JsonArray inputs = msg.getJsonArray("inputs");
          if (inputs != null) {
            for (Object el : inputs) {
              JsonObject prev_out = ((JsonObject) el).getJsonObject("prev_out");
              if (prev_out != null) {
                String addr = prev_out.getString("addr");
                if (addr != null) {
                  addresses.add(addr);
                }
              }
            }
          }

          JsonArray outputs = msg.getJsonArray("out");
          if (outputs != null) {
            for (Object el : outputs) {
              String addr = ((JsonObject) el).getString("addr");
              if (addr != null) {
                addresses.add(addr);
              }
            }
          }

          for (String addr : addresses) {
            Handler<Map<String, Object>> h = this.addresses.get(addr);
            if (h != null) {
              h.handle(msg.getMap());
            }
          }

          if (unconfirmed != null && "utx".equals(op)) {
            unconfirmed.handle(msg.getMap());
          }

          if (blocks != null && "block".equals(op)) {
            blocks.handle(msg.getMap());
          }
        }
      });

      ws.exceptionHandler(t -> {
        if (errorHandler != null) {
          errorHandler.handle(t);
        }
      });

      handler.handle(null);
    });
  }

  /**
   * Disconnect from the current service.
   */
  public void disconnect() {
    if (ws != null) {
      ws.close();
      ws = null;
    }

    if (client != null) {
      client.close();
      client = null;
    }
  }
}
