package org.dsqrwym.shared.ui.screen

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
import org.dsqrwym.shared.language.SharedLanguageMap
import org.dsqrwym.shared.ui.component.SharedWebView
import org.dsqrwym.shared.util.formatter.stringFormat

class SharedAgreement {
    companion object {
        const val PRIVACY_POLICY_BASE_URL =
            "https://dsqrwym.github.io/Plataforma-Gestion-Distribucion-Mayorista/asset/Privacy-Policy/%s.html"
        const val USER_AGREEMENT_BASE_URL =
            "https://dsqrwym.github.io/Plataforma-Gestion-Distribucion-Mayorista/asset/User-Agreement/%s.html"
    }
}

@Composable
fun SharedAgreementScreen(
    modifier: Modifier = Modifier,
    baseUrl: String,
    getVersion: (String) -> Unit = {},
    onBackButtonClick: () -> Unit = {}
) {

    val language = SharedLanguageMap.getCurrentLanguage()
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
                    SharedLanguageMap.currentStrings.value.login_button_back_button_content_description,
                    modifier = Modifier.fillMaxSize().scale(1.3f),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        SharedWebView(url){
            getVersion(it)
        }
    }
}