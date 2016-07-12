package com.jetdrone.scala.next

import io.vertx.core.{AbstractVerticle, Handler}
import io.vertx.core.http.HttpServerRequest
import io.vertx.core.logging.LoggerFactory

class ScalaVerticle extends AbstractVerticle {

  private final val log = LoggerFactory.getLogger("com.jetdrone.scala.next.ScalaVerticle")

  override def start() {

    def helloWorld = new Handler[HttpServerRequest] {
      override def handle(req: HttpServerRequest): Unit = {
        req.response()
          .putHeader("content-type", "text/plain")
          .end("Hello from Scala Vert.x!")
      }
    }

    vertx.createHttpServer()
      .requestHandler(helloWorld)
      .listen(8080)

    println("Server started!")
  }
}
