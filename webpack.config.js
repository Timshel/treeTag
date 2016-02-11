module.exports = {
  entry: "./assets/javascripts/main.js",
  output: {
      path: "./public/javascripts",
      filename: "main.js"
  },
  module: {
    loaders: [
      {
        test: /\.js$/,
        loader: 'jsx-loader'
      }
    ]
  }
};