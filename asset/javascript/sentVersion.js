document.addEventListener('DOMContentLoaded', function () {
    const maxAttempts = 60; // 最多尝试次数，避免死循环（比如总共30秒）
    let attempts = 0;

    const timer = setInterval(() => {
        console.log("Checking for version meta tag... Attempt:", attempts + 1);
        const meta = document.querySelector('meta[name="version"]');
        if (meta && meta.content) {
            console.log("Found version meta tag:", meta.content);
            if (window.kmpJsBridge) {
                window.kmpJsBridge.callNative('versionMessage', meta.content);
                console.log("Version sent via kmpJsBridge:", meta.content);
                clearInterval(timer); // 成功后停止检查
            } else {
                console.warn("kmpJsBridge not ready. Retrying...");
            }
        } else {
            console.warn("Version meta tag not found or empty. Retrying...");
        }

        if (++attempts >= maxAttempts) {
            clearInterval(timer);
            console.error("Failed to send version after max attempts.");
        }
    }, 500); // 每500ms检查一次
});
