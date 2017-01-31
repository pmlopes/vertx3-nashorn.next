package xyz.jetdrone.vertx.factory.jython;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.python.core.PyDictionary;
import org.python.core.PyList;

import javax.script.*;

public class Loader {

  private static final Logger log = LoggerFactory.getLogger(Loader.class);

  protected final ScriptEngine engine;
  private final Vertx vertx;

  public Loader(final Vertx vertx) throws ScriptException, NoSuchMethodException {
    // create a engine instance
    this.vertx = vertx;
    engine = new ScriptEngineManager().getEngineByName("jython");

    // register a default codec to allow JSON messages directly from nashorn to the JVM world
    vertx.eventBus().unregisterDefaultCodec(PyDictionary.class);
    vertx.eventBus().registerDefaultCodec(PyDictionary.class, new JythonPyDictionaryMessageCodec());
    vertx.eventBus().unregisterDefaultCodec(PyList.class);
    vertx.eventBus().registerDefaultCodec(PyList.class, new JythonPyListMessageCodec());
  }

  public ScriptEngine getEngine() {
    return engine;
  }

  public void config(final JsonObject config) throws ScriptException {
    final Bindings engineBindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
    // expose the config as a global
    engineBindings.put("config", config != null ? config.getMap() : null);
  }

  public void main(String main) throws ScriptException, NoSuchMethodException {
    vertx.fileSystem().readFile(main, res -> {
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
