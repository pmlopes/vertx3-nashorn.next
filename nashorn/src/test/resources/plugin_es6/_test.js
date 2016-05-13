define(["_reporter", "require"], function (amdJS, require) {

  require(['classpath:es6!src/class'], function (ES6Class) {
    amdJS.assert(ES6Class, 'es6class: loaded');
    amdJS.assert(true, 'plugin_es6: ES6 plugin called okay');
    amdJS.done();
  });
});
