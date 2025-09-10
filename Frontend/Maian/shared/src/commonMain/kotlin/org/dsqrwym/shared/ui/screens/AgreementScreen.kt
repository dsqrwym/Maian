package org.dsqrwym.shared.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import org.dsqrwym.shared.localization.LanguageManager
import org.dsqrwym.shared.ui.components.MyWebView
import org.dsqrwym.shared.util.formatter.stringFormat
import org.jetbrains.compose.resources.stringResource
import plataformagestio_ndistribucio_nmayorista.shared.generated.resources.SharedRes
import plataformagestio_ndistribucio_nmayorista.shared.generated.resources.button_back_button_content_description

class Agreement {
    companion object {
        const val PRIVACY_POLICY_BASE_URL =
            "https://maian.dsqrwym.es/asset/Privacy-Policy/%s.html"
        const val USER_AGREEMENT_BASE_URL =
            "https://maian.dsqrwym.es/asset/Privacy-Policy/%s.html"
    }
}

@Composable
fun AgreementScreen(
    modifier: Modifier = Modifier,
    baseUrl: String,
    getVersion: (String) -> Unit = {},
    onBackButtonClick: () -> Unit = {}
) {

    val language = LanguageManager.getCurrentLanguage()
    val url = stringFormat(baseUrl, language)
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(26.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = onBackButtonClick) {
                Icon(
                    Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                    stringResource(SharedRes.string.button_back_button_content_description),
                    modifier = Modifier.fillMaxSize().scale(1.3f),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        MyWebView(url) {
            getVersion(it)
        }
    }
}