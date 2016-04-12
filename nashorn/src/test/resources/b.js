print('b starting');
exports.done = false;
var a = require('./a.js');
print('in b, a.done = ' + a.done);
exports.done = true;
print('b done');
