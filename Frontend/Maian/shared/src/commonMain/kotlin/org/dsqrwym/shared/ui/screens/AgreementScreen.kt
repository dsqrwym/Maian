package org.dsqrwym.shared.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.dsqrwym.shared.localization.LanguageManager
import org.dsqrwym.shared.ui.components.MyWebView
import org.dsqrwym.shared.ui.components.topbar.AuthTopBar
import org.dsqrwym.shared.util.formatter.stringFormat

class Agreement {
    companion object {
        const val PRIVACY_POLICY_BASE_URL =
            "https://maian.dsqrwym.es/asset/Privacy-Policy/%s.html"
        const val USER_AGREEMENT_BASE_URL =
            "https://maian.dsqrwym.es/asset/Privacy-Policy/%s.html"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
        AuthTopBar(
            onBackButtonClick = onBackButtonClick,
            enableLanguageSwitcher = false
        )

        MyWebView(url) {
            getVersion(it)
        }
    }
}