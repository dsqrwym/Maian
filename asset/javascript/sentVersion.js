window.addEventListener("message", function (event) {
    if (event.data.type !== "getVersion") return;

    console.log(`Received version request from origin: ${event.origin}`);

    const maxRetries = 6;
    const retryDelay = 500; // 毫秒

    let attempt = 0;

    const origin = event.origin;
    const source = event.source; // 提前缓存，避免嵌套访问触发跨域异常

    function trySendVersion() {
        const meta = document.querySelector('meta[name="version"]');

        if (meta && meta.content) {
            const versionMessage = {
                type: "version",
                version: meta.content
            };

            try {
                if (source && typeof source.postMessage === "function") {
                    source.postMessage(versionMessage, origin || "*");
                    console.log(`Version sent via event.source to ${origin}`);
                } else {
                    window.postMessage(versionMessage, origin);
                    console.log(`Version sent via window.postMessage to ${origin}`);
                }
            } catch (e) {
                console.error("Failed to send version via postMessage:", e);
            }
        } else {
            attempt++;
            if (attempt <= maxRetries) {
                console.log(`Meta not found. Retrying (${attempt}/${maxRetries})...`);
                setTimeout(trySendVersion, retryDelay);
            } else {
                console.warn("Failed to get version meta tag after multiple attempts.");
            }
        }
    }

    trySendVersion();
});
