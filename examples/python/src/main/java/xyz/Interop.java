package xyz;

import io.vertx.core.json.*;

public class Interop {
//  public void sayHello(Object obj) {
//    System.out.println("Hello " + obj.getClass());
//  }

  public void sayHello(JsonObject obj) {
    System.out.println("Hello " + obj.encodePrettily());
  }

  public void sayHello(JsonArray obj) {
    System.out.println("Hello " + obj.encodePrettily());
  }
}
