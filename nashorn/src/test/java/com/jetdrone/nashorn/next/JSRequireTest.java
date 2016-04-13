package com.jetdrone.nashorn.next;

import org.junit.Test;

import javax.script.*;

import java.io.*;
import java.net.URL;

import static org.junit.Assert.*;

public class JSRequireTest {

  private static JSRequire require;

  @org.junit.BeforeClass
  public static void setUp() throws Exception {
    ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");

    URL url = JSRequireTest.class.getClassLoader().getResource("empty.txt");
    assertNotNull(url);

    Bindings bindings = new SimpleBindings();
    // create a script loader
    require = new JSRequire(engine, new File(url.getPath()).getParent());

    // emulate NodeJS require
    bindings.put("require", require);
    engine.setBindings(bindings, ScriptContext.GLOBAL_SCOPE);
  }

  @Test
  public void testEmpty() throws ScriptException, NoSuchMethodException {
    assertNotNull(require.load("./empty"));
  }

  @Test
  public void testRequireCycle() throws ScriptException, NoSuchMethodException {
    assertNotNull(require.load("./main"));

    // expected output:

//    main starting
//    a starting
//    b starting
//    in b, a.done = false
//    b done
//    in a, b.done = true
//    a done
//    in main, a.done=true, b.done=true
  }

  @Test
  public void testPlainRequire() throws ScriptException, NoSuchMethodException {
    assertNotNull(require.load("./loader_test.js"));
  }
}
