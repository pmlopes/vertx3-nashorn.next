package com.jetdrone.nashorn.next;

import io.vertx.core.Vertx;
import io.vertx.core.file.FileSystem;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import jdk.nashorn.api.scripting.AbstractJSObject;
import jdk.nashorn.api.scripting.JSObject;
import jdk.nashorn.api.scripting.ScriptObjectMirror;

import javax.script.*;
import java.util.Locale;

public final class AMD {

  private final ScriptEngine engine;

  private static final Logger log = LoggerFactory.getLogger(AMD.class);

  public AMD(final Vertx vertx) throws ScriptException, NoSuchMethodException {
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
    // add async fetch
    globalBindings.put("fetchText", new AbstractJSObject() {
      final FileSystem fs = vertx.fileSystem();

      @Override
      public Object call(final Object self, final Object... arguments) {

        if (arguments != null && arguments.length > 0) {
          String resource = (String) arguments[0];

          if (arguments.length > 1) {
            JSObject callback = (JSObject) arguments[1];

            fs.readFile(resource, res -> {
              if (res.failed()) {
                if (callback != null) {
                  callback.call(self, res.cause());
                }
                return;
              }

              if (callback != null) {
                callback.call(self, null, res.result().toString());
              }
            });

            return null;
          }

          return fs.readFileBlocking(resource).toString();
        }

        return null;
      }

      @Override
      public boolean isFunction() {
        return true;
      }
    });

    engine.setBindings(globalBindings, ScriptContext.GLOBAL_SCOPE);

    // install the console object
    ((Invocable) engine).invokeFunction("load", "classpath:console.js");

    // update JSON to handle native JsonObject/JsonArray types
    ((Invocable) engine).invokeFunction("load", "classpath:JSON.js");

    // loads the shims and AMD light

    final Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
    // bind vertx instance
    bindings.put("vertx", vertx);
    // delete any old setup
    engine.eval("function (global) { if (global.define !== undefined) { delete global['define']; } }(this);");
    // install the loader
    ((Invocable) engine).invokeFunction("load", "classpath:amdlite.js");
    // unbind vertx instance
    bindings.remove("vertx");
  }

  public void config(final JsonObject config) throws ScriptException {
    // configure the loader
    engine.eval("try { define.amd.lite.config(" + config.encode() + "); } catch (e) { console.trace(e); throw e; }");
  }

  public ScriptEngine getEngine() {
    return engine;
  }

  public void main(String main) throws ScriptException {
    engine.eval("try { load('" + AMD.escapeJSString(main) + "'); } catch (e) { console.trace(e); throw e; }");
  }

  public static String escapeJSString(String str) {
    return escapeJavaStyleString(str, false, false);
  }

  public static String escapeJavaStyleString(String str, boolean escapeSingleQuote, boolean escapeForwardSlash) {
    if (str == null) {
      return null;
    }

    final StringBuilder out = new StringBuilder(str.length() * 2);

    int sz;
    sz = str.length();
    for (int i = 0; i < sz; i++) {
      char ch = str.charAt(i);

      // handle unicode
      if (ch > 0xfff) {
        out.append("\\u").append(hex(ch));
      } else if (ch > 0xff) {
        out.append("\\u0").append(hex(ch));
      } else if (ch > 0x7f) {
        out.append("\\u00").append(hex(ch));
      } else if (ch < 32) {
        switch (ch) {
          case '\b':
            out.append('\\');
            out.append('b');
            break;
          case '\n':
            out.append('\\');
            out.append('n');
            break;
          case '\t':
            out.append('\\');
            out.append('t');
            break;
          case '\f':
            out.append('\\');
            out.append('f');
            break;
          case '\r':
            out.append('\\');
            out.append('r');
            break;
          default:
            if (ch > 0xf) {
              out.append("\\u00").append(hex(ch));
            } else {
              out.append("\\u000").append(hex(ch));
            }
            break;
        }
      } else {
        switch (ch) {
          case '\'':
            if (escapeSingleQuote) {
              out.append('\\');
            }
            out.append('\'');
            break;
          case '"':
            out.append('\\');
            out.append('"');
            break;
          case '\\':
            out.append('\\');
            out.append('\\');
            break;
          case '/':
            if (escapeForwardSlash) {
              out.append('\\');
            }
            out.append('/');
            break;
          default:
            out.append(ch);
            break;
        }
      }
    }

    return out.toString();
  }

  private static String hex(char ch) {
    return Integer.toHexString(ch).toUpperCase(Locale.ENGLISH);
  }
}
