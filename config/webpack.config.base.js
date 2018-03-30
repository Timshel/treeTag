const helpers               = require('./helpers')
const LimitChunkCountPlugin = require('webpack/lib/optimize/LimitChunkCountPlugin')
const NamedModulesPlugin    = require('webpack/lib/NamedModulesPlugin')
const CopyWebpackPlugin     = require('copy-webpack-plugin')

let config = {
  entry: {
    'main': helpers.root('/assets/main.ts')
  },
  output: {
    path: helpers.root('/public'),
    filename: 'js/[name].js',
    publicPath: '/'
  },
  devtool: 'source-map',
  resolve: {
    extensions: ['.ts', '.js', '.html'],
    alias: {
      'vue$': 'vue/dist/vue.esm.js'
    }
  },
  module: {
    rules: [{
      test: /\.ts$/,
      exclude: /node_modules/,
      enforce: 'pre',
      loader: 'tslint-loader'
    },
    {
      test: /\.ts$/,
      exclude: /node_modules/,
      loader: 'awesome-typescript-loader'
    },
    {
      test: /\.html$/,
      loader: 'raw-loader',
      exclude: ['./assets/index.html']
    }
    ]
  },
  plugins: [
    new LimitChunkCountPlugin({
      maxChunks: 1,
    }),
    new NamedModulesPlugin(),
    new CopyWebpackPlugin([{
      from: 'assets/assets',
      to: '.'
    } ])
  ]
}

module.exports = config
