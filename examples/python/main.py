httpServer = vertx.createHttpServer()

def hello_handler(req):
  res = req.response()
  res.putHeader("content-type", "text/plain")
  res.end("Hello from Vert.x!")

httpServer.requestHandler(hello_handler)
httpServer.listen(8080)

print 'Server ready!'
