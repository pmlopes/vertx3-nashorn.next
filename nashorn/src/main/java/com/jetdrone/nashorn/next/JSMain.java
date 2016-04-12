package com.jetdrone.nashorn.next;

import javax.script.*;
import java.io.FileNotFoundException;

public class JSMain {

  public static void main(String[] args) throws ScriptException, FileNotFoundException {

    String mainScript;

    switch (args.length) {
      case 0:
        mainScript = "./index.js";
        break;
      case 1:
        if (args[0].charAt(0) == '.' || args[0].charAt(0) == '/') {
          mainScript = args[0];
        } else {
          mainScript = "./" + args[0];
        }
        break;
      default:
        throw new ScriptException("Too many startup scripts!");
    }

    ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
    Bindings bindings = new SimpleBindings();

    // create a script loader
    final JSRequire require = new JSRequire(engine, bindings);
    // emulate NodeJS require
    bindings.put("require", require);
    engine.setBindings(bindings, ScriptContext.GLOBAL_SCOPE);

    // start the app by loading the main script
    require.require(mainScript);
  }
}
