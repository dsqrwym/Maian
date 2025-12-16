package org.dsqrwym.shared.navigation.menu

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.ModeNight
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.material3.TooltipDefaults.rememberTooltipPositionProvider
import androidx.compose.runtime.*
import androidx.compose.foundation.text.selection.SelectionContainer
import maian.shared.generated.resources.*
import org.dsqrwym.shared.data.local.SharedUserPreferences
import org.dsqrwym.shared.drawable.SharedIcons
import org.dsqrwym.shared.drawable.sharedicons.Language
import org.dsqrwym.shared.localization.LanguageManager
import org.dsqrwym.shared.ui.components.buttons.LanguageMenuItem
import org.jetbrains.compose.resources.stringResource

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
                        SelectionContainer {
                            Text(text = LanguageManager.getCurrent().displayName)
                        }
                    }
                },
                state = rememberTooltipState()
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
            val defaultTheme = remember { SharedUserPreferences.getIsDarkTheme() }
            val isDarkTheme by SharedUserPreferences.isDarkThemeFlow.collectAsState(defaultTheme)
            val currentThemeDescription = when (isDarkTheme) {
                true -> stringResource(SharedRes.string.theme_dark_mode)
                false -> stringResource(SharedRes.string.theme_light_mode)
                null -> stringResource(SharedRes.string.theme_follow_system)
            }
            val currentThemeIcon = when (isDarkTheme) {
                true -> Icons.Outlined.ModeNight
                false -> Icons.Outlined.LightMode
                null -> Icons.Outlined.Settings
            }
            var expanded by remember { mutableStateOf(false) }
            val onClick: () -> Unit = { expanded = !expanded }
            TooltipBox(
                positionProvider = rememberTooltipPositionProvider(toolTipAnchorPosition),
                tooltip = {
                    PlainTooltip {
                        SelectionContainer {
                            Text(currentThemeDescription)
                        }
                    }
                },
                state = rememberTooltipState()
            ) {
                IconButton(onClick = onClick) {
                    Icon(
                        imageVector = currentThemeIcon,
                        contentDescription = currentThemeDescription
                    )
                }

                // 下拉菜单：跟随系统 / 浅色模式 / 深色模式
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    if (isDarkTheme != null) {
                        DropdownMenuItem(
                            text = { Text(stringResource(SharedRes.string.theme_follow_system)) },
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.Settings,
                                    contentDescription = stringResource(SharedRes.string.theme_follow_system)
                                )
                            },
                            onClick = {
                                if (isDarkTheme != null) {
                                    SharedUserPreferences.setIsDarkTheme(null)
                                }
                                expanded = false
                            }
                        )
                    }

                    if (isDarkTheme != false) {
                        DropdownMenuItem(
                            text = { Text(stringResource(SharedRes.string.theme_light_mode)) },
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.LightMode,
                                    contentDescription = stringResource(SharedRes.string.theme_light_mode)
                                )
                            },
                            onClick = {
                                if (isDarkTheme != false) {
                                    SharedUserPreferences.setIsDarkTheme(false)
                                }
                                expanded = false
                            }
                        )
                    }

                    if (isDarkTheme != true) {
                        DropdownMenuItem(
                            text = { Text(stringResource(SharedRes.string.theme_dark_mode)) },
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.ModeNight,
                                    contentDescription = stringResource(SharedRes.string.theme_dark_mode)
                                )
                            },
                            onClick = {
                                if (isDarkTheme != true) {
                                    SharedUserPreferences.setIsDarkTheme(true)
                                }
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    )
}
