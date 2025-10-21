package org.dsqrwym.shared.navigation.menu

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.ModeNight
import androidx.compose.material3.*
import androidx.compose.material3.TooltipDefaults.rememberTooltipPositionProvider
import androidx.compose.runtime.*
import org.dsqrwym.shared.LocalIsDarkTheme
import org.dsqrwym.shared.data.local.SharedUserPreferences
import org.dsqrwym.shared.drawable.SharedIcons
import org.dsqrwym.shared.drawable.sharedicons.Language
import org.dsqrwym.shared.localization.LanguageManager
import org.dsqrwym.shared.ui.components.buttons.LanguageMenuItem
import org.jetbrains.compose.resources.stringResource
import plataformagestio_ndistribucio_nmayorista.shared.generated.resources.SharedRes
import plataformagestio_ndistribucio_nmayorista.shared.generated.resources.change_to_dark_mode
import plataformagestio_ndistribucio_nmayorista.shared.generated.resources.change_to_light_mode
import plataformagestio_ndistribucio_nmayorista.shared.generated.resources.icon_content_description_language

/**
 * 定义通用的可组合菜单行为按钮
 */
@OptIn(ExperimentalMaterial3Api::class)
open class SharedMenuActions(
    val content: @Composable (toolTipAnchorPosition: TooltipAnchorPosition) -> Unit
) {
    object LanguageSwitcherIconButton : SharedMenuActions(
        content = { toolTipAnchorPosition ->
            var expanded by remember { mutableStateOf(false) }
            val supportedLanguages by remember { mutableStateOf(LanguageManager.SupportedLanguages.entries) }
            val onClick: () -> Unit = { expanded = !expanded }
            TooltipBox(
                positionProvider = rememberTooltipPositionProvider(toolTipAnchorPosition),
                tooltip = {
                    PlainTooltip {
                        Text(text = LanguageManager.getCurrent().displayName)
                    }
                },
                state = TooltipState()
            ) {
                IconButton(onClick = onClick) {
                    Icon(
                        imageVector = SharedIcons.Language,
                        contentDescription = stringResource(SharedRes.string.icon_content_description_language),
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
                                LanguageManager.setLocaleLanguage(item.code)
                                SharedUserPreferences.setUserLanguage(item.code)
                            })
                        }
                    }
                }
            }
        }
    )

    object ThemeChangeIconButton : SharedMenuActions(
        content = { toolTipAnchorPosition ->
            val isDarkTheme = LocalIsDarkTheme.current
            val onClick: () -> Unit = { SharedUserPreferences.setIsDarkTheme(!isDarkTheme) }
            TooltipBox(
                positionProvider = rememberTooltipPositionProvider(toolTipAnchorPosition),
                tooltip = {
                    PlainTooltip {
                        Text(
                            text = if (isDarkTheme) stringResource(SharedRes.string.change_to_light_mode)
                            else stringResource(
                                SharedRes.string.change_to_dark_mode
                            )
                        )
                    }
                },
                state = TooltipState()
            ) {
                IconButton(onClick = onClick) {
                    Icon(
                        imageVector = if (isDarkTheme) Icons.Outlined.LightMode else Icons.Outlined.ModeNight,
                        contentDescription = if (isDarkTheme) stringResource(SharedRes.string.change_to_light_mode)
                        else stringResource(
                            SharedRes.string.change_to_dark_mode
                        ),
                    )
                }
            }
        }
    )
}
