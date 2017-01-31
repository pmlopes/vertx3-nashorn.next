from io.vertx.core import Future
from io.jetdrone.services import MyService

class PyMyListener (MyService):
  def sayHello(self, handler):
    handler.handle(Future.succeededFuture('Hello there!'))

MyService.registerService(vertx, 'io.jetdrone.services', PyMyListener())
