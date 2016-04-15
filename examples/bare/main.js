define(['vertx'], function (vertx) {

  vertx.createHttpServer().requestHandler(function (req) {
    req.response()
      .putHeader("content-type", "text/plain")
      .end("Hello from Vert.x!");
  }).listen(8080, function (ar) {
    if (ar.failed()) {
      ar.cause().printStackTrace();
      exit(1);
    }

    console.log('Server ready!');
  });
});