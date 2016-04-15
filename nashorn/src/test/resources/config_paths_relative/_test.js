define.amd.lite.config({
  paths: {
    "array": "impl/array"
  }
});

define(["_reporter", "require", "array"],
  function (amdJS, require, array) {
    amdJS.assert('impl/array' === array.name, 'config_paths_relative: array.name');
    amdJS.assert('util' === array.utilName, 'config_paths_relative: relative to module ID, not URL');
    amdJS.done();
  });
