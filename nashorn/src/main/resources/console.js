(function (global) {

  var System = Java.type('java.lang.System');

  var counters = {};
  var timers = {};

  var RESET = '\u001B[0m';
  var BOLD = '\u001B[1m';
  var RED = '\u001B[31m';
  var GREEN = '\u001B[32m';
  var YELLOW = '\u001B[33m';
  var BLUE = '\u001B[34m';

  global['console'] = {
    'assert': function (expression, message) {
      if (!expression) {
        print.apply(global, [RED + message + RESET]);
      }
    },

    count: function (label) {
      var counter;

      if (label) {
        if (counters.hasOwnProperty(label)) {
          counter = counters[label];
        } else {
          counter = 0;
        }

        // update
        counters[label] = ++counter;
        print.apply(global, [GREEN + label + ':' + RESET, counter]);
      }
    },

    debug: function () {
      var args = Array.prototype.slice.call(arguments);
      if (args.length > 0) {
        args[0] = GREEN + args[0];
        args[args.length - 1] = args[args.length - 1] + RESET;
      }
      print.apply(global, args);
    },

    info: function () {
      var args = Array.prototype.slice.call(arguments);

      if (args.length > 0) {
        args[0] = BLUE + args[0];
        args[args.length - 1] = args[args.length - 1] + RESET;
      }
      print.apply(global, args);
    },

    log: function () {
      print.apply(global, arguments);
    },

    warn: function () {
      var args = Array.prototype.slice.call(arguments);

      if (args.length > 0) {
        args[0] = YELLOW + args[0];
        args[args.length - 1] = args[args.length - 1] + RESET;
      }
      print.apply(global, args);
    },

    error: function () {
      var args = Array.prototype.slice.call(arguments);

      if (args.length > 0) {
        args[0] = RED + args[0];
        args[args.length - 1] = args[args.length - 1] + RESET;
      }
      print.apply(global, args);
    },

    trace: function (e) {
      // isolate first the first line
      var idx = e.stack.indexOf('\n');

      var msg = BOLD + RED + e.stack.substr(0, idx) + RESET;
      var trace = e.stack.substr(idx);

      print.apply(global, [msg + trace]);
    },

    time: function (label) {
      if (label) {
        timers[label] = System.currentTimeMillis();
      }
    },
    timeEnd: function (label) {
      if (label) {
        var now = System.currentTimeMillis();
        if (timers.hasOwnProperty(label)) {
          print.apply(global, [GREEN + label + ':' + RESET, (now - timers[label]) + 'ms']);
          delete timers[label];
        } else {
          print.apply(global, [RED + label + ':' + RESET, '<no timer>']);
        }
      }
    }
  };
})(this);