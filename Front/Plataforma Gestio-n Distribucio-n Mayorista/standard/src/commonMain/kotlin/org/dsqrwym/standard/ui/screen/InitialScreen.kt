package org.dsqrwym.standard.ui.screen

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
import org.dsqrwym.shared.language.SharedLanguageMap
import org.dsqrwym.shared.ui.component.AgreementSection
import org.dsqrwym.shared.ui.component.button.SharedLoginButton
import org.dsqrwym.shared.ui.component.button.SharedTextButton
import org.dsqrwym.shared.ui.component.container.SharedSnackbarScaffold

@Composable
fun InitialScreen(
    showAgreementWarning: Boolean = false,
    dev: Boolean = false,
    onUserAgreementClick: () -> Unit = {},
    onPrivacyPolicyClick: () -> Unit = {},
    onLoginClick: () -> Unit = {}
) {
    var isNavEnabled by remember { mutableStateOf( if(dev) false else UserPreferences.isUserAgreed()) }
//    var isNavEnabled by remember { mutableStateOf(false) }
    var snackbarMessage: String? = null
    // 显示提示消息
    if (showAgreementWarning) {
        snackbarMessage = SharedLanguageMap.currentStrings.value.initial_screen_agreement_warning /*"请先同意用户协议才能继续"*/
    }

    SharedSnackbarScaffold(snackbarMessage = snackbarMessage, content = {
        Column(
            Modifier
                .fillMaxSize()
                .padding(26.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // 标题
            InitialTitle()
            // 动画层
            Spacer(Modifier.weight(1f))

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
    })
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