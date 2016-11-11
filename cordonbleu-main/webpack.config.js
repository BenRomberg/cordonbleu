var webpack = require('webpack')
var vue = require('vue-loader')

module.exports = {
  entry : "./src/main/resources/webpack/entry.js",
  output : {
    path : __dirname + "/target/classes/static/js",
    filename : "bundle.js"
  },
  module : {
    loaders : [ {
      test : /\.vue$/,
      loader : 'vue'
    }, {
      test : /\.js$/,
      loader : 'babel',
      exclude : /node_modules/
    }, {
      test: /\.scss$/,
      loaders : ['style', 'css', 'sass']
    } ]
  },
  babel : {
    presets : [ 'es2015' ]
  }
};

if (process.argv.indexOf('-p') >= 0) {
  module.exports.plugins = [
    new webpack.optimize.UglifyJsPlugin({
      compress: {
        warnings: false
      }
    }),
    new webpack.optimize.OccurenceOrderPlugin()
  ]
}
