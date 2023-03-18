const prod = process.env.NODE_ENV === 'production';

const HtmlWebpackPlugin = require('html-webpack-plugin');
const MiniCssExtractPlugin = require('mini-css-extract-plugin');
const FaviconsWebpackPlugin = require('favicons-webpack-plugin');
const webpack = require('webpack');
const CopyPlugin = require('copy-webpack-plugin');
const path = require('path');

require('dotenv').config();

module.exports = {
    mode: prod ? 'production' : 'development',
    entry: './src/index.tsx',
    output: {
        path: `${__dirname}/dist/`,
        publicPath: '/',
    },
    module: {
        rules: [
            {
                test: /\.(ts|tsx)$/,
                exclude: /node_modules/,
                resolve: {
                    extensions: ['.ts', '.tsx', '.js', '.json'],
                },
                use: 'ts-loader',
            },
            {
                test: /\.css$/,
                use: [MiniCssExtractPlugin.loader, 'css-loader'],
            },
            {
                test: /\.(png|jpe?g|gif|svg|ico)$/i,
                type: 'asset/resource',
            },
        ],
    },
    devtool: prod ? 'source-map' : 'eval-cheap-module-source-map',
    plugins: [
        new HtmlWebpackPlugin({
            template: './public/index.html',
        }),
        new MiniCssExtractPlugin(),
        new FaviconsWebpackPlugin('./public/assets/favicon.png'),
        new webpack.DefinePlugin({
            'process.env.GOOGLE_API_KEY': JSON.stringify(
                process.env.GOOGLE_API_KEY
            ),
            'process.env.API_URL': JSON.stringify(process.env.API_URL),
            'process.env.ONESIGNAL_APP_ID': JSON.stringify(
                process.env.ONESIGNAL_APP_ID
            ),
            'process.env.EXTERNAL_USER_ID': JSON.stringify(
                process.env.EXTERNAL_USER_ID
            ),
        }),
        new CopyPlugin({
            patterns: [
                {
                    from: '**/*',
                    context: path.resolve(__dirname, 'public'),
                    globOptions: {
                        ignore: ['**/index.html', '**/assets/**'],
                    },
                },
            ],
        }),
    ],
    devServer: {
        historyApiFallback: true,
    },
};
