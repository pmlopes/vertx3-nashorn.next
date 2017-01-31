package xyz.jetdrone.vertx.factory.nashorn;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import jdk.nashorn.api.scripting.AbstractJSObject;
import jdk.nashorn.api.scripting.JSObject;
import jdk.nashorn.api.scripting.ScriptObjectMirror;

import javax.script.*;

public class Loader {

  private static final Logger log = LoggerFactory.getLogger(Loader.class);

  protected final ScriptEngine engine;

  public Loader(final Vertx vertx) throws ScriptException, NoSuchMethodException {
    // create a engine instance
    engine = new ScriptEngineManager().getEngineByName("nashorn");

    // register a default codec to allow JSON messages directly from nashorn to the JVM world
    vertx.eventBus().unregisterDefaultCodec(ScriptObjectMirror.class);
    vertx.eventBus().registerDefaultCodec(ScriptObjectMirror.class, new NashornJSObjectMessageCodec(engine));

    final Bindings engineBindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
    // remove the exit and quit functions
    engineBindings.remove("exit");
    engineBindings.remove("quit");

    final Bindings globalBindings = new SimpleBindings();

    final JSObject exit = new AbstractJSObject() {
      @Override
      public Object call(Object self, Object... arguments) {

        final int exitCode;

        if (arguments != null && arguments.length > 0) {
          Object retValue = arguments[0];
          if (retValue instanceof Number) {
            exitCode = ((Number) retValue).intValue();
          } else if (retValue instanceof String) {
            int parsed;
            try {
              parsed = Integer.parseInt((String) retValue);
            } catch (NumberFormatException e) {
              parsed = -1;
            }
            exitCode = parsed;
          } else {
            exitCode = -1;
          }
        } else {
          exitCode = 0;
        }

        vertx.close(res -> {
          if (res.failed()) {
            log.fatal("Failed to close", res.cause());
            System.exit(-1);
          } else {
            System.exit(exitCode);
          }
        });
        return null;
      }

      @Override
      public boolean isFunction() {
        return true;
      }
    };

    // re-add exit and quit but a proper one
    globalBindings.put("exit", exit);
    globalBindings.put("quit", exit);

    engine.setBindings(globalBindings, ScriptContext.GLOBAL_SCOPE);

    // load polyfills
    ((Invocable) engine).invokeFunction("load", "classpath:polyfill.js");
    // install the console object
    ((Invocable) engine).invokeFunction("load", "classpath:console.js");
    // update JSON to handle native JsonObject/JsonArray types
    ((Invocable) engine).invokeFunction("load", "classpath:JSON.js");
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
    // install the console object
    ((Invocable) engine).invokeFunction("load", main);
  }
}
