var fs = require('fs');
var path = require('path');

module.exports = {

  entry: path.resolve(__dirname, 'src/main/resources/main.js'),

  output: {
    filename: 'src/main/resources/server.js'
  },

  module: {
    loaders: [
      {test: /\.js$/, exclude: /node_modules/, loader: 'babel-loader?presets[]=es2015'}
    ]
  },

  // externals: [
  //   'commonjs vertx-web-js/router'
  // ]
};

