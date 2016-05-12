package com.jetdrone.nashorn.next;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import javax.script.*;

import java.io.*;
import java.net.URL;

import static org.junit.Assert.*;

@RunWith(VertxUnitRunner.class)
public class FetchTextTest {

  private static ScriptEngine engine;

  @BeforeClass
  public static void beforeClass() throws ScriptException, NoSuchMethodException {
    AMD amd = new AMD(Vertx.vertx());
    engine = amd.getEngine();
  }

  @Test(timeout = 10000)
  public void testFetchText(TestContext ctx) throws ScriptException, NoSuchMethodException {
    final Async async = ctx.async();

    // share test control variables
    engine.put("assert", ctx);
    engine.put("test", async);

    try {
      engine.eval("fetchText('shared/_reporter.js', function (err, text) { if (err) { return test.fail(err); } test.complete(); });");
    } catch (ScriptException e) {
      ctx.fail(e.getMessage());
    }

    // wait for assertions
    async.await();
  }
}
