package com.jetdrone.nashorn.next;

import jdk.nashorn.api.scripting.JSObject;

import javax.script.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * This emulates NodeJS require function
 *
 *
   require(X) from module at path Y
   1. If X is a core module,
   a. return the core module
   b. STOP
   2. If X begins with './' or '/' or '../'
   a. LOAD_AS_FILE(Y + X)
   b. LOAD_AS_DIRECTORY(Y + X)
   3. LOAD_NODE_MODULES(X, dirname(Y))
   4. THROW "not found"

   LOAD_AS_FILE(X)
   1. If X is a file, load X as JavaScript text.  STOP
   2. If X.js is a file, load X.js as JavaScript text.  STOP
   3. If X.json is a file, parse X.json to a JavaScript Object.  STOP
   4. If X.node is a file, load X.node as binary addon.  STOP

   LOAD_AS_DIRECTORY(X)
   1. If X/package.json is a file,
   a. Parse X/package.json, and look for "main" field.
   b. let M = X + (json main field)
   c. LOAD_AS_FILE(M)
   2. If X/index.js is a file, load X/index.js as JavaScript text.  STOP
   3. If X/index.json is a file, parse X/index.json to a JavaScript object. STOP
   4. If X/index.node is a file, load X/index.node as binary addon.  STOP

   LOAD_NODE_MODULES(X, START)
   1. let DIRS=NODE_MODULES_PATHS(START)
   2. for each DIR in DIRS:
   a. LOAD_AS_FILE(DIR/X)
   b. LOAD_AS_DIRECTORY(DIR/X)

   NODE_MODULES_PATHS(START)
   1. let PARTS = path split(START)
   2. let I = count of PARTS - 1
   3. let DIRS = []
   4. while I >= 0,
   a. if PARTS[I] = "node_modules" CONTINUE
   c. DIR = path join(PARTS[0 .. I] + "node_modules")
   b. DIRS = DIRS + DIR
   c. let I = I - 1
   5. return DIRS
 */
public final class JSRequire implements JSObject {

  private static final Pattern FQCN = Pattern.compile("^[a-z][a-z0-9_]*(\\.[a-z0-9_]+)+[0-9a-z_]$", Pattern.CASE_INSENSITIVE);

  private final ScriptEngine engine;
  private final Bindings global;

  private final String root;
  private final Map<String, Object> cache = new ConcurrentHashMap<>();
  private final Deque<String> loading = new ArrayDeque<>();

  // TODO: missing properties
  // main (the startup module)
  // cache (needs to be exposed)

  JSRequire(ScriptEngine engine, Bindings global) {
    this(engine, global, System.getProperty("user.dir"));
  }

  JSRequire(ScriptEngine engine, Bindings global, String root) {
    this.engine = engine;
    this.global = global;
    this.root = root;
  }

  Object require(String path) throws ScriptException {
    // is this a java object?
    if (FQCN.matcher(path).matches()) {
      if (cache.containsKey(path)) {
        return cache.get(path);
      } else {
        try {
          Class.forName(path);
          // native type
          Object module = engine.eval("Java.type('" + path + "')");
          cache.put(path, module);
          return module;
        } catch (ClassNotFoundException e) {
          throw new ScriptException("Cannot load native object: " + path);
        }
      }
    }

    // in this case we're handling files

    // handle ./ vs global
    final String parent;

    if (path.charAt(0) == '.') {
      // relative module
      parent = loading.size() > 0 ? loading.peek() : root;
    } else {
      // TODO: if char(0) == / parent is / else parent + node_modules
      parent = root;
    }

    File file = new File(parent, path);

    // do we have a file?
    if (file.exists()) {
      if (file.isDirectory()) {
        // TODO: lookup of there is a package.json and read the main property default to index.js
        file = new File(file, "index.js");
        if (!file.exists()) {
          throw new ScriptException("Cannot load script: " + file.getAbsolutePath());
        }
      }
    } else {
      // resolve extension
      if (!path.endsWith(".js")) {
        path += ".js";
      }

      file = new File(parent, path);

      if (!file.exists() || file.isDirectory()) {
        throw new ScriptException("Cannot load script: " + file.getAbsolutePath());
      }
    }

    // at this point we should have a resolved path
    final String absolutePath;

    try {
      file = file.getCanonicalFile();
      absolutePath = file.getAbsolutePath();
    } catch (IOException e) {
      throw new ScriptException(e);
    }

    if (cache.containsKey(absolutePath)) {
      return cache.get(absolutePath);
    } else {
      final ScriptContext context = new SimpleScriptContext();
      Object module = engine.eval("this", context);

      // save the current loading module path
      loading.push(file.getParent());
      cache.put(absolutePath, module);

      context.setBindings(global, ScriptContext.GLOBAL_SCOPE);

      final Bindings locals = new SimpleBindings();
      context.setBindings(locals, ScriptContext.ENGINE_SCOPE);

      // define a new exports
      locals.put("exports", module);
      // load the new script
      engine.eval("load(\"" + absolutePath + "\")", context);

      loading.pop();

      return module;
    }
  }

  @Override
  public Object call(Object o, Object... objects) {
    if (objects == null || objects.length != 1) {
      throw new UnsupportedOperationException("Invalid arguments to require(var:String)");
    }

    try {
      return require(objects[0].toString());
    } catch (ScriptException e) {
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
}
