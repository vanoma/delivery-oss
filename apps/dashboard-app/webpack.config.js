require('dotenv').config();
const HtmlWebpackPlugin = require('html-webpack-plugin');
const MiniCssExtractPlugin = require('mini-css-extract-plugin');
const webpack = require('webpack');
const CopyPlugin = require('copy-webpack-plugin');
const WorkboxWebpackPlugin = require('workbox-webpack-plugin');
const path = require('path');
const MomentLocalesPlugin = require('moment-locales-webpack-plugin');
// const SentryCliPlugin = require('@sentry/webpack-plugin');

module.exports = {
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
    devtool:
        process.env.NODE_ENV === 'production'
            ? 'source-map'
            : 'eval-cheap-module-source-map',
    plugins: [
        new HtmlWebpackPlugin({
            template: './public/index.html',
        }),
        new MiniCssExtractPlugin(),
        new webpack.DefinePlugin({
            'process.env.API_URL': JSON.stringify(process.env.API_URL),
            'process.env.WEB_APP_URL': JSON.stringify(process.env.WEB_APP_URL),
            'process.env.SENTRY_DSN': JSON.stringify(process.env.SENTRY_DSN),
            'process.env.SENTRY_ENVIRONMENT': JSON.stringify(
                process.env.SENTRY_ENVIRONMENT
            ),
            'process.env.GOOGLE_API_KEY': JSON.stringify(
                process.env.GOOGLE_API_KEY
            ),
            'process.env.ONESIGNAL_APP_ID': JSON.stringify(
                process.env.ONESIGNAL_APP_ID
            ),
        }),
        new webpack.IgnorePlugin({ resourceRegExp: /^\.\/OneSignalSDK$/ }),
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
        new WorkboxWebpackPlugin.InjectManifest({
            swSrc: './src/service-worker.ts',
            dontCacheBustURLsMatching: /\.[0-9a-f]{8}\./,
            exclude: [/\.map$/, /asset-manifest\.json$/, /LICENSE/],
            // Bump up the default maximum size (2mb) that's precached,
            // to make lazy-loading failure scenarios less likely.
            // See https://github.com/cra-template/pwa/issues/13#issuecomment-722667270
            maximumFileSizeToCacheInBytes: 15 * 1024 * 1024,
        }),
        new MomentLocalesPlugin(), // Strip out all locales except “en” - https://momentjs.com/docs/#/use-it/webpack/
        // new SentryCliPlugin({
        //     include: '.',
        //     ignore: ['node_modules', 'webpack.config.js'],
        //     org: 'vanoma',
        //     project: 'dashboard-app',
        //     authToken:
        //         'c5c2f7d559d7411ebbb89a48815d5526aafecf22072f4761bf0a12b77ab54f67',
        // }),
    ],
    devServer: {
        historyApiFallback: true,
    },
};
