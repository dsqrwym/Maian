package org.dsqrwym.shared.ui.components.buttons

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.dsqrwym.shared.ui.components.containers.SharedStateContent
import org.dsqrwym.shared.ui.components.containers.SharedUiState
import org.jetbrains.compose.resources.stringResource
import plataformagestio_ndistribucio_nmayorista.shared.generated.resources.SharedRes
import plataformagestio_ndistribucio_nmayorista.shared.generated.resources.login_button_login

@Composable
fun SharedLoginButton(
    loginEnabled: Boolean = true,
    modifier: Modifier = Modifier.animateContentSize(),
    loginUiState: SharedUiState = SharedUiState.Idle,
    onLoginClick: () -> Unit
) {
    Button(
        shape = RoundedCornerShape(18.dp),
        enabled = loginEnabled,
        onClick = onLoginClick
    ) {
        SharedStateContent(
            state = loginUiState,
            successIconTint = MaterialTheme.colorScheme.onPrimary,
            modifier = if (loginUiState == SharedUiState.Idle) modifier.fillMaxWidth() else modifier,
        ) {
            Text(
                text = stringResource(SharedRes.string.login_button_login), //SharedLanguageMap.currentStrings.value.login_button_login,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 18.5.sp
            )
        }
    }
}