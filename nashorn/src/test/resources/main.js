print('main starting');
var a = require('./a.js');
var b = require('./b.js');
print('in main, a.done=', a.done, ', b.done=', b.done);
a.hello();
