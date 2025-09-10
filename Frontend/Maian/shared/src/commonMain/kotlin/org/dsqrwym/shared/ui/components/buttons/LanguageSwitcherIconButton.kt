package org.dsqrwym.shared.ui.components.buttons

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.dsqrwym.shared.drawable.SharedIcons
import org.dsqrwym.shared.drawable.sharedicons.Language
import org.dsqrwym.shared.localization.LanguageManager

@Composable
fun LanguageSwitcherIconButton(modifier: Modifier = Modifier, padding: Dp = 6.dp) {
    var expanded by remember { mutableStateOf(false) }
    val supportedLanguages by remember { mutableStateOf(LanguageManager.SupportedLanguages.entries) }
    val onClick: () -> Unit = { expanded = !expanded }

    Row(
        modifier = modifier.padding(padding).clickable(onClick = onClick).animateContentSize(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = LanguageManager.getCurrent().displayName, color = MaterialTheme.colorScheme.onBackground)
        IconButton(onClick = onClick) {
            Icon(imageVector = SharedIcons.Language, contentDescription = "", tint = MaterialTheme.colorScheme.onBackground)
        }


        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            for (item in supportedLanguages) {
                if (item.code != LanguageManager.getCurrent().code) {
                    DropdownMenuItem(
                        onClick = { LanguageManager.setLocaleLanguage(item.code) },
                        text = { Text(item.displayName) })
                }
            }
        }
    }
}