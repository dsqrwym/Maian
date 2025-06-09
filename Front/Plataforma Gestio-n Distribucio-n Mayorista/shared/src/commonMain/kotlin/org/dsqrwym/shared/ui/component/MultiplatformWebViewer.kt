package org.dsqrwym.shared.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun MultiplatformWebView(modifier: Modifier = Modifier, url: String, onDataExtract: (version: String) -> Unit)