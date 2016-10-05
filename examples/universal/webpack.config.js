var webpack = require('webpack');

module.exports = {
  entry: './index.js',

  output: {
    path: 'webroot',
    filename: 'bundle.js'
    //publicPath: '/'
  },

  module: {
    loaders: [
      {test: /\.js$/, exclude: /node_modules/, loader: 'babel-loader?presets[]=es2015&presets[]=react'}
    ]
  }
};
