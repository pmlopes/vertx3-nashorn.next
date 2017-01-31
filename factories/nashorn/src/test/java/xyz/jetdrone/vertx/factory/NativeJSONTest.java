package xyz.jetdrone.vertx.factory;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import xyz.jetdrone.vertx.factory.nashorn.Loader;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(VertxUnitRunner.class)
public class NativeJSONTest {

  private static ScriptEngine engine;

  @BeforeClass
  public static void beforeClass() throws ScriptException, NoSuchMethodException {
    Loader loader = new DummyLoader(Vertx.vertx());
    engine = loader.getEngine();
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
