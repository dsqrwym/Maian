package org.dsqrwym.shared.ui.components.buttons

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import org.dsqrwym.shared.drawable.SharedIcons
import org.dsqrwym.shared.drawable.sharedicons.BarcodeScanner
import org.dsqrwym.shared.util.log.SharedLog

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("""
(onSuccess, onError) => {
    const lib = window.Html5QrcodeLibrary || window;
    const Html5Qrcode = lib.Html5Qrcode;
    if (!Html5Qrcode) {
        onError("Html5Qrcode library not loaded.");
        return;
    }
    
    if (window.__scannerInstance) return;

    const readerId = "reader";
    const readerElem = document.getElementById(readerId);
    readerElem.style.display = "block";
    readerElem.style.width = "500px"; // 这个大小决定了解析的大小
    
    // 使用透明度和位置将其“隐藏”
    readerElem.style.opacity = "0";
    readerElem.style.position = "fixed";
    readerElem.style.top = "0";
    readerElem.style.left = "0";
    readerElem.style.zIndex = "-1";

    const html5QrCode = new Html5Qrcode(readerId);
    window.__scannerInstance = html5QrCode;

    // 辅助函数：进入画中画
    const enterPiP = (video) => {
        if (video.readyState >= 1) { // HAVE_METADATA = 1
            video.requestPictureInPicture().catch(e => console.error("PiP Error:", e));
        } else {
            video.onloadedmetadata = () => {
                video.requestPictureInPicture().catch(e => console.error("PiP Error:", e));
            };
        }
    };

    html5QrCode.start(
        { facingMode: "environment" }, 
        { fps: 30 },
        (decodedText) => {
            onSuccess(decodedText);
            if (document.pictureInPictureElement) {
                document.exitPictureInPicture();
            }
            html5QrCode.stop().then(() => {
                window.__scannerInstance = null;
                readerElem.style.display = "none";
            });
        },
        (errorMessage) => {}
    ).then(() => {
        const video = readerElem.querySelector('video');
        if (video) {
            // 解决 Metadata 未加载的问题
            enterPiP(video);
            
            video.addEventListener('leavepictureinpicture', () => {
                if (window.__scannerInstance) {
                    window.__scannerInstance.stop().then(() => {
                        window.__scannerInstance = null;
                        readerElem.style.display = "none";
                    });
                }
            }, { once: true });
        }
    }).catch(err => {
        window.__scannerInstance = null;
        onError(err.toString());
    });
}
""")
external fun startJsScanner(
    onSuccess: (String) -> Unit,
    onError: (String) -> Unit
)

@Composable
actual fun SharedScannerButton(onResult: (String) -> Unit) {
    IconButton(onClick = {
        startJsScanner(onResult) {
            SharedLog.log("startJsScanner onResult: $it")
        }
    }) {
        Icon(
            SharedIcons.BarcodeScanner,
            contentDescription = SharedIcons.BarcodeScanner.name
        )
    }
}
