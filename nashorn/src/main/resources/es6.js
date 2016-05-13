// the babel transpiler is patched to generated modules using always this plugin
// if it gets updated it needs to be "fixed" by adding the filter:
//   sources.filter(function (el) {if (el.type === 'StringLiteral' && el.value && ['require', 'exports', 'module', 'vertx'].indexOf(el.value) === -1) { el.value = 'classpath:es6!' + el.value; } return true; })
// to the sources array in :
//   n.body={MODULE_NAME: m, SOURCES: o, FACTORY: f}
define(['classpath:babel/babel.min'], function (babel) {
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

      var es6Module = load({
        script: babel.transform(fetchText(url), {
          presets: ['es2015'],
          sourceMaps: 'inline',
          sourceFileName: url,
          moduleId: 'classpath:es6!' + name,
          plugins: ['transform-es2015-modules-amd']
        }).code,
        name: url
      });

      onload(es6Module);
    }
  };
});

//define(['vertx', 'require'], function (vertx, require) {
//  return {
//    /**
//     * AMD plugin to load ES6 modules
//     *
//     * @param {string} name
//     * @param {function()} req
//     * @param {function()} onload
//     * @param {Object=} config
//     */
//    load: function (name, req, onload, config) {
//
//      var url = req.toUrl(name + '.js');
//
//      // avoid blocking the event-loop (loading the compiler)
//      vertx.executeBlocking(
//        function (future) {
//          require(['classpath:babel/babel.min'], function (Babel) {
//            var compiled = Babel.transform(fetchText(url), {
//              presets: ['es2015'],
//              sourceMaps: 'inline',
//              sourceFileName: url,
//              moduleId: 'classpath:es6!' + name,
//              plugins: ['transform-es2015-modules-amd']
//            }).code;
//
//            try {
//              future.complete(load({
//                script: compiled,
//                name: url
//              }));
//            } catch (e) {
//              future.fail(e);
//            }
//          });
//        },
//        function (res) {
//          if (res.succeeded()) {
//            onload(res.result());
//          }
//        }
//      );
//    }
//  };
//});
