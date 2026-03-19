package org.dsqrwym.shared.ui.components.buttons

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import org.dsqrwym.shared.drawable.SharedIcons
import org.dsqrwym.shared.drawable.sharedicons.BarcodeScanner
import org.dsqrwym.shared.util.validation.sanitizeProductCode
import org.ncgroup.kscan.BarcodeFormat
import org.ncgroup.kscan.BarcodeResult
import org.ncgroup.kscan.ScannerView

@Composable
actual fun SharedScannerButton(onResult: (String) -> Unit) {
    var showScanner by remember { mutableStateOf(false) }

    if (showScanner) {
        ScannerView(
            codeTypes = BarcodeFormat.entries,
            scannerUiOptions = null
        ) { result ->
            when (result) {
                is BarcodeResult.OnSuccess -> {
                    onResult(sanitizeProductCode(result.barcode.data))
                }

                is BarcodeResult.OnFailed -> {
                    showScanner = false
                }

                is BarcodeResult.OnCanceled -> {
                    showScanner = false
                }
            }

        }
    }

    IconButton(onClick = {
        showScanner = true
    }) {
        Icon(
            SharedIcons.BarcodeScanner,
            contentDescription = SharedIcons.BarcodeScanner.name
        )
    }
}