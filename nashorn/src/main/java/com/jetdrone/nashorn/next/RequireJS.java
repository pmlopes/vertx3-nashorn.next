package com.jetdrone.nashorn.next;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import javax.script.*;

public final class RequireJS extends Loader {

  public RequireJS(final Vertx vertx) throws ScriptException, NoSuchMethodException {
    super(vertx);

    // loads the shims and RequireJS light
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
  
  public void main(String main) throws ScriptException {
    engine.eval("try { load('" + escapeJSString(main) + "'); } catch (e) { console.trace(e); throw e; }");
  }
}
