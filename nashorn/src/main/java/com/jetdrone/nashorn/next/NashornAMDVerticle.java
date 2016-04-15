package com.jetdrone.nashorn.next;

import io.vertx.core.*;
import io.vertx.core.json.JsonObject;

import javax.script.*;
import java.io.File;

public class NashornAMDVerticle extends AbstractVerticle {

  public void start() throws Exception {
    // create a new AMD loader
    final AMD loader = new AMD(vertx);

    // configure the loader
    final JsonObject config = config();
    final String cwd = config.getString("baseUrl", new File(System.getProperty("user.dir")).getCanonicalPath());

    loader.config(
        new JsonObject()
            .put("baseUrl", cwd)
            .put("paths", config.getJsonObject("paths", new JsonObject())));

    // run main
    try {
      loader.main(new File(cwd, "main.js").getCanonicalPath());
    } catch (ScriptException e) {
      vertx.close();
      System.exit(1);
    }
  }
}
