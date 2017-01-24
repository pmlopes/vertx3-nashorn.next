package io.jetdrone.services;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.serviceproxy.ProxyHelper;

@VertxGen
@ProxyGen
public interface MyService {

  void sayHello(Handler<AsyncResult<String>> handler);


  /**
   * Method called to create a proxy (to consume the service).
   *
   * @param vertx   vert.x
   * @param address the address on the vent bus where the service is served.
   * @return the proxy on the {@link MyService}
   */
  static MyService createProxy(Vertx vertx, String address) {
    return ProxyHelper.createProxy(MyService.class, vertx, address);
  }

  /**
   * Method called to create a proxy (to consume the service).
   *
   * @param vertx   vert.x
   * @param address the address on the vent bus where the service is served.
   * @return the proxy on the {@link MyService}
   */
  static void registerService(Vertx vertx, String address, MyService service) {
    ProxyHelper.registerService(MyService.class, vertx, service, address);
  }
}
