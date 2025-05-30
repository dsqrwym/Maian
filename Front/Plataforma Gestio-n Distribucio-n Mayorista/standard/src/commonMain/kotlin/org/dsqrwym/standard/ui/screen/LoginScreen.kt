package org.dsqrwym.standard.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.dsqrwym.shared.drawable.Visibility
import org.dsqrwym.shared.drawable.VisibilityOff
import org.dsqrwym.shared.drawable.getImageMobileBackground
import org.dsqrwym.shared.language.SharedLanguageMap
import org.dsqrwym.shared.ui.component.BackgroundImage
import org.dsqrwym.shared.ui.component.SharedHorizontalDivider
import org.dsqrwym.shared.ui.component.SharedTextButton

@Composable
fun LoginScreen(onBackButtonClick: () -> Unit = {}, onForgetPasswordClick: () -> Unit = {}) {
    var usernameOrEmail by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    BoxWithConstraints {
        val notMobile = maxWidth > 600.dp
        val blurRadius = if (notMobile) 30.dp else 0.dp
        BackgroundImage(getImageMobileBackground(), blurRadius) {
            // 居中内容，宽度限制仅非手机端
            val transparency = 0.85f
            val contentModifier = if (notMobile) {
                Modifier
                    .widthIn(max = 600.dp)
                    .heightIn(max = 700.dp)
                    .graphicsLayer { // 加alpha保证不会和shadow一样出现边缘更透的情况
                        shadowElevation = 20.dp.toPx()
                        shape = RoundedCornerShape(16.dp)
                        clip = true
                        alpha = transparency // 保证不会边缘更透的情况
                    }
                    .background(MaterialTheme.colorScheme.background.copy(alpha = transparency))
                    .align(Alignment.Center)
            } else {
                Modifier.fillMaxSize()
            }

            // 主体内容
            Column(
                modifier = contentModifier
                    .padding(26.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    IconButton(
                        onClick = onBackButtonClick
                    ) {
                        Icon(
                            Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                            SharedLanguageMap.currentStrings.value.login_button_back_button_content_description,
                            modifier = Modifier.fillMaxSize().scale(1.3f),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
                // 标题
                Text(
                    text = SharedLanguageMap.currentStrings.value.login_title, // 登录
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 39.sp,
                    fontWeight = FontWeight.W800,
                    modifier = Modifier.align(Alignment.Start).padding(vertical = 26.dp)
                )
                // 副标题
                Text(
                    text = SharedLanguageMap.currentStrings.value.login_subtitle, // 请先登录以继续使用
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.weight(0.68f))

                OutlinedTextField(
                    label = { Text(SharedLanguageMap.currentStrings.value.login_field_username_or_email_label /*用户名或者邮箱*/, color = MaterialTheme.colorScheme.onBackground) },
                    placeholder = { Text(SharedLanguageMap.currentStrings.value.login_field_username_or_email_placeholder /*"请输入用户名或者邮箱"*/, color = MaterialTheme.colorScheme.surfaceVariant) },
                    value = usernameOrEmail,
                    onValueChange = { usernameOrEmail = it },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.padding(vertical = 13.dp))

                // 密码输入框（可显示/隐藏密码）
                OutlinedTextField(
                    label = { Text(SharedLanguageMap.currentStrings.value.login_field_password_label/*"密码"*/, color = MaterialTheme.colorScheme.onBackground) },
                    placeholder = { Text(SharedLanguageMap.currentStrings.value.login_field_password_placeholder/*"请输入密码"*/, color = MaterialTheme.colorScheme.surfaceVariant) },
                    value = password,
                    onValueChange = { password = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if(passwordVisible) Visibility else VisibilityOff,
                                contentDescription = SharedLanguageMap.currentStrings.value.login_password_toggle_visibility/*"切换密码可见性"*/,
                                tint = if (passwordVisible) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.padding(vertical = 13.dp))

                // 忘记密码
                SharedTextButton(Modifier.align(Alignment.End),SharedLanguageMap.currentStrings.value.login_button_forget_password/*"忘记密码"*/) {
                    onForgetPasswordClick()
                }

                Spacer(modifier = Modifier.padding(vertical = 10.dp))
                // 登录按钮
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    onClick = {}
                ) {
                    Text(
                        text = SharedLanguageMap.currentStrings.value.login_button_login/*"登录"*/,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 18.5.sp
                    )
                }

                //分割线， 其他登录方式
                SharedHorizontalDivider(SharedLanguageMap.currentStrings.value.login_button_other_login_methods/*"其他登录方式"*/)

                Spacer(modifier = Modifier.weight(0.32f))

                //底部留白
                Spacer(modifier = Modifier.padding(vertical = 10.dp))
            }
        }
    }
}
