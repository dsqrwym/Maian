package org.dsqrwym.shared.ui.components.topbar

import androidx.compose.foundation.MarqueeSpacing
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.dsqrwym.shared.ui.components.buttons.LanguageSwitcherIconButton
import org.jetbrains.compose.resources.stringResource
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.button_back_button_content_description

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthTopBar(
    title: String? = null,
    titleMarqueeSpacing: MarqueeSpacing = MarqueeSpacing(2.dp),
    scrollBehavior: TopAppBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(),
    enableLanguageSwitcher: Boolean = true,
    onBackButtonClick: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = {
            title?.let {
                Text(
                    text = it,
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                        .basicMarquee(spacing = titleMarqueeSpacing, iterations = Int.MAX_VALUE),
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onBackButtonClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                    contentDescription = stringResource(SharedRes.string.button_back_button_content_description),
                    modifier = Modifier.fillMaxSize(),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        },
        actions = {
            if (enableLanguageSwitcher) {
                LanguageSwitcherIconButton()
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
        ),
        scrollBehavior = scrollBehavior
    )
}