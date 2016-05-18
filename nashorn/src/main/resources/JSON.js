(function (JSON) {
  var JsonArray = Java.type('io.vertx.core.json.JsonArray');
  var JsonObject = Java.type('io.vertx.core.json.JsonObject');

  var Map = Java.type('java.util.Map');
  var List = Java.type('java.util.List');

  // this will wrap the original function to handle Vert.x native types too
  var _stringify = JSON.stringify;

  // patch the original JSON object
  JSON.stringify = function () {
    var val = arguments[0];
    if (val instanceof JsonArray || val instanceof JsonObject) {
      return val.encode();
    }
    // convert from map to object
    if (val instanceof Map) {
      return new JsonObject(val).encode();
    }
    // convert from list to array
    if (val instanceof List) {
      return new JsonArray(val).encode();
    }
    return _stringify.apply(JSON, Array.prototype.slice.call(arguments))
  };
})(JSON);
