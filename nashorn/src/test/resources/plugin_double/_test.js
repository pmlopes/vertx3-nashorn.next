define(["_reporter", "require"], function(amdJS, require) {
  var count = 0;

  var done = function() {
    count++;
    if (count === 2) {
      amdJS.assert(true, 'plugin_double: double plugin called okay');
      amdJS.done();
    }
  };

  require(['double!foo'],
  function (foo) {
    if (foo === 'x') {
      done();
    }
  });

  require(['double!foo'],
  function (foo) {
    if (foo === 'x') {
      done();
    }
  });
});