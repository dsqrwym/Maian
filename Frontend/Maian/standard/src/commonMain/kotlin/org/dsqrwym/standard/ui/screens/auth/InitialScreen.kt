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
import org.dsqrwym.shared.ui.components.AgreementSection
import org.dsqrwym.shared.ui.components.buttons.LanguageSwitcherIconButton
import org.dsqrwym.shared.ui.components.buttons.LoginButton
import org.dsqrwym.shared.ui.components.buttons.MyTextButton
import org.dsqrwym.shared.ui.components.containers.FloatingBreathingBox
import org.dsqrwym.shared.ui.components.graphics.AnimatedImgVector
import org.jetbrains.compose.resources.stringResource
import plataformagestio_ndistribucio_nmayorista.shared.generated.resources.*

@Composable
fun InitialScreen(
    dev: Boolean = false,
    onUserAgreementClick: () -> Unit = {},
    onPrivacyPolicyClick: () -> Unit = {},
    onLoginClick: () -> Unit = {}
) {
    var isNavEnabled by remember { mutableStateOf(if (dev) false else UserPreferences.isUserAgreed()) }

    Box(modifier = Modifier.fillMaxSize()) {
        LanguageSwitcherIconButton(modifier = Modifier.align(Alignment.TopEnd))

        Column(
            Modifier.fillMaxSize().padding(26.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // 标题
            InitialTitle()
            // 动画层
            FloatingBreathingBox(
                modifier = Modifier.weight(1f),
                scaleRange = Pair(0.98f, 1f),
                alphaRange = Pair(0.6f, 0.9f),
            ) {
                AnimatedImgVector(
                    imageVector = SharedIcons.MaianLogo, modifier = Modifier.fillMaxSize(0.8f)
                )
            }
            // 导航交互
            LoginButton(
                loginEnabled = isNavEnabled,
                modifier = Modifier.fillMaxWidth(0.78f).padding(vertical = 3.dp),
                onLoginClick = onLoginClick
            )

            Text(
                text = stringResource(SharedRes.string.initial_screen_quick_login_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
            )

            Spacer(modifier = Modifier.padding(vertical = 2.dp))

            MyTextButton(
                modifier = Modifier.fillMaxWidth(0.56f),
                text = stringResource(SharedRes.string.button_register_new_account),
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
                text = stringResource(SharedRes.string.initial_screen_welcome),
                textAlign = textAlign,
                color = color,
                fontSize = fontSize,
                fontWeight = fontWeight,
                lineHeight = fontSize
            )
            Spacer(modifier = Modifier.padding(horizontal = 5.dp))
            Text(
                text = stringResource(SharedRes.string.initial_screen_platform_name),
                textAlign = textAlign,
                color = color,
                fontSize = fontSize,
                fontWeight = fontWeight,
                lineHeight = fontSize
            )
        }
        Text(
            text = stringResource(SharedRes.string.initial_screen_instruction),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}