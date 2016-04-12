var Vertx = require('io.vertx.core.Vertx');

var vertx = Vertx.vertx();

vertx.createHttpServer().requestHandler(function (req) {
  req.response()
    .putHeader("content-type", "text/plain")
    .end("Hello from Vert.x!");
}).listen(80, function (ar) {
  if (ar.failed()) {
    return ar.cause().printStackTrace();
  }

  print('Server ready!');
});
