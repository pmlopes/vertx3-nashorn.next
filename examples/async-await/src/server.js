var WebClient = Java.type('io.vertx.webclient.WebClient');

(async function () {
  try {
    var response = await Promise.devertxify(WebClient.create(vertx).get(80, "www.google.com", "/"), 'send');
    console.log("Received response with status code: " + response.statusCode());
  } catch (e) {
    console.error(e);
  }
})();
