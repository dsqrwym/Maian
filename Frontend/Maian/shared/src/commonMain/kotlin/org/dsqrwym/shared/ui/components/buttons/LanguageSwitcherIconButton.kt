package org.dsqrwym.shared.ui.components.buttons

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.icon_content_description_language
import kotlinx.coroutines.launch
import org.dsqrwym.shared.data.user.SharedUserSettingsRepository
import org.dsqrwym.shared.localization.LanguageManager
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

/**
 * A button component that allows users to switch between supported languages.
 * 允许用户在支持的语言之间切换的按钮组件。
 *
 * @param modifier The modifier to be applied to the button layout.
 *                 应用于按钮布局的修饰符。
 * @param padding The padding around the button content.
 *                按钮内容周围的边距。
 */
@Composable
fun LanguageSwitcherIconButton(modifier: Modifier = Modifier, padding: Dp = 6.dp) {
    var expanded by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val userSettingsRepository = koinInject<SharedUserSettingsRepository>()
    val supportedLanguages = remember { LanguageManager.SupportedLanguages.entries }

    Row(
        modifier = modifier.padding(padding),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        /**
         * The Text button to toggle the language switcher dropdown menu.
         * 切换语言切换器下拉菜单的文本按钮。
         */
        TextButton(modifier = Modifier.animateContentSize(), onClick = { expanded = true }) {
            /**
             * The text displaying the current language.
             * 显示当前语言的文本。
             */
            Text(text = LanguageManager.getCurrent().displayName, color = MaterialTheme.colorScheme.onBackground)

            Spacer(modifier = Modifier.padding(horizontal = 2.dp))

            Icon(
                imageVector = Icons.Outlined.Language,
                contentDescription = stringResource(SharedRes.string.icon_content_description_language),
                tint = MaterialTheme.colorScheme.onBackground
            )
        }

        /**
         * The dropdown menu containing language options.
         * 包含语言选项的下拉菜单。
         */
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            /**
             * Populate the dropdown menu with language options.
             * 用语言选项填充下拉菜单。
             */
            for (item in supportedLanguages) {
                if (item.code != LanguageManager.getCurrent().code) {
                    LanguageMenuItem(item, onClick = {
                        expanded = false
                        coroutineScope.launch {
                            userSettingsRepository.updateLanguage(item.code)
                        }
                    })
                }
            }
        }
    }
}

/**
 * A dropdown menu item that represents a language option in the language switcher.
 * 表示语言切换器中语言选项的下拉菜单项。
 *
 * @param item The language to display in the menu item.
 *             要在菜单项中显示的语言。
 * @param onClick Callback when this language is selected.
 *                选择此语言时的回调。
 */
@Composable
fun LanguageMenuItem(item: LanguageManager.SupportedLanguages, onClick: () -> Unit) {
    DropdownMenuItem(
        modifier = Modifier.animateContentSize(),
        onClick = onClick,
        text = { Text(item.displayName) }
    )
}
