package com.jetdrone.nashorn.next;

import io.vertx.core.AbstractVerticle;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptException;
import java.io.File;

public class JSVerticle extends AbstractVerticle {

  public void start() throws Exception {
    // create a new CommonJS loader
    final Loader loader = new Loader(vertx);

    final Bindings bindings = loader.getEngine().getBindings(ScriptContext.ENGINE_SCOPE);
    // expose vertx
    bindings.put("vertx", vertx);
    // expose config
    loader.config(config());
    // run
    try {
      loader.main(new File(System.getProperty("user.dir"), "server.js").getCanonicalPath());
    } catch (ScriptException e) {
      e.printStackTrace();
      vertx.close();
      System.exit(1);
    }
  }
}
