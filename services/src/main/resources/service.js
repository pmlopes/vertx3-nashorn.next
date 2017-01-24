var Future = require('vertx-js/future');
var MyService = require('jetdrone-services-js/my_service');
// force nashorn to convert to this type later on
var JMyService = Java.type('io.jetdrone.services.MyService');

MyService.registerService(vertx, 'io.jetdrone.services', {
  _jdel: new JMyService ({
    sayHello: function (handler) {
      handler.handle(Future.succeededFuture('Hello there!'));
    }
  })
});
