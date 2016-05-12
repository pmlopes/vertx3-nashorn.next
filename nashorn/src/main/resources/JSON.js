(function (JSON) {
  var JsonArray = Java.type('io.vertx.core.json.JsonArray');
  var JsonObject = Java.type('io.vertx.core.json.JsonObject');

  // this will wrap the original function to handle Vert.x native types too
  var _stringify = JSON.stringify;

  // patch the original JSON object
  JSON.stringify = function () {
    var val = arguments[0];
    if (val instanceof JsonArray || val instanceof JsonObject) {
      return val.encode();
    }
    return _stringify.apply(JSON, Array.prototype.slice.call(arguments))
  };
})(JSON);
