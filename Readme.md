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

There are 2 main alternatives here:

### Alternative #1

For this new loader there are several ground breaking changes:

* No more CommonJS modules (not entirely true)
* No need for maven (unless user needs extra java libraries bundled in the runner)
* Currently no packaging is provided (distribution is left to the user)
* A new loader based on RequireJS (this is small and allows a proper debugging experience)
* Builtin support for ES6 using babel compiler as a loader plugin

#### Additions

There are several additions to the runtime:

* RequireJS define function - the new loader
* Console object
* type AMD plugin - helper to load any java type
* quit and end functions properly end vert.x
* Nashorn JSON codec to eventbus
* Enhanced JSON.stringify

#### AMD support

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
* loading commonJS defined module
* loading commonJS named module

Plugin system limitations:

* No support for dynamic plugins

#### AMD defined objects

The `define` function internally defines the following objects:

* `require()` the require function to load modules within the module definition itself
* `module` the module object
* `exports` basically like commonJS is a link to `module.exports`
* `vertx` this is an extension and is a reference to the current running vertx instance.

#### Config the loader

The loader has a config function that can handle 3 types of configuration:

* `baseUrl` when ommited falls back to the current working directory
* `paths` a object following AMD spec, a key (source) and value (target)
* `plugins` a general object that given a key name will be passed to the plugin when loading an object

In order to provide this config you should have a JSON document with the desired config and be provided to the runner
with `--conf your_json.json`. The runner is the base vert.x Loader so all flags are provided such as `--cluster`,
etc....

## Option #2

Alternatively one can take a different approach. Why not just use the tooling that frontend developers are used too, for example:

* Babel
* Webpack

By using this simple stack one can quickly solve the vast majority of the issues we're facing on nashorn as of today. We can use the latest language features of javascript (ES6) in contrast to (ES5) that nashorn supports. It solves the issue of module loading by resolving them from the current standard `npm` and bundle everything in a single file.

This alternative is not perfect though, even though debugging does work it always refers to the single file, line column and not to the original source. The reason is that nashorn **does** support source location mapping but **not** source-maps. This could trigger and attempt to port the original sourcemap implementation made by mozilla to translate stacktraces at runtime. Again this approach would not solve the resolution of code points at runtime debugging.


### JS object and the eventbus

This loader defines a user codec that will transform JS objects or arrays to `JsonObject` or `JsonArray` when sent over
the eventbus.

### Enhanced JSON.stringify

The `JSON.stringify` function has been enhanced to also accept `JsonObject`, `JsonArray`, `Map` and `List` objects, in
this case the optional arguments (replacer and space) are ignored.

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
                    <Main-Verticle>com.jetdrone.nashorn.next.RequireJSVerticle</Main-Verticle>
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

## How to use with Docker compose

This loader suits perfect with docker compose. As an example, this is how you would build a web counter app backed by
redis. First you implement your application `main.js` as:

```js
define(['vertx', 'classpath:type!io.vertx.ext.web.Router', 'classpath:type!io.vertx.redis.RedisClient', 'classpath:type!io.vertx.redis.RedisOptions'], function (vertx, Router, RedisClient, RedisOptions) {

  // Create the redis client
  var redis = RedisClient.create(vertx, new RedisOptions().setHost('redis'));
  var router = Router.router(vertx);

  router.route().handler(function (ctx) {
    redis.incr('hits', function (res) {
      if (res.failed()) {
        ctx.fail(res.cause());
        return;
      }

      ctx.response()
        .putHeader('content-type', 'text/html')
        .end('Hello World! I have been seen ' + res.result() + ' times.');
    });
  });

  vertx.createHttpServer().requestHandler(function (req) {
    router.accept(req);
  }).listen(8080, '0.0.0.0', function (ar) {
    if (ar.failed()) {
      ar.cause().printStackTrace();
      exit(1);
    }

    console.log('Server ready!');
  });
});
```

After in the same directory add a `Dockerfile`:

```Dockerfile
FROM vertx-nashorn:latest
COPY . /usr/src/app
EXPOSE 8080
```

The `vertx-nashorn:latest` image is provided in the examples. This is just a JVM image with the runner jar as described
in bootstrap a project.

Finally to link to Redis you need to compose your deployment, for this you add the following `docker-compose.yml`:

```yaml
web:
  build: .
#  command: "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 -jar /usr/vertx/run.jar"
  ports:
#   - "5005:5005"
   - "8080:8080"
  links:
   - redis
redis:
  image: redis
```

In case you want to remote debug your application uncomment the command and also the 5005 port. Docker compose gives you
a quick development environment setup and once you're happy you can publish your application using the provided
Dockerfile.

## Using EcmaScript6

Nashorn on JDK8 does not support EcmaScript6, however its support can be achieved using Babel to transpile ES6 code to
ES5. Say for example that you want to write your next vertx-web application using ES6, you would do something like:

```js
import vertx from 'vertx';
import Router from 'classpath:type!io.vertx.ext.web.Router';

const router = Router.router(vertx);

router.route().handler(ctx => {
  ctx.response()
    .putHeader("content-type", "text/html")
    .end("Hello World!");
});

vertx.createHttpServer().requestHandler(req => {
  router.accept(req);
}).listen(8080);
```

Now nashorn does not support this out of the box, so there is a special bundled plugin that will perform the
transpilation from ES6 to ES5 and using this loader. For this you need to bootstrap your application like:

```js
define(['classpath:es6!app/main'], function (app) {
  console.log('Application loaded!');
});
```

It is not recommended to mix ES6 and ES5 since the transpiled code will be using all the babel infrastructure so types
will most likely not match or be compatible. However basic JSON objects and closures **should** be fine.
