package org.dsqrwym.shared.ui.component.button

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.dsqrwym.shared.language.SharedLanguageMap

@Composable
fun SharedLoginButton(
    loginEnabled: Boolean = true,
    modifier: Modifier = Modifier.fillMaxWidth(),
    onLoginClick: () -> Unit) {
    Button(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        enabled = loginEnabled,
        onClick = onLoginClick
    ) {
        Text(
            text = SharedLanguageMap.currentStrings.value.login_button_login,
            color = MaterialTheme.colorScheme.onPrimary,
            fontSize = 18.5.sp
        )
    }
}