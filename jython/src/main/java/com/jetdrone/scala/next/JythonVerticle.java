package com.jetdrone.scala.next;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import javax.script.*;

public class JythonVerticle extends AbstractVerticle {

  private final Logger log = LoggerFactory.getLogger(JythonVerticle.class);
  private final ScriptEngine engine = new ScriptEngineManager().getEngineByName("jython");

  @Override
  public void start() throws Exception {

    final Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
    // bind vertx instance
    bindings.put("vertx", vertx);

    vertx.fileSystem().readFile("main.py", res -> {
      if (res.failed()) {
        die(res.cause());
      } else {
        try {
          engine.eval(res.result().toString());
        } catch (ScriptException e) {
          die(e);
        }
      }
    });
  }

  private void die(Throwable cause) {
    log.fatal("Fatal error", cause);

    vertx.close(res -> {
      if (res.failed()) {
        log.fatal("Failed to close", res.cause());
      }
      System.exit(-1);
    });
  }
}
