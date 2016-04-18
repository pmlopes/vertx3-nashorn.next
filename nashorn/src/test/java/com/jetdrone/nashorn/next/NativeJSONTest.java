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
public class NativeJSONTest {

  private static AMD amd;
  private static ScriptEngine engine;

  @BeforeClass
  public static void beforeClass() throws ScriptException, NoSuchMethodException {
    amd = new AMD(Vertx.vertx());
    engine = amd.getEngine();
  }

  @Test
  public void testNativeJsonObject(TestContext ctx) throws ScriptException, NoSuchMethodException {
    Object JSON = engine.get("JSON");

    Object result = ((Invocable) engine).invokeMethod(JSON, "stringify", new JsonObject().put("foo", "bar"));
    assertNotNull(result);
    assertEquals("{\"foo\":\"bar\"}", result);
  }

  @Test
  public void testNativeJsonArray(TestContext ctx) throws ScriptException, NoSuchMethodException {
    Object JSON = engine.get("JSON");

    Object result = ((Invocable) engine).invokeMethod(JSON, "stringify", new JsonArray().add("foo").add("bar"));
    assertNotNull(result);
    assertEquals("[\"foo\",\"bar\"]", result);
  }

  @Test
  public void testOriginalObject(TestContext ctx) throws ScriptException, NoSuchMethodException {
    Object result = engine.eval("JSON.stringify({foo: 'bar'})");
    assertNotNull(result);
    assertEquals("{\"foo\":\"bar\"}", result);
  }

  @Test
  public void testOriginalArray(TestContext ctx) throws ScriptException, NoSuchMethodException {
    Object result = engine.eval("JSON.stringify(['foo', 'bar'])");
    assertNotNull(result);
    assertEquals("[\"foo\",\"bar\"]", result);
  }
}
