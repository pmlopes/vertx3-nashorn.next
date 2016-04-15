define(['vertx', 'durp'], function (vertx, durp) {

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