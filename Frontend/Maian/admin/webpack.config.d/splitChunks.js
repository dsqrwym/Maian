// webpack.config.d/splitChunks.js
//
// 说明：这个文件会在 Kotlin Multiplatform 的 webpack 配置生成后自动执行，
// 你可以在这里修改或增强配置。
// 目标：启用代码分割（splitChunks）以减少主 bundle 的体积，并提取公共依赖。

console.log("[webpack.config.d] 启用 splitChunks 优化配置");

config.optimization = config.optimization || {};

// 启用代码分割
config.optimization.splitChunks = {
    chunks: 'all', // 对同步与异步模块都启用
    minSize: 20 * 1024, // 最小分割大小 (20KB)
    maxSize: 500 * 1024, // 最大分割大小 (500KB)，超过则拆分
    cacheGroups: {
        // 提取第三方依赖为 vendors.js
        vendor: {
            test: /[\\/]node_modules[\\/]/,
            name: 'vendors',
            chunks: 'all',
            priority: -10,
            reuseExistingChunk: true,
        },
        // 提取公共代码为 common.js
        common: {
            name: 'common',
            minChunks: 2,
            chunks: 'all',
            priority: -20,
            reuseExistingChunk: true,
        },
    },
};

// 让 webpack 生成独立的 runtime 文件（避免缓存问题）
config.optimization.runtimeChunk = {
    name: (entrypoint) => `runtime~${entrypoint.name}`,
};

// （可选）调试时输出文件名更直观
config.output = config.output || {};
config.output.filename = '[name].[contenthash].js';

console.log("[webpack.config.d] splitChunks 配置已生效");
