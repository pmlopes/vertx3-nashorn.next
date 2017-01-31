from io.vertx.ext.web import Router
from xyz import Interop
from xyz.jetdrone.vertx import JSON

router = Router.router(vertx)
greeting = Interop()

# greeting.sayHello(JSON.to_native({"id": 1}))
greeting.sayHello(JSON.to_native(["one", 2]))

def hello_handler(ctx):
  greeting.sayHello(dict)
  ctx.response()\
    .putHeader("content-type", "text/html")\
    .end("Hello from Python Vert.x!")

router.route().handler(hello_handler)

httpServer = vertx.createHttpServer()

def accept(req):
  router.accept(req)

httpServer.requestHandler(accept)
httpServer.listen(8080)

print 'Server ready!'
