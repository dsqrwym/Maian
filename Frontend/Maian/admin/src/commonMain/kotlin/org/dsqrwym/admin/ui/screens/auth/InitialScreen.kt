package org.dsqrwym.admin.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.initial_screen_quick_login_hint
import org.dsqrwym.business.ui.components.auth.BusinessInitialTitle
import org.dsqrwym.shared.data.local.SharedUserPreferences
import org.dsqrwym.shared.drawable.SharedIcons
import org.dsqrwym.shared.ui.components.AgreementSection
import org.dsqrwym.shared.ui.components.buttons.LanguageSwitcherIconButton
import org.dsqrwym.shared.ui.components.buttons.LoginButton
import org.dsqrwym.shared.ui.components.containers.FloatingBreathingBox
import org.dsqrwym.shared.ui.components.graphics.AnimatedImgVector
import org.jetbrains.compose.resources.stringResource
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
fun InitialScreen(
    dev: Boolean = false,
    onUserAgreementClick: () -> Unit = {},
    onPrivacyPolicyClick: () -> Unit = {},
    onLoginClick: () -> Unit = {}
) {
    var isNavEnabled by remember { mutableStateOf(if (dev) false else SharedUserPreferences.isUserAgreed()) }
    Box(modifier = Modifier.fillMaxSize()) {
        LanguageSwitcherIconButton(modifier = Modifier.align(Alignment.TopEnd))

        Column(
            Modifier.fillMaxSize().padding(26.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // 标题
            BusinessInitialTitle()
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

            AgreementSection(
                isAgreed = isNavEnabled,
                onAgreementChange = {
                    isNavEnabled = !isNavEnabled
                    SharedUserPreferences.setUserAgreed(isNavEnabled)
                },
                onUserAgreementClick = { onUserAgreementClick() },
                onPrivacyPolicyClick = { onPrivacyPolicyClick() },
            )
        }
    }
}
