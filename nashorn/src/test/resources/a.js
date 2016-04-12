print('a starting');
exports.done = false;
var b = require('./b.js');
print('in a, b.done = ' + b.done);
exports.done = true;
print('a done');
exports.hello = function () {
  print('hello!');
};
