package xyz.jetdrone.vertx.factory;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import xyz.jetdrone.vertx.factory.nashorn.NashornJSObjectMessageCodec;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

@RunWith(VertxUnitRunner.class)
public class EventBusTest {

  private static ScriptEngine engine;
  private static Vertx vertx;

  @BeforeClass
  public static void beforeClass() {
    vertx = Vertx.vertx();
    engine = new ScriptEngineManager().getEngineByName("nashorn");
    engine.put("eb", vertx.eventBus());

    vertx.eventBus().registerDefaultCodec(ScriptObjectMirror.class, new NashornJSObjectMessageCodec(engine));
  }

  @Test(timeout = 10000)
  public void testNativeJSObjectOverEB(TestContext ctx) throws ScriptException {
    final Async async = ctx.async();

    vertx.eventBus().consumer("test.address.object", msg -> {
      ctx.assertNotNull(msg);
      ctx.assertNotNull(msg.body());
      Object res = msg.body();
      ctx.assertNotNull(res);
      ctx.assertTrue(res instanceof JsonObject);
      async.complete();
    });

    engine.eval("eb.send('test.address.object', {foo: 'bar'})");
    async.await();
  }

  @Test(timeout = 10000)
  public void testNativeJSArrayOverEB(TestContext ctx) throws ScriptException {
    final Async async = ctx.async();

    vertx.eventBus().consumer("test.address.array", msg -> {
      ctx.assertNotNull(msg);
      ctx.assertNotNull(msg.body());
      Object res = msg.body();
      ctx.assertNotNull(res);
      ctx.assertTrue(res instanceof JsonArray);
      async.complete();
    });

    engine.eval("eb.send('test.address.array', ['foo', 'bar'])");
    async.await();
  }
}
