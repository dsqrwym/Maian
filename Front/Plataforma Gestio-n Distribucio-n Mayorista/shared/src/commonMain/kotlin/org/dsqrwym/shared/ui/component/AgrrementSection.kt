import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import org.dsqrwym.shared.language.SharedLanguageMap
import org.dsqrwym.shared.util.formatter.stringFormat

@Composable
fun AgreementSection(
    isAgreed: Boolean,
    onAgreementChange: (Boolean) -> Unit,
    onUserAgreementClick: () -> Unit,
    onPrivacyPolicyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.padding(8.dp)
    ) {
        Checkbox(
            checked = isAgreed,
            onCheckedChange = onAgreementChange
        )

        val userAgreementText = SharedLanguageMap.currentStrings.value.initial_screen_agreement_section_user_agreement // "用户协议"
        val privacyPolicyText = SharedLanguageMap.currentStrings.value.initial_screen_agreement_section_privacy_policy // "隐私政策"
        val fullAgreementText = stringFormat(SharedLanguageMap.currentStrings.value.initial_screen_agreement_section_agreement_text_template /*"我已阅读并同意《%s》和《%s》"*/, userAgreementText, privacyPolicyText)

        val primaryColor = MaterialTheme.colorScheme.primary
        val underlineStyle = SpanStyle(color = primaryColor, textDecoration = TextDecoration.Underline)

        val annotatedString = remember(fullAgreementText, userAgreementText, privacyPolicyText) { // remember的key也需要包含这些动态字符串
            buildAnnotatedString {
                // fullAgreementText 已经包含了所有文本，只需要找到链接的开始和结束位置
                append(fullAgreementText)

                // 找到用户协议文本的开始和结束索引
                val userAgreementStart = fullAgreementText.indexOf(userAgreementText)
                if (userAgreementStart != -1) { // 确保找到文本
                    val userAgreementEnd = userAgreementStart + userAgreementText.length
                    addStyle(
                        style = underlineStyle,
                        start = userAgreementStart,
                        end = userAgreementEnd
                    )
                    addStringAnnotation(
                        tag = "USER_AGREEMENT",
                        annotation = "user_agreement_route",
                        start = userAgreementStart,
                        end = userAgreementEnd
                    )
                }

                // 找到隐私政策文本的开始和结束索引
                val privacyPolicyStart = fullAgreementText.indexOf(privacyPolicyText)
                if (privacyPolicyStart != -1) { // 确保找到文本
                    val privacyPolicyEnd = privacyPolicyStart + privacyPolicyText.length
                    addStyle(
                        style = underlineStyle,
                        start = privacyPolicyStart,
                        end = privacyPolicyEnd
                    )
                    addStringAnnotation(
                        tag = "PRIVACY_POLICY",
                        annotation = "privacy_policy_route",
                        start = privacyPolicyStart,
                        end = privacyPolicyEnd
                    )
                }
            }
        }

        Text(
            text = annotatedString,
            modifier = Modifier
                .pointerInput(Unit) { // Using pointerInput for more robust click detection on specific text parts
                    detectTapGestures { offset ->
                        annotatedString.getStringAnnotations(tag = "USER_AGREEMENT", start = offset.x.toInt(), end = offset.x.toInt())
                            .firstOrNull()?.let {
                                onUserAgreementClick()
                            }
                        annotatedString.getStringAnnotations(tag = "PRIVACY_POLICY", start = offset.x.toInt(), end = offset.x.toInt())
                            .firstOrNull()?.let {
                                onPrivacyPolicyClick()
                            }
                    }
                },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}