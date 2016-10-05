var fs = require('fs');
var path = require('path');

module.exports = {

  entry: path.resolve(__dirname, 'main.js'),

  output: {
    filename: 'server.js'
  },

  module: {
    loaders: [
      {test: /\.js$/, exclude: /node_modules/, loader: 'babel-loader?presets[]=es2015&presets[]=react'}
    ]
  }
};

