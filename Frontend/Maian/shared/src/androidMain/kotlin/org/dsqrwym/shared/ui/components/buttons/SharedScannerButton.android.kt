package org.dsqrwym.shared.ui.components.buttons

import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import com.journeyapps.barcodescanner.CaptureActivity
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import org.dsqrwym.shared.drawable.SharedIcons
import org.dsqrwym.shared.drawable.sharedicons.BarcodeScanner
import org.dsqrwym.shared.util.validation.sanitizeProductCode


class AutoTorchCaptureActivity : CaptureActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val barcodeView = findViewById<DecoratedBarcodeView>(
            com.google.zxing.client.android.R.id.zxing_barcode_scanner
        )
        // 自动感光
        barcodeView.barcodeView.cameraSettings.isAutoTorchEnabled = true
    }
}

@Composable
actual fun SharedScannerButton(onResult: (String) -> Unit) {
    // 注册
    val launcher = rememberLauncherForActivityResult(ScanContract()) { result ->
        result?.contents?.let { scanned ->
            onResult(sanitizeProductCode(scanned))
        }
    }
    IconButton(onClick = {
        val options = ScanOptions().apply {
            setDesiredBarcodeFormats(ScanOptions.ALL_CODE_TYPES)
            setCameraId(0)
            setBeepEnabled(true)
            setOrientationLocked(true)
            setCaptureActivity(AutoTorchCaptureActivity::class.java)
        }
        launcher.launch(options)
    }) {
        Icon(
            SharedIcons.BarcodeScanner,
            contentDescription = SharedIcons.BarcodeScanner.name
        )
    }
}