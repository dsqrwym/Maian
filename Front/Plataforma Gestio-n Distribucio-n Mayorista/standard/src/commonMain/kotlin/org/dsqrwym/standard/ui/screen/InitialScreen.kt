package org.dsqrwym.standard.ui.screen

import AgreementSection
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
import org.dsqrwym.shared.ui.component.button.SharedLoginButton
import org.dsqrwym.shared.ui.component.button.SharedTextButton
import org.dsqrwym.shared.ui.component.container.SharedAuthContainer

@Composable
fun InitialScreen(onLoginClick: () -> Unit = {}) {
    var isNavEnabled by remember { mutableStateOf(UserPreferences.isUserAgreed()) }
    SharedAuthContainer {
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
                    .padding(vertical = 3.dp)
            ){
                onLoginClick()
            }

            SharedTextButton(
                text = "注册新账户",
                isEnabled = isNavEnabled,
            ){}

            AgreementSection(
                isAgreed = isNavEnabled,
                onAgreementChange = {
                    isNavEnabled = !isNavEnabled
                    UserPreferences.setUserAgreed(isNavEnabled)
                },
                onUserAgreementClick = {},
                onPrivacyPolicyClick = {}
            )
        }
    }
}
@Composable
fun InitialTitle() {
    Column { // 加一层Colum免得里面的组件被外部影响布局，而colum可以被影响
        FlowRow(modifier = Modifier.padding(vertical = 16.dp)) {
            Text(
                text = "欢迎来到",
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 39.sp,
                fontWeight = FontWeight.W800
            )
            Spacer(modifier = Modifier.padding(horizontal = 5.dp))
            Text(
                text = "PGDM平台",
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 39.sp,
                fontWeight = FontWeight.W800
            )
        }
        Text(
            text = "请选择登录或注册以继续",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}