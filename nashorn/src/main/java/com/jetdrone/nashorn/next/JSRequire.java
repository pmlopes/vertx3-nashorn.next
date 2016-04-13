package com.jetdrone.nashorn.next;

import jdk.nashorn.api.scripting.JSObject;

import javax.script.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * This emulates NodeJS require function
 * <p>
 * <p>
 * require(X) from module at path Y
 * 1. If X is a core module,
 * a. return the core module
 * b. STOP
 * 2. If X begins with './' or '/' or '../'
 * a. LOAD_AS_FILE(Y + X)
 * b. LOAD_AS_DIRECTORY(Y + X)
 * 3. LOAD_NODE_MODULES(X, dirname(Y))
 * 4. THROW "not found"
 * <p>
 * LOAD_AS_FILE(X)
 * 1. If X is a file, load X as JavaScript text.  STOP
 * 2. If X.js is a file, load X.js as JavaScript text.  STOP
 * 3. If X.json is a file, parse X.json to a JavaScript Object.  STOP
 * 4. If X.node is a file, load X.node as binary addon.  STOP
 * <p>
 * LOAD_AS_DIRECTORY(X)
 * 1. If X/package.json is a file,
 * a. Parse X/package.json, and look for "main" field.
 * b. let M = X + (json main field)
 * c. LOAD_AS_FILE(M)
 * 2. If X/index.js is a file, load X/index.js as JavaScript text.  STOP
 * 3. If X/index.json is a file, parse X/index.json to a JavaScript object. STOP
 * 4. If X/index.node is a file, load X/index.node as binary addon.  STOP
 * <p>
 * LOAD_NODE_MODULES(X, START)
 * 1. let DIRS=NODE_MODULES_PATHS(START)
 * 2. for each DIR in DIRS:
 * a. LOAD_AS_FILE(DIR/X)
 * b. LOAD_AS_DIRECTORY(DIR/X)
 * <p>
 * NODE_MODULES_PATHS(START)
 * 1. let PARTS = path split(START)
 * 2. let I = count of PARTS - 1
 * 3. let DIRS = []
 * 4. while I >= 0,
 * a. if PARTS[I] = "node_modules" CONTINUE
 * c. DIR = path join(PARTS[0 .. I] + "node_modules")
 * b. DIRS = DIRS + DIR
 * c. let I = I - 1
 * 5. return DIRS
 */
public final class JSRequire implements JSObject {

  private static final Pattern FQCN = Pattern.compile("^[a-z][a-z0-9_]*(\\.[a-z0-9_]+)+[0-9a-z_]$", Pattern.CASE_INSENSITIVE);

  private final ScriptEngine engine;

  private final String root;
  private final Map<String, Object> cache = new ConcurrentHashMap<>();
  private final Deque<String> loading = new ArrayDeque<>();

  // TODO: missing properties
  // main (the startup module)
  // cache (needs to be exposed)

  JSRequire(ScriptEngine engine) {
    this(engine, System.getProperty("user.dir"));
  }

  JSRequire(ScriptEngine engine, String root) {
    this.engine = engine;
    this.root = root;
  }

  /**
   * The entry point, it should behave like this:
   * <p>
   * require(X) from module at path Y
   * 1. If X is a native module,
   * a. return the core module
   * b. STOP
   * 2. If X begins with './' or '/' or '../'
   * a. LOAD_AS_FILE(Y + X)
   * b. LOAD_AS_DIRECTORY(Y + X)
   * 3. LOAD_NODE_MODULES(X, dirname(Y))
   * 4. THROW "not found"
   */
  Object load(String id) throws ScriptException, NoSuchMethodException {

    Object module;

    // step 1

    // is this a java object?
    if (FQCN.matcher(id).matches()) {
      if (cache.containsKey(id)) {
        return cache.get(id);
      } else {
        try {
          Class.forName(id);
          // native type
          module = engine.eval("Java.type('" + id + "')");
          cache.put(id, module);
          return module;
        } catch (ClassNotFoundException e) {
          // special case, this isn't a class file, fall back to step 2
        } catch (RuntimeException e) {
          throw new ScriptException(e);
        }
      }
    }

    // step 2

    if (id.charAt(0) == '.' || id.charAt(0) == '/') {
      final String parent;

      if (id.charAt(0) == '.') {
        // relative module
        parent = loading.size() > 0 ? loading.peek() : root;
      } else {
        parent = root;
      }

      module = loadFile(parent, id);
      if (module == null) {
        module = loadAsDirectory(parent, id);
      }
      if (module != null) {
        return module;
      }
    }

    // step 3
    module = loadNodeModule(id, null);
    if (module != null) {
      return module;
    }

    // step 4
    throw new ScriptException("module not found: " + id);
  }

  /**
   * 1. If f is a file, load f as JavaScript text.  STOP
   * 2. If f.js is a file, load f.js as JavaScript text.  STOP
   * 3. If f.json is a file, parse f.json to a JavaScript Object.  STOP
   */
  private Object loadFile(String parent, String id) throws ScriptException, NoSuchMethodException {

    File f = new File(parent, id);
    boolean json = id.endsWith(".json");

    // 1. If f is a file, load f as JavaScript text.
    if (!f.exists() || !f.isFile()) {
      // if file was not found, stop assuming it is JSON
      json = false;
      // 2. If f.js is a file, load f.js as JavaScript text.
      f = new File(parent, id + ".js");
      if (!f.exists() || !f.isFile()) {
        // 3. If f.json is a file, parse f.json to a JavaScript Object.
        f = new File(parent, id + ".json");
        if (!f.exists() || !f.isFile()) {
          // not found, cannot do anything
          return null;
        }
        // special care
        json = true;
      }
    }

    // at this point we should have a resolved path
    final String absolutePath;

    // this is the trick to get nashorn debugger to work, we need absolute paths
    try {
      f = f.getCanonicalFile();
      absolutePath = f.getAbsolutePath();
    } catch (IOException e) {
      throw new ScriptException(e);
    }

    if (cache.containsKey(absolutePath)) {
      return cache.get(absolutePath);
    } else {

      if (json) {
        Object module = loadJSON(f);
        cache.put(absolutePath, module);
        return module;
      }

      // main module
      if (loading.size() == 0) {
        // save the current loading module path and context
        loading.push(f.getParent());
        // load the new script
        ((Invocable) engine).invokeFunction("load", absolutePath);
        // done
        loading.pop();
        return "<main>";
      }

      // create the module on the main context
      JSObject module = (JSObject) engine.eval("(function () { return {exports: {}}; })()");

      // save the current loading module path and context
      loading.push(f.getParent());

      // save the temp ref for circular loading
      cache.put(absolutePath, module.getMember("exports"));

      final Bindings bindings = new SimpleBindings();

      // define temporal locals
      bindings.put("module", module);
      bindings.put("exports", module.getMember("exports"));
      // load the new script
      engine.eval("load(\"" + escapeJSString(absolutePath) + "\")", bindings);

      loading.pop();

      // force cache update if references got changed
      cache.put(absolutePath, module.getMember("exports"));
      return module.getMember("exports");
    }
  }

  /**
   * 1. If X/package.json is a file,
   * a. Parse X/package.json, and look for "main" field.
   * b. let M = X + (json main field)
   * c. LOAD_AS_FILE(M)
   * 2. If X/index.js is a file, load X/index.js as JavaScript text.  STOP
   * 3. If X/index.json is a file, parse X/index.json to a JavaScript object. STOP
   */
  private Object loadAsDirectory(String parent, String id) throws ScriptException, NoSuchMethodException {
    final File dir = new File(parent, id);
    File f;

    if (dir.isDirectory()) {
      f = new File(dir, "package.json");
      // 1. If X/package.json is a file,
      if (f.exists() && f.isFile()) {
        // a. Parse X/package.json, and look for "main" field.
        JSObject packageJson = loadJSON(f);
        // b. let M = X + (json main field)
        String main = (String) packageJson.getMember("main");
        if (main != null) {
          // c. LOAD_AS_FILE(M)
          return loadFile(dir.getAbsolutePath(), main);
        }
      }

      f = new File(dir, "index.js");
      // 1. If X/package.json is a file,
      if (f.exists() && f.isFile()) {
        return loadFile(dir.getAbsolutePath(), "index.js");
      }

      f = new File(dir, "index.json");
      // 1. If X/package.json is a file,
      if (f.exists() && f.isFile()) {
        return loadFile(dir.getAbsolutePath(), "index.json");
      }
    }

    return null;
  }

  private Object loadNodeModule(String id, File start) {
    return null;
  }

  private List getModulePaths(String start) {
    return null;
  }

  private JSObject loadJSON(File f) throws ScriptException, NoSuchMethodException {
    final String jsonString;
    try {
      jsonString = new Scanner(f).useDelimiter("\\A").next();
    } catch (FileNotFoundException e) {
      // should not happen
      throw new ScriptException(e);
    }

    return (JSObject) ((Invocable) engine).invokeMethod(engine.get("JSON"), "parse", jsonString);
  }

  @Override
  public Object call(Object o, Object... objects) {
    if (objects == null || objects.length != 1) {
      throw new UnsupportedOperationException("Invalid arguments to require(var:String)");
    }

    try {
      return load(objects[0].toString());
    } catch (ScriptException | NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Object newObject(Object... objects) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object eval(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object getMember(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object getSlot(int i) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean hasMember(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean hasSlot(int i) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void removeMember(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setMember(String s, Object o) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setSlot(int i, Object o) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Set<String> keySet() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Collection<Object> values() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isInstance(Object o) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isInstanceOf(Object o) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getClassName() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isFunction() {
    return true;
  }

  @Override
  public boolean isStrictFunction() {
    return false;
  }

  @Override
  public boolean isArray() {
    return false;
  }

  @Override
  @Deprecated
  public double toNumber() {
    throw new UnsupportedOperationException();
  }


  private static String escapeJSString(String str) {
    return escapeJavaStyleString(str, false, false);
  }

  private static String escapeJavaStyleString(String str, boolean escapeSingleQuote, boolean escapeForwardSlash) {
    if (str == null) {
      return null;
    }

    final StringBuilder out = new StringBuilder(str.length() * 2);

    int sz;
    sz = str.length();
    for (int i = 0; i < sz; i++) {
      char ch = str.charAt(i);

      // handle unicode
      if (ch > 0xfff) {
        out.append("\\u").append(hex(ch));
      } else if (ch > 0xff) {
        out.append("\\u0").append(hex(ch));
      } else if (ch > 0x7f) {
        out.append("\\u00").append(hex(ch));
      } else if (ch < 32) {
        switch (ch) {
          case '\b':
            out.append('\\');
            out.append('b');
            break;
          case '\n':
            out.append('\\');
            out.append('n');
            break;
          case '\t':
            out.append('\\');
            out.append('t');
            break;
          case '\f':
            out.append('\\');
            out.append('f');
            break;
          case '\r':
            out.append('\\');
            out.append('r');
            break;
          default:
            if (ch > 0xf) {
              out.append("\\u00").append(hex(ch));
            } else {
              out.append("\\u000").append(hex(ch));
            }
            break;
        }
      } else {
        switch (ch) {
          case '\'':
            if (escapeSingleQuote) {
              out.append('\\');
            }
            out.append('\'');
            break;
          case '"':
            out.append('\\');
            out.append('"');
            break;
          case '\\':
            out.append('\\');
            out.append('\\');
            break;
          case '/':
            if (escapeForwardSlash) {
              out.append('\\');
            }
            out.append('/');
            break;
          default:
            out.append(ch);
            break;
        }
      }
    }

    return out.toString();
  }

  private static String hex(char ch) {
    return Integer.toHexString(ch).toUpperCase(Locale.ENGLISH);
  }
}
