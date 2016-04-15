define(["_reporter", "require"], function (amdJS, require) {

  require(['classpath:type!io.vertx.core.json.JsonObject'], function (JsonObject) {
    var nativeJson = new JsonObject();
    if (nativeJson.encode() === '{}') {
      amdJS.assert(true, 'plugin_vertx: vertx plugin called okay');
      amdJS.done();
    }
  });
});