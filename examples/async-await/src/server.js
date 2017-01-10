// get a reference to the web webclient class from java
const WebClient = Java.type('io.vertx.webclient.WebClient');

// in order to use await one needs to make a call from a async function, so we wrap our main code into
// and async function
(async function () {
  // error handling can be done with try catch
  try {
    let webClient = WebClient.create(vertx).get(80, "www.google.com", "/");
    // since vertx API is callback style we need to convert that into a Promise style, either you do it manually
    // or use this helper that wraps a object and returns a proxy that adds a extra last parameter to all method
    // calls that handles the async result objects
    let response = await Promise.devertxify(webClient).send();
    // there was no threads involved in this code, the call was async and run on vert.x event loop
    // however you did not need to create callbacks and chain functions! hurray!!!
    console.log("Received response with status code: " + response.statusCode());
  } catch (e) {
    // oops! there was an error on the callback but now it is all managed as if it was blocking code,
    // just handle the exception in a try catch block!
    console.error(e);
  }
})();
