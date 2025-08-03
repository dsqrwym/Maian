package org.dsqrwym.standard.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.dsqrwym.shared.data.local.UserPreferences
import org.dsqrwym.shared.drawable.SharedIcons
import org.dsqrwym.shared.language.SharedLanguageMap
import org.dsqrwym.shared.ui.animations.containers.SharedFloatingBreathingBox
import org.dsqrwym.shared.ui.components.AgreementSection
import org.dsqrwym.shared.ui.components.buttons.SharedLoginButton
import org.dsqrwym.shared.ui.components.buttons.SharedTextButton
import org.dsqrwym.shared.ui.components.graphics.SharedAnimatedImgVector

@Composable
fun InitialScreen(
    dev: Boolean = false,
    onUserAgreementClick: () -> Unit = {},
    onPrivacyPolicyClick: () -> Unit = {},
    onLoginClick: () -> Unit = {}
) {
    var isNavEnabled by remember { mutableStateOf(if (dev) false else UserPreferences.isUserAgreed()) }

    Column(
        Modifier
            .fillMaxSize()
            .padding(26.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // 标题
        InitialTitle()
        // 动画层
        SharedFloatingBreathingBox (
            modifier = Modifier.weight(1f),
            scaleRange = Pair(0.98f, 1f),
            alphaRange = Pair(0.6f, 0.9f),
        ) {
            SharedAnimatedImgVector(
                imageVector = SharedIcons.MaianLogo,
                modifier = Modifier
                    .fillMaxSize(0.8f)
            )
        }
        // 导航交互
        SharedLoginButton(
            loginEnabled = isNavEnabled,
            modifier = Modifier
                .fillMaxWidth(0.78f)
                .padding(vertical = 3.dp),
            onLoginClick = onLoginClick
        )

        Text(
            text = SharedLanguageMap.currentStrings.value.initial_screen_quick_login_hint,/*"⏫ 支持谷歌、微信 快速登录 ⏫"*/
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
        )

        Spacer(modifier = Modifier.padding(vertical = 2.dp))

        SharedTextButton(
            modifier = Modifier.fillMaxWidth(0.56f),
            text = SharedLanguageMap.currentStrings.value.login_button_register_new_account, /*"注册新账户"*/
            isEnabled = isNavEnabled,
        ) {}

        AgreementSection(
            isAgreed = isNavEnabled,
            onAgreementChange = {
                isNavEnabled = !isNavEnabled
                UserPreferences.setUserAgreed(isNavEnabled)
            },
            onUserAgreementClick = { onUserAgreementClick() },
            onPrivacyPolicyClick = { onPrivacyPolicyClick() },
        )
    }
}

@Composable
fun InitialTitle() {
    val textAlign = TextAlign.Center
    val fontSize = 39.sp
    val fontWeight = FontWeight.W800
    val color = MaterialTheme.colorScheme.onBackground
    Column { // 加一层Colum免得里面的组件被外部影响布局，而colum可以被影响
        FlowRow(modifier = Modifier.padding(vertical = 16.dp)) {
            Text(
                text = SharedLanguageMap.currentStrings.value.initial_screen_welcome /*"欢迎来到"*/,
                textAlign = textAlign,
                color = color,
                fontSize = fontSize,
                fontWeight = fontWeight,
                lineHeight = fontSize
            )
            Spacer(modifier = Modifier.padding(horizontal = 5.dp))
            Text(
                text = SharedLanguageMap.currentStrings.value.initial_screen_platform_name /*"PGDM平台"*/,
                textAlign = textAlign,
                color = color,
                fontSize = fontSize,
                fontWeight = fontWeight,
                lineHeight = fontSize
            )
        }
        Text(
            text = SharedLanguageMap.currentStrings.value.initial_screen_instruction /*"请选择登录或注册以继续"*/,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}