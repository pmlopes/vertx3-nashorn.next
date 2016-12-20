var Router = Java.type('io.vertx.ext.web.Router');

var router = Router.router(vertx);

router.route().handler(function (ctx) {
  ctx.response()
    .putHeader("content-type", "text/html")
    .end("Hello World!");
});

vertx.createHttpServer().requestHandler(function (req) { router.accept(req); }).listen(8080);
