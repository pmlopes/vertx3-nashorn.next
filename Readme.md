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

## Bootstrap a project

You could reuse the runner jar but before you have to bootstap one. Bootstraping a runner is as simple as copy and paste
a xml file. You should have the following:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">

  <modelVersion>4.0.0</modelVersion>
  <packaging>pom</packaging>

  <groupId>com.jetdrone</groupId>
  <artifactId>jsexample</artifactId>
  <version>1.0.0-SNAPSHOT</version>

  <dependencies>
    <dependency>
      <groupId>io.vertx</groupId>
      <artifactId>polyglot.nashorn</artifactId>
      <version>1.0</version>
    </dependency>
  </dependencies>

  <build>
    <plugins>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-shade-plugin</artifactId>
        <version>2.3</version>
        <executions>
          <execution>
            <phase>package</phase>
            <goals>
              <goal>shade</goal>
            </goals>
            <configuration>
              <transformers>
                <transformer implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
                  <manifestEntries>
                    <Main-Class>io.vertx.core.Launcher</Main-Class>
                    <Main-Verticle>com.jetdrone.nashorn.next.NashornAMDVerticle</Main-Verticle>
                  </manifestEntries>
                </transformer>
                <transformer implementation="org.apache.maven.plugins.shade.resource.AppendingTransformer">
                  <resource>META-INF/services/io.vertx.core.spi.VerticleFactory</resource>
                </transformer>
              </transformers>
              <artifactSet>
              </artifactSet>
              <outputFile>${project.basedir}/run.jar</outputFile>
            </configuration>
          </execution>
        </executions>
      </plugin>
    </plugins>
  </build>

</project>
```

This `pom` will give you only `vertx-core` so if you want other java components to be available you should include them
to the dependencies block as you would with maven. This will create a runnable jar named `run.jar` in the root of your
project.

After that just add your javascript code.