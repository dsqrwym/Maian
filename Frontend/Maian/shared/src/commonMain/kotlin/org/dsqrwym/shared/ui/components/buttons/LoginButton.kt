package org.dsqrwym.shared.ui.components.buttons

/**
 * A reusable login button component with loading and success states.
 * 可重用的登录按钮组件，带有加载和成功状态。
 *
 * @param loginEnabled Whether the button is enabled for interaction.
 *                     按钮是否可交互。
 * @param modifier The modifier to be applied to the button.
 *                 应用于按钮的修饰符。
 * @param loginUiState The current UI state of the login process.
 *                     登录过程的当前UI状态。
 * @param onLoginClick Callback when the button is clicked.
 *                     点击按钮时的回调。
 */

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.dsqrwym.shared.ui.components.containers.StateContent
import org.dsqrwym.shared.ui.components.containers.UiState
import org.jetbrains.compose.resources.stringResource
import plataformagestio_ndistribucio_nmayorista.shared.generated.resources.SharedRes
import plataformagestio_ndistribucio_nmayorista.shared.generated.resources.button_login

@Composable
fun LoginButton(
    loginEnabled: Boolean = true,
    modifier: Modifier = Modifier.animateContentSize(),
    loginUiState: UiState = UiState.Idle,
    onLoginClick: () -> Unit
) {
    FilledTonalButton(
        shape = RoundedCornerShape(18.dp),
        enabled = loginEnabled,
        onClick = onLoginClick
    ) {
        StateContent(
            state = loginUiState,
            successIconTint = MaterialTheme.colorScheme.onPrimary,
            modifier = if (loginUiState == UiState.Idle) modifier.fillMaxWidth() else modifier,
        ) {
            Text(
                text = stringResource(SharedRes.string.button_login), //SharedLanguageMap.currentStrings.value.login_button_login,
                textAlign = TextAlign.Center,
                fontSize = 18.5.sp
            )
        }
    }
}