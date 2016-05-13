// _reporter.js
(function() {
  var factory = function () {
    var exports = {};

    exports.print = print;

    exports.done = function () {
      test.complete();
    };

    exports.assert = function (guard, message) {
      if (guard) {
        print("PASS " + message, "pass");
      } else {
        print("FAIL " + message, "fail");
      }
      assert.assertTrue(guard, message);
    };

    return exports;
  };

  // define this module
  define("_reporter", [], factory);

})();
