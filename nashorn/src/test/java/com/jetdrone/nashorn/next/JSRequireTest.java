package com.jetdrone.nashorn.next;

import org.junit.Ignore;
import org.junit.Test;

import javax.script.*;

import java.io.File;
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
    require = new JSRequire(engine, bindings, new File(url.getPath()).getParent());

    // emulate NodeJS require
    bindings.put("require", require);
    engine.setBindings(bindings, ScriptContext.GLOBAL_SCOPE);
  }

  @Test
  public void testPlainRequire() throws ScriptException {
    assertNotNull(require.require("./empty"));
  }

  @Test
  public void testRequireCycle() throws ScriptException {
    assertNotNull(require.require("./main"));

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
  public void testAsClosure() throws ScriptException {
    assertNotNull(require.require("./closure"));

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
}
