// the babel transpiler is patched to generated modules using always this plugin
// if it gets updated it needs to be "fixed" by adding the filter:
//   sources.filter(function (el) {if (el.type === 'StringLiteral' && el.value && ['require', 'exports', 'module', 'vertx'].indexOf(el.value) === -1 && el.value.indexOf('!') === -1 && el.value.indexOf(':') == -1) { el.value = 'classpath:es6!' + el.value; } return true; })
// to the sources array in :
//   n.body={MODULE_NAME: m, SOURCES: o, FACTORY: f}
define(['vertx', 'classpath:babel/babel.min'], function (vertx, Babel) {
  return {
    /**
     * AMD plugin to load ES6 modules
     *
     * @param {string} name
     * @param {function()} req
     * @param {function()} onload
     * @param {Object=} config
     */
    load: function (name, req, onload, config) {
      var ext = config.extension || '.js';

      var url = req.toUrl(name + ext);

      // avoid blocking the event-loop (loading the compiler)
      vertx.executeBlocking(
        function (future) {
          try {
            future.complete(Babel.transform(fetchText(url), {
              presets: ['es2015'],
              sourceMaps: 'inline',
              sourceFileName: url,
              moduleId: 'classpath:es6!' + name,
              plugins: ['transform-es2015-modules-amd']
            }).code);
          } catch (e) {
            console.trace(e);
          }
        },
        function (res) {
          if (res.succeeded()) {
            var es6Module = load({
              script: res.result(),
              name: url
            });

            onload(es6Module);
          }
        }
      );
    }
  };
});
