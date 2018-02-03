const webpackConfig = require('./webpack.config.base')
const HtmlWebpackPlugin = require('html-webpack-plugin')
const DefinePlugin = require('webpack/lib/DefinePlugin')
const env = require('../environment/dev.env')
const path = require('path')

const ROOT = path.resolve(__dirname, '..')

function root (args) {
  args = Array.prototype.slice.call(arguments, 0)
  return path.join.apply(path, [ROOT].concat(args))
}


webpackConfig.module.rules = [...webpackConfig.module.rules,
  {
    test: /\.scss$/,
    use: [{
      loader: 'style-loader'
    },
    {
      loader: 'css-loader'
    },
    {
      loader: 'sass-loader'
    }
    ]
  },
  {
    test: /\.(jpg|png|gif|eot|svg|ttf|woff|woff2)$/,
    loader: 'file-loader'
  }
]

webpackConfig.plugins = [...webpackConfig.plugins,
  new HtmlWebpackPlugin({
    inject: true,
    template: helpers.root('/assets/index.html'),
    favicon: helpers.root('/assets/favicon.ico')
  }),
  new DefinePlugin({
    'process.env': env
  })
]

webpackConfig.devServer = {
  port: 8080,
  host: 'localhost',
  historyApiFallback: true,
  watchOptions: {
    aggregateTimeout: 300,
    poll: 1000
  },
  contentBase: './assets',
  open: true
}

module.exports = webpackConfig
