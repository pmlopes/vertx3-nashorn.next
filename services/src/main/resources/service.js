var Future = require('vertx-js/future');
var MyService = require('jetdrone-services-js/my_service');
// force nashorn to convert to this type later on
var JMyService = Java.type('io.jetdrone.services.MyService');

MyService.registerService(
  vertx,
  'io.jetdrone.services',
  // this is the JS facade
  new MyService(
    // this is the Java Interface implementation
    new JMyService({
      sayHello: function (handler) {
        handler.handle(Future.succeededFuture('Hello there!'));
      }
    })
  )
);
