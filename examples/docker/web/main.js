define(['vertx', 'classpath:type!io.vertx.ext.web.Router', 'classpath:type!io.vertx.redis.RedisClient'], function (vertx, Router, RedisClient) {

  // Create the redis client
  var redis = RedisClient.create(vertx);
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