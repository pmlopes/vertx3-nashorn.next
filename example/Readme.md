# Example Vert.x nashorn.next APP

This is an experimental alternative to standard Vert.x javascript applications. The ideas behind this are:

* Write JS for Nashorn
* Interop with any JVM object
* Debug from your IDE or for the faint of heart `jdb`
* Avoid maven
* Simple modules that do not polute the global namespace

## State of the art

Currently Javascript'ing Vert.x uses Nashorn as its default runtime engine. This is expected since all code is to be run
on a JVM. Sadly Nashorn lacks a proper module loader and behaves a bit like a dumb web browser runtime.

One of the good things about Nashorn is that it improves over Rhino in terms of language spec `EcmaScript 5.1` and
performance, which in some benchmarks are quite close to `V8`.

In order to overcome these lacks, Vert.x currently uses `npm-jvm.js` to emulate `Node.JS` `commonJS` loader. This turns
out to be a problem once one is interested in debugging a live application with a interactive debugger. The breakpoints
are not respected.

Also the exceptions thrown by the current loader are always handled on the Java side of the things which means that JS
calls are shown as mangled names.

`CommonJS` modules are simple to write and very popular on node world and that is sometimes a problem since Vert.x users
will attempt to use node modules and do not understand that modules fail to load because the native node modules are not
present.

## Proposed nashorn.next

For this new loader there are several ground breaking changes:

* No more CommonJS modules (not entirely true)
* No need for maven (unless user needs extra java libraries bundled in the runner)
* Currently no packaging is provided (distribution is left to the user)
* A new loader based on AMD (this is small and allows a proper debugging experience)

### Additions

There are several additions to the runtime:

* AMD define function - the new loader
* Console object
* type AMD plugin - helper to load any java type
* quit and end functions properly end vert.x

### AMD support

The AMD loader currently supports (passes the official AMD test suite):

* anonymous circular module dependencies
* anonymous relative modules
* anonymous simple modules
* basic circular module dependencies
* basic defined modules
* basic empty dependencies
* basic without dependencies
* basic simple modules
* basic config for paths
* basic config for relative paths
* basic plugin loading

Currently missing are:

* loading commonJS defined module
* loading commonJS named module

Plugin system limitations:

* No support for dynamic plugins

### AMD defined objects

The `define` function internally defines the following objects:

* `require()` the require function to load modules within the module definition itself
* `module` the module object
* `exports` basically like commonJS is a link to `module.exports`
* `vertx` this is an extension and is a reference to the current running vertx instance.

### Config the loader

The loader has a config function that can handle 3 types of configuration:

* `baseUrl` when ommited falls back to the current working directory
* `paths` a object following AMD spec, a key (source) and value (target)
* `plugins` a general object that given a key name will be passed to the plugin when loading an object

In order to provide this config you should have a JSON document with the desired config and be provided to the runner
with `--conf your_json.json`. The runner is the base vert.x Loader so all flags are provided such as `--cluster`,
etc....

## How to use

A typical AMD module main module is the file `main.js` so the expected start of your application should be on `main.js`.

To use other than the current working directory to locate your main you should look at the config `baseUrl`.

A hello http server can be written as (`main.js`):

```js
define(['vertx'], function (vertx, durp) {

  vertx.createHttpServer().requestHandler(function (req) {
    req.response()
      .putHeader("content-type", "text/plain")
      .end("Hello from Vert.x!");
  }).listen(8080, function (ar) {
    if (ar.failed()) {
      return ar.cause().printStackTrace();
    }

    console.log('Server ready!');
  });
});
```

## How to debug

In order to debug you need to run:

```
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 -jar run.jar
```

After that use a java debugger and remote connect to port 5005 (note that the port is defined in the command above so
you can use any port you like). Once you do this, add breakpoints to the js file (`main.js`) and you should see that you
can interactively debug your code.

### Debugging with IDE

Debugging is known to work out of the box with:

* IntelliJ
* Netbeans

Not tested:

* Eclipse
