package org.dsqrwym.shared.ui.components.cards

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.dsqrwym.shared.drawable.SharedIcons
import org.dsqrwym.shared.drawable.sharedicons.InProgress
import org.dsqrwym.shared.theme.AppExtraColors
import org.dsqrwym.shared.ui.components.containers.UiState
import org.dsqrwym.shared.ui.components.graphics.AnimatedImgVector
import org.jetbrains.compose.resources.stringResource
import plataformagestio_ndistribucio_nmayorista.shared.generated.resources.SharedRes
import plataformagestio_ndistribucio_nmayorista.shared.generated.resources.status_completed_content_description
import plataformagestio_ndistribucio_nmayorista.shared.generated.resources.status_error_content_description
import plataformagestio_ndistribucio_nmayorista.shared.generated.resources.status_in_progress_content_description

@Composable
        /**
         * AuthStepCard
         *
         * EN: Card showing a step indicator for multistep auth flows (e.g., forgot password).
         * Displays current/total steps and an animated icon for Error/Loading/Success.
         *
         * ZH: 用于多步骤认证流程（如忘记密码）的步骤卡片。显示当前/总步数，并以动画图标表示
         * 错误/加载中/成功状态。
         */
fun AuthStepCard(
    step: Int,
    currentStep: Int,
    maxStep: Int,
    hasError: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val elevation by animateDpAsState(targetValue = if (currentStep == step) 8.dp else 0.dp)
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.outlinedCardElevation(elevation),
        border = CardDefaults.outlinedCardBorder(currentStep == step)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(10.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.End),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$step/$maxStep",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                )
                val commonDurationMillis = 380
                val commonDrawFillAfter = false
                val commonUseOriginalStrokeColor = false
                val commonModifier = Modifier.size(24.dp)

                val targetState = when {
                    hasError -> UiState.Error
                    currentStep == step -> UiState.Loading
                    currentStep > step -> UiState.Success
                    else -> null
                }
                targetState?.let {
                    when (it) {
                        UiState.Error -> {
                            AnimatedImgVector(
                                imageVector = Icons.Outlined.Info,
                                durationMillis = commonDurationMillis,
                                strokeWidth = 0.1.dp.value,
                                drawFillAfter = commonDrawFillAfter,
                                tint = MaterialTheme.colorScheme.error,
                                useOriginalStrokeColor = commonUseOriginalStrokeColor,
                                contentDescription = stringResource(SharedRes.string.status_error_content_description),
                                modifier = commonModifier
                            )
                        }

                        UiState.Success -> {
                            AnimatedImgVector(
                                imageVector = Icons.Outlined.CheckCircle,
                                durationMillis = commonDurationMillis,
                                drawFillAfter = commonDrawFillAfter,
                                tint = AppExtraColors.current.correct,
                                useOriginalStrokeColor = commonUseOriginalStrokeColor,
                                contentDescription = stringResource(SharedRes.string.status_completed_content_description),
                                modifier = commonModifier
                            )
                        }

                        UiState.Loading -> {
                            AnimatedImgVector(
                                imageVector = SharedIcons.InProgress,
                                durationMillis = commonDurationMillis,
                                drawFillAfter = commonDrawFillAfter,
                                tint = MaterialTheme.colorScheme.secondary,
                                strokeWidth = 0.3.dp.value,
                                useOriginalStrokeColor = commonUseOriginalStrokeColor,
                                contentDescription = stringResource(SharedRes.string.status_in_progress_content_description),
                                modifier = commonModifier
                            )
                        }

                        else -> {}
                    }
                }
            }
            Column(
                content = content
            )
        }
    }
}