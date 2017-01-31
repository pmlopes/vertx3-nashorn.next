package xyz.jetdrone.vertx.factory;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import xyz.jetdrone.vertx.JSON;
import xyz.jetdrone.vertx.factory.jython.Loader;

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
    Object result = JSON.stringify(new JsonObject().put("foo", "bar"));
    assertNotNull(result);
    assertEquals("{\"foo\":\"bar\"}", result);
  }

  @Test
  public void testNativeJsonArray(TestContext ctx) throws ScriptException, NoSuchMethodException {
    Object result = JSON.stringify(new JsonArray().add("foo").add("bar"));
    assertNotNull(result);
    assertEquals("[\"foo\",\"bar\"]", result);
  }

  @Test
  public void testOriginalObject(TestContext ctx) throws ScriptException, NoSuchMethodException {
    engine.eval("from xyz.jetdrone.vertx import JSON\nres = JSON.stringify({'foo': 'bar'})");
    Object result = engine.get("res");
    assertNotNull(result);
    assertEquals("{\"foo\":\"bar\"}", result);
  }

  @Test
  public void testOriginalArray(TestContext ctx) throws ScriptException, NoSuchMethodException {
    engine.eval("from xyz.jetdrone.vertx import JSON\nres = JSON.stringify(['foo', 'bar'])");
    Object result = engine.get("res");
    assertNotNull(result);
    assertEquals("[\"foo\",\"bar\"]", result);
  }
}
