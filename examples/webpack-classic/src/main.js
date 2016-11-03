const posts = require('./posts');

const Router = require("vertx-web-js/router");
const app = Router.router(vertx);

app.get('/api/post').handler((ctx) => {
  ctx.response()
    .putHeader("content-type", "application/json")
    .end(JSON.stringify(posts));
});

app.get('/api/post/:id').handler((ctx) => {
  const id = ctx.request().getParam('id');

  const post = posts.filter(p => p.id == id);

  if (post) {
    ctx.response()
      .putHeader("content-type", "application/json")
      .end(JSON.stringify(post[0]));
  } else {
    ctx.fail(404);
  }
});

vertx.createHttpServer().requestHandler(app.accept).listen(8080);
