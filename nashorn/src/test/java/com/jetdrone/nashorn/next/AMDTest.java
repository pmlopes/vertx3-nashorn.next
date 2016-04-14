package com.jetdrone.nashorn.next;

import io.vertx.core.Vertx;
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

  private static final ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
  private static final Bindings bindings = new SimpleBindings();

  private static Vertx vertx;
  private static String root;

  static {
    engine.setBindings(bindings, ScriptContext.GLOBAL_SCOPE);
  }

  @BeforeClass
  public static void beforeClass() {
    vertx = Vertx.vertx();
    URL url = AMDTest.class.getClassLoader().getResource("empty.txt");
    assertNotNull(url);
    root = new File(url.getPath()).getParent();
  }

  @AfterClass
  public static void afterClass() {
    vertx.close();
  }

  private void run(TestContext ctx, String base) throws ScriptException, NoSuchMethodException {

    // install the loader
    bindings.put("define", new AMD(vertx, engine, new File(root, base).getAbsolutePath()));
    // share test control variables
    final Async async = ctx.async();
    engine.put("assert", ctx);
    engine.put("test", async);

    // run
    ((Invocable) engine).invokeFunction("load", new File(root, base +"/_test.js").getAbsolutePath());

    // wait for assertions
    async.await();
  }

  @Test //(timeout = 1000)
  public void tesBasicDefine(TestContext ctx) throws ScriptException, NoSuchMethodException {
    run(ctx, "basic_define");
  }

  @Test //(timeout = 1000)
  public void tesBasicNoDeps(TestContext ctx) throws ScriptException, NoSuchMethodException {
    run(ctx, "basic_no_deps");
  }

  @Test //(timeout = 1000)
  public void tesBasicEmptyDeps(TestContext ctx) throws ScriptException, NoSuchMethodException {
    run(ctx, "basic_empty_deps");
  }

  @Test //(timeout = 1000)
  public void tesBasicRequire(TestContext ctx) throws ScriptException, NoSuchMethodException {
    run(ctx, "basic_require");
  }
}
