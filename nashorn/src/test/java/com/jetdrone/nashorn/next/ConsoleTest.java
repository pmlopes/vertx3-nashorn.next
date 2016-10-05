package com.jetdrone.nashorn.next;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import javax.script.*;

@RunWith(VertxUnitRunner.class)
public class ConsoleTest {

  private ScriptEngine engine;

  private static Vertx vertx;

  @BeforeClass
  public static void beforeClass() {
    vertx = Vertx.vertx();
  }

  @AfterClass
  public static void afterClass() {
    vertx.close();
  }

  @Before
  public void setup() throws ScriptException, NoSuchMethodException {
    Loader loader = new DummyLoader(vertx);
    engine = loader.getEngine();
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

    engine.eval("try { throw new Error('durp!'); } catch (e) { console.trace(e); }\n//@ sourceURL=/index.js");
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
}
