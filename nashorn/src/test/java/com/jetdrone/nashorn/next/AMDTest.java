package com.jetdrone.nashorn.next;

import io.vertx.core.Vertx;
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
public class AMDTest {

  private static AMD amd;
  private static ScriptEngine engine;

  private static String root;

  @BeforeClass
  public static void beforeClass() throws ScriptException, NoSuchMethodException {
    amd = new AMD(Vertx.vertx());
    engine = amd.getEngine();

    URL url = AMDTest.class.getClassLoader().getResource("empty.txt");
    assertNotNull(url);
    root = new File(url.getPath()).getParent();
  }

  private void run(TestContext ctx, String base) throws ScriptException, NoSuchMethodException {
    // reset the loader caches
    amd.reload();

    // configure the loader
    amd.config(new JsonObject().put("baseUrl", new File(root, base).getAbsolutePath()));

    // share test control variables
    final Async async = ctx.async();
    engine.put("assert", ctx);
    engine.put("test", async);

    try {
      amd.main(new File(root, base + "/_test.js").getAbsolutePath());
    } catch (ScriptException e) {
      ctx.fail(e.getMessage());
    }

    // wait for assertions
    async.await();
  }

  @Test
  public void testConsole(TestContext ctx) throws ScriptException, NoSuchMethodException {
    // print some stuff
    engine.eval("console.debug('Hello', 'World', '!')");
    engine.eval("console.info('Hello', 'World', '!')");
    engine.eval("console.log('Hello', 'World', '!')");
    engine.eval("console.warn('Hello', 'World', '!')");
    engine.eval("console.error('Hello', 'World', '!')");
  }

  @Test
  public void testConsoleTrace(TestContext ctx) throws ScriptException, NoSuchMethodException {
    // install the console object
    ((Invocable) engine).invokeFunction("load", "classpath:console.js");

    engine.eval("//@ sourceURL=/index.js\ntry { throw new Error('durp!'); } catch (e) { console.trace(e); }");
  }

  @Test
  public void testConsoleCount(TestContext ctx) throws ScriptException, NoSuchMethodException {
    // install the console object
    ((Invocable) engine).invokeFunction("load", "classpath:console.js");

    engine.eval("console.count('durp'); console.count('durp'); console.count('durp'); console.count('durp')");
  }

  @Test
  public void testConsoleTime(TestContext ctx) throws ScriptException, NoSuchMethodException {
    // install the console object
    ((Invocable) engine).invokeFunction("load", "classpath:console.js");

    engine.eval("console.time('durp'); for (var i = 0; i < 1000; i++); console.timeEnd('durp')");
    engine.eval("console.timeEnd('durp');");
  }

  @Test(timeout = 10000)
  public void testBasicDefine(TestContext ctx) throws ScriptException, NoSuchMethodException {
    run(ctx, "basic_define");
  }

  @Test(timeout = 10000)
  public void testBasicNoDeps(TestContext ctx) throws ScriptException, NoSuchMethodException {
    run(ctx, "basic_no_deps");
  }

  @Test(timeout = 10000)
  public void tesBasicEmptyDeps(TestContext ctx) throws ScriptException, NoSuchMethodException {
    run(ctx, "basic_empty_deps");
  }

  @Test(timeout = 10000)
  public void testBasicRequire(TestContext ctx) throws ScriptException, NoSuchMethodException {
    run(ctx, "basic_require");
  }

  @Test(timeout = 10000)
  public void testBasicSimple(TestContext ctx) throws ScriptException, NoSuchMethodException {
    run(ctx, "basic_simple");
  }

  @Test(timeout = 10000)
  public void testBasicCircular(TestContext ctx) throws ScriptException, NoSuchMethodException {
    run(ctx, "basic_circular");
  }

  @Test(timeout = 10000)
  public void testAnonSimple(TestContext ctx) throws ScriptException, NoSuchMethodException {
    run(ctx, "anon_simple");
  }

  @Test(timeout = 10000)
  public void testAnonRelative(TestContext ctx) throws ScriptException, NoSuchMethodException {
    run(ctx, "anon_relative");
  }

  @Test(timeout = 10000)
  public void testAnonCircular(TestContext ctx) throws ScriptException, NoSuchMethodException {
    run(ctx, "anon_circular");
  }

  @Test(timeout = 10000)
  @Ignore
  public void testCjsDefine(TestContext ctx) throws ScriptException, NoSuchMethodException {
    run(ctx, "cjs_define");
  }

  @Test(timeout = 10000)
  @Ignore
  public void testCjsNamed(TestContext ctx) throws ScriptException, NoSuchMethodException {
    run(ctx, "cjs_named");
  }

  @Test(timeout = 10000)
  public void testConfigPaths(TestContext ctx) throws ScriptException, NoSuchMethodException {
    run(ctx, "config_paths");
  }

  @Test(timeout = 10000)
  public void testConfigPathsRelative(TestContext ctx) throws ScriptException, NoSuchMethodException {
    run(ctx, "config_paths_relative");
  }

  @Test(timeout = 10000)
  public void testPluginDouble(TestContext ctx) throws ScriptException, NoSuchMethodException {
    run(ctx, "plugin_double");
  }

  @Test(timeout = 10000)
  public void testPluginVertx(TestContext ctx) throws ScriptException, NoSuchMethodException {
    run(ctx, "plugin_vertx");
  }
}
