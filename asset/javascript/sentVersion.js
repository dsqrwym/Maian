window.addEventListener("load", function () {
    const meta = document.querySelector('meta[name="version"]');
    if (meta && meta.content) {
        window.parent.postMessage(
            { type: "version", version: meta.content },
            "*"
        );
        console.log("Version sent to parent:", meta.content);
    }else{
        console.warn("Version meta tag not found or empty.");
    }
});