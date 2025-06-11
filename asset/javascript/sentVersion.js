document.addEventListener('DOMContentLoaded', function () {
    const meta = document.querySelector('meta[name="version"]');
    if (meta && meta.content) {
        // 直接通过 kmpJsBridge 调用 Kotlin Native 方法
        if (window.kmpJsBridge) {
            window.kmpJsBridge.callNative('versionMessage', meta.content);
            console.log("Version sent via kmpJsBridge:", meta.content);
        } else {
            console.warn("kmpJsBridge not found. Cannot send version.");
        }
    } else {
        console.warn("Version meta tag not found or empty.");
    }
});