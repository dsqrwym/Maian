window.addEventListener("message", function (event) {
    if (event.data.type !== "getVersion") return;

    console.log(`Received version request from origin: ${event.origin}`);

    const maxRetries = 6;
    const retryDelay = 500; // 毫秒

    let attempt = 0;

    function trySendVersion() {
        const meta = document.querySelector('meta[name="version"]');

        if (meta && meta.content) {
            const versionMessage = {
                type: "version",
                version: meta.content
            };
            // 若 event.source 存在
            if (event.source && typeof event.source.postMessage === "function") {
                event.source.postMessage(versionMessage,event.origin || "*");
                console.log(`Version sent to ${event.source} : ${meta.content}`);
            } else {
                window.postMessage(
                    versionMessage,
                    event.origin || "*"
                );
                console.log(`Version sent to window : ${meta.content}`);
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