package org.dsqrwym.shared.ui.components.buttons

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