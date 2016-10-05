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
public class RequireJSTest {

  private RequireJS amd;
  private ScriptEngine engine;

  private static String root;
  private static Vertx vertx;

  @BeforeClass
  public static void beforeClass() {
    URL url = RequireJSTest.class.getClassLoader().getResource("empty.txt");
    assertNotNull(url);
    root = new File(url.getPath()).getParent();
    vertx = Vertx.vertx();
  }

  @AfterClass
  public static void afterClass() {
    vertx.close();
  }

  @Before
  public void setup() throws ScriptException, NoSuchMethodException {
    amd = new RequireJS(vertx);
    engine = amd.getEngine();
  }

  private void run(TestContext ctx, String base) throws ScriptException, NoSuchMethodException {
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
  public void testCjsDefine(TestContext ctx) throws ScriptException, NoSuchMethodException {
    run(ctx, "cjs_define");
  }

  @Test(timeout = 10000)
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

  @Test(timeout = 30000)
  public void testPluginES6(TestContext ctx) throws ScriptException, NoSuchMethodException {
    run(ctx, "plugin_es6");
  }
}
