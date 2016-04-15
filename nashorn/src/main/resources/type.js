define({
  /**
   * AMD plugin to load JVM types
   *
   * @param {string} name
   * @param {function()} parentRequire
   * @param {function()} onload
   * @param {Object=} config
   */
  load: function (name, parentRequire, onload, config) {
    onload(Java.type(name));
  }
});
