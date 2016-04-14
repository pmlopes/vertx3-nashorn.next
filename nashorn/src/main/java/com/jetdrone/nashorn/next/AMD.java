package com.jetdrone.nashorn.next;

import io.vertx.core.Vertx;
import jdk.nashorn.api.scripting.AbstractJSObject;
import jdk.nashorn.api.scripting.JSObject;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.util.*;
import java.util.regex.Pattern;

public class AMD extends AbstractJSObject {

  private static final Pattern FQCN = Pattern.compile("^[a-z][a-z0-9_]*(\\.[a-z0-9_]+)+[0-9a-z_]$", Pattern.CASE_INSENSITIVE);

  private final ScriptEngine engine;
  private final AMDConfig config;
  private final Vertx vertx;

  /**
   * Modules waiting for dependencies to be exported.
   */
  private final List<Module> pendingModules = new LinkedList<>();

  /**
   * New modules since the last script loaded.
   */
  private final List<Module> newModules = new LinkedList<>();

  /**
   * Loaded modules, keyed by id.
   */
  private final Map<String, Module> cache = new HashMap<>();

  /**
   * Names of modules which are loading/loaded.
   */
  private final Map<String, Boolean> loads = new HashMap<>();

  public class Module {

    private final List<String> dependencies;
    private final JSObject factoryFunction;

    private final JSObject exports;
    private final JSObject generator;

    private String id;
    private Object exportValue;

    /**
     * Module definition.
     *
     * @param id           Optional string identifying the module.
     * @param dependencies Optional array of strings identifying the module's dependencies.
     * @param factory      Optional function returning the export value of the module.
     * @param exportValue  Optional export value for modules without a factory.
     * @param generator    Optional function returning a dynamic export value for the module.
     */
    Module(String id, List<String> dependencies, JSObject factory, JSObject exportValue, JSObject generator) throws ScriptException {
      this.id = id;
      this.dependencies = dependencies;
      this.factoryFunction = factory;
      this.exports = (JSObject) engine.eval("new Object()");
      this.generator = generator;

      if (factory == null) {
        this.exportValue = exportValue != null ? exportValue : this.exports;
      } else {
        this.exportValue = null;
      }

      if (id != null) {
        loads.put(id, true);
        cache.put(id, this);
      }
    }

    /**
     * Check dependencies.
     * <p>
     * Checks if all dependencies of a module are ready.
     *
     * @param ignore Module name to ignore, for circular reference check.
     * @return true if all dependencies are ready, else false.
     */
    boolean checkDependencies(String ignore) {
      List<String> dependencies = this.dependencies != null ? this.dependencies : Collections.emptyList();
      Module dep;

      for (int i = dependencies.size() - 1; i != -1; i--) {
        dep = getCached(dependencies.get(i));
        // if the dependency doesn't exist, it's not ready
        if (dep == null) {
          return false;
        }
        // if the dependency already exported something, it's ready
        if (dep.exportValue != null) {
          continue;
        }
        // if the dependency is only blocked by this module, it's ready
        // (circular reference check, this module)
        if (ignore == null && dep.checkDependencies(this.id)) {
          continue;
        }
        // if we're ignoring this dependency, it's ready
        // (circular reference check, dependency of dependency)
        if (ignore != null && (ignore.equals(dep.id))) {
          continue;
        }
        // else it's not ready
        return false;
      }
      return true;
    }

    /**
     * Get dependency value.
     * <p>
     * Gets the value of a cached or builtin dependency module by id.
     *
     * @return the dependency value.
     */
    Object getDependencyValue(String id) {
      Module dep = getCached(id);
      if (dep != null) {
        return dep.generator != null ? dep.generator.call(null, this) : dep.exportValue;
      }

      return null;
    }

    /**
     * Load dependencies.
     */
    void loadDependencies() throws ScriptException, NoSuchMethodException {
      for (int i = dependencies.size() - 1; i != -1; i--) {
        String id = dependencies.get(i);

        if (FQCN.matcher(id).matches()) {
          // native module
          try {
            Class.forName(id);
            // native type
            loads.put(id, true);
            Module module = new Module(id, null, null, (JSObject) engine.eval("Java.type('" + id + "')"), null);
            cache.put(id, module);
            // set export values for modules that have all dependencies ready
            exportValues();
          } catch (ClassNotFoundException e) {
            throw new ScriptException(e);
          }
        } else if (id.startsWith("http:") || id.startsWith("https:") || id.startsWith("classpath:")) {
          // TODO: url loading
          throw new UnsupportedOperationException();
        } else if (id.charAt(0) == '.' || id.charAt(0) == '/' || id.endsWith(".js") || id.endsWith(".json")) {
          // normalize relative deps
          if (this.id.indexOf('/') >= 0) {
            String parent = this.id.replaceFirst("/[^/]*$", "");
            String[] segments = id.split("/");
            for (String segment : segments) {
              if (".".equals(segment) || "".equals(segment)) {
                // skip
                continue;
              }
              if ("..".equals(segment)) {
                // pop element from parent
                parent = parent.replaceFirst("/[^/]*$", "/");
                continue;
              }
              parent += "/" + segment;
            }
            id = parent;
          } else {
            id = "/" + id;
          }
          id = id.replaceAll("[/]\\.[/]", "/");
          dependencies.set(i, id);
        } else {
          // amd module
          // TODO: handle paths
        }

        // load deps that haven't started loading yet
        if (!loads.containsKey(id)) {
          this.loadScript(id);
        }
      }
    }

    /**
     * Load a script by module id.
     *
     * @param id Module id.
     */
    private void loadScript(String id) throws ScriptException, NoSuchMethodException {
      loads.put(id, true);

      ((Invocable) engine).invokeFunction("load", config.getBaseUrl() + "/" + id + ".js");

      boolean hasDefinition = false; // anonymous or matching id
      Module module;

      // loading amd modules
      while(newModules.size() > 0) {
        module = newModules.remove(newModules.size() - 1);

        if (module.id == null || module.id.equals(id)) {
          hasDefinition = true;
          module.id = id;
        }
        if (getCached(module.id) != null) {
          cache.put(module.id, module);
        }
      }
      // loading alien script
      if (!hasDefinition) {
        module = new Module(id, null, null, null, null);
        cache.put(id, module);
      }
      // set export values for modules that have all dependencies ready
      exportValues();
    }
  }

  public AMD(Vertx vertx, ScriptEngine engine, String baseUrl, Map<String, String> paths) throws ScriptException {
    this.vertx = vertx;
    this.engine = engine;
    this.config = new AMDConfig(baseUrl, paths);

    // Built-in dynamic modules
    dynamic("require", module -> {
      final JSObject r = new AbstractJSObject() {
        @Override
        public Object call(Object o, Object... arguments) {
          try {
            return require(arguments);
          } catch (ScriptException | NoSuchMethodException e) {
            throw new RuntimeException(e);
          }
        }

        @Override
        public boolean isFunction() {
          return true;
        }
      };

      r.setMember("toUrl", new AbstractJSObject() {
        @Override
        public Object call(Object o, Object... arguments) {
          String path = "undefined";

          if (arguments != null && arguments.length > 0) {
            path = (String) arguments[0];
          }

          return module.id + '/' + path;
        }

        @Override
        public boolean isFunction() {
          return true;
        }
      });

      return r;
    });

    dynamic("exports", module -> module.exports);

    dynamic("module", module -> module);
  }

  public AMD(Vertx vertx, ScriptEngine engine, String root) throws ScriptException {
    this(vertx, engine, root, null);
  }

  public AMD(Vertx vertx, ScriptEngine engine) throws ScriptException {
    this(vertx, engine, System.getProperty("user.dir"));
  }

  /**
   * Define a module.
   *
   * Wrap Module constructor and fiddle with optional arguments.
   *
   * optional param id - Module id.
   * optional param dependencies - Module dependencies.
   * optional param factory - Module factory.
   */
  private Object define(Object... arguments) throws ScriptException, NoSuchMethodException {
    int argc = arguments != null ? arguments.length : 0;

    String id;
    List<String> dependencies;
    JSObject factory;

    List<String> defaultDeps = Arrays.asList("require", "exports", "module");

    Module module;
    JSObject exportValue = null;

    switch (argc) {
      case 0:
        id = null;
        dependencies = null;
        factory = null;
        break;
      case 1:
        id = null;
        dependencies = defaultDeps;
        factory = (JSObject) arguments[0];
        break;
      case 2:
        factory = (JSObject) arguments[1];

        if (arguments[0] instanceof String) {
          id = (String) arguments[0];
          dependencies = defaultDeps;
        } else {
          id = null;
          dependencies = new LinkedList<>();
          for (Object value : ((JSObject) arguments[0]).values()) {
            dependencies.add((String) value);
          }
        }
        break;
      default:
        id = (String) arguments[0];
        dependencies = new LinkedList<>();
        for (Object value : ((JSObject) arguments[1]).values()) {
          dependencies.add((String) value);
        }
        ;
        factory = (JSObject) arguments[2];
        break;
    }

    if (factory != null && !factory.isFunction()) {
      exportValue = factory;
      factory = null;
    }

    module = new Module(id, dependencies, factory, exportValue, null);

    newModules.add(module);
    pendingModules.add(module);

    vertx.runOnContext(v -> {
      try {
        module.loadDependencies();
      } catch (ScriptException | NoSuchMethodException e) {
        throw new RuntimeException(e);
      }
    });

    exportValues();

    return module;
  }

  /**
   * Built-in require function.
   *
   * If callback is present, call define, else return the export value
   * of the cached module identified by the first argument.
   *
   * https://github.com/amdjs/amdjs-api/blob/master/require.md
   *
   * param dependencies - Module dependencies.
   * optional callback - Module factory.
   *
   * @return Module or null
   */
  private Object require(Object... arguments) throws ScriptException, NoSuchMethodException {
    Object dependencies = arguments != null && arguments.length > 0 ? arguments[0] : null;
    Object callback = arguments != null && arguments.length > 1 ? arguments[1] : null;

    if (dependencies instanceof JSObject && ((JSObject) dependencies).isArray() && callback != null) {
      return define(dependencies, callback);
    } else if (dependencies instanceof String) {
      Module module = getCached((String) dependencies);
      return module != null ? module.exportValue : null;
    } else {
      throw new RuntimeException("malformed require");
    }
  }

  private void dynamic(String id, Generator<Module> generator) throws ScriptException {
    cache.put(id, new Module(id, null, null, null, Generator.create(generator)));
    loads.put(id, true);
  }

  @Override
  public Object call(Object o, Object... arguments) {
    try {
      return define(arguments);
    } catch (ScriptException | NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Export module values.
   * <p>
   * For each module with all dependencies ready, set the
   * export value from the factory or exports object.
   */
  private void exportValues() {
    int count = 0;
    int lastCount = 1;
    Module module;
    JSObject factory;

    while (count != lastCount) {
      lastCount = count;
      for (int i = pendingModules.size() - 1; i != -1; i--) {
        module = pendingModules.get(i);
        if ((module.exportValue == null) && module.checkDependencies(null)) {
          pendingModules.remove(i);
          factory = module.factoryFunction;
          Deque<Object> args = new LinkedList<>();
          for (int j = module.dependencies.size() - 1; j != -1; j--) {
            String id = module.dependencies.get(j);
            args.addFirst(module.getDependencyValue(id));
          }
          Object value = factory.call(module.exports, args.toArray());
          module.exportValue = value != null ? value : module.exports;
          ++count;
        }
      }
    }
  }

  /**
   * Get a cached module.
   *
   * @param id Module id.
   */
  private Module getCached(String id) {
    if (cache.containsKey(id)) {
      return cache.get(id);
    }

    return null;
  }

  @Override
  public Object getMember(String s) {
    if ("amd".equals(s)) {
      // TODO: this should be always the same reference
      try {
        return engine.eval("{}");
      } catch (ScriptException e) {
        throw new RuntimeException(e);
      }
    }

    throw new UnsupportedOperationException();
  }


  @Override
  public boolean isFunction() {
    return true;
  }
}
