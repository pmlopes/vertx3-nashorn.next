// This is a similar bootstrap script to JSVerticle, it is useful when working with JJS repl.
vertx = (function (global) {
  if (global.vertx) {
    return;
  }

  var Vertx = Java.type('io.vertx.core.Vertx');
  var NashornJSObjectMessageCodec = Java.type('com.jetdrone.nashorn.next.NashornJSObjectMessageCodec');

  // the vertx instance
  var vertx = Vertx.vertx();

  // load polyfills
  load('classpath:polyfill.js');
  // install the console object
  load('classpath:console.js');
  // update JSON to handle native JsonObject/JsonArray types
  load('classpath:JSON.js');

  // register a default codec to allow JSON messages directly from nashorn to the JVM world
  vertx.eventBus().unregisterDefaultCodec(Java.type('jdk.nashorn.api.scripting.ScriptObjectMirror').class);
  vertx.eventBus().registerDefaultCodec(Java.type('jdk.nashorn.api.scripting.ScriptObjectMirror').class, new NashornJSObjectMessageCodec(JSON, Java));

  // remove the exit and quit functions
  delete global.exit;
  delete global.quit;

  global.exit = function () {
    var exitCode;

    if (arguments) {
      try {
        exitCode = parseInt(Number(arguments[0]), 10);
      } catch (e) {
        exitCode = -1;
      }
    } else {
      exitCode = 0;
    }

    vertx.close(function (res) {
      if (res.failed()) {
        log.fatal("Failed to close", res.cause());
        System.exit(-1);
      } else {
        System.exit(exitCode);
      }
    });
  };

  // re-add exit and quit but a proper one
  global.quit = global.exit;

  // ready
  return vertx;
})(this);
