package com.jetdrone.nashorn.next;

import io.vertx.core.*;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public class NashornAMDVerticle extends AbstractVerticle {

  private final ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");

  public void start() throws Exception {
  }

  /**
   * If your verticle has simple synchronous clean-up tasks to complete then override this method and put your clean-up
   * code in there.
   * @throws Exception
   */
  public void stop() throws Exception {
  }

}
