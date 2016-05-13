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
