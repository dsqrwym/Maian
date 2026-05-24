package org.dsqrwym.enterprise.ui.components.employees

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Numbers
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import maian.enterprise.generated.resources.EnterpriseRes
import maian.enterprise.generated.resources.employee_user_id
import maian.enterprise.generated.resources.resend_employee_activation_email_tooltip
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.edit
import maian.shared.generated.resources.email
import maian.shared.generated.resources.field_username_label
import maian.shared.generated.resources.not_set
import maian.shared.generated.resources.tax_id
import maian.shared.generated.resources.telephone
import org.dsqrwym.business.ui.components.button.BusinessOutlinedDeleteButton
import org.dsqrwym.business.ui.components.tooltip.PermissionTooltip
import org.dsqrwym.enterprise.data.employee.EmployeeStatus
import org.dsqrwym.enterprise.data.employee.displayName
import org.dsqrwym.enterprise.domain.employee.Employee
import org.dsqrwym.shared.util.modifier.placeholderWithShimmer
import org.jetbrains.compose.resources.stringResource

@Composable
fun EmployeeCard(
    employee: Employee,
    isLoading: Boolean,
    canManage: Boolean,
    noPermissionText: String,
    isResendingActivationEmail: Boolean,
    activationEmailResendCooldownSeconds: Int,
    onEdit: () -> Unit,
    onResendActivationEmail: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 230.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SelectionContainer(modifier = Modifier.weight(1f)) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            itemVerticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                modifier = Modifier.placeholderWithShimmer(isLoading),
                                text = employee.fullName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            EmployeeStatusChip(
                                status = employee.status,
                                modifier = Modifier.placeholderWithShimmer(isLoading),
                            )
                        }
                        /*
                        EmployeeInlineInfo(
                            icon = Icons.Outlined.Work,
                            text = employee.role.displayName(),
                            isLoading = isLoading,
                        )
                        */
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (employee.status == EmployeeStatus.PENDING_VERIFICATION) {
                        val resendActivationEmailBaseText = stringResource(
                            EnterpriseRes.string.resend_employee_activation_email_tooltip
                        )
                        val resendActivationEmailText =
                            if (activationEmailResendCooldownSeconds > 0) {
                                "$resendActivationEmailBaseText (${activationEmailResendCooldownSeconds}s)"
                            } else {
                                resendActivationEmailBaseText
                            }
                        if (canManage) {
                            EmployeeActionTooltip(
                                text = resendActivationEmailText,
                            ) {
                                if (activationEmailResendCooldownSeconds > 0) {
                                    TextButton(
                                        onClick = onResendActivationEmail,
                                        enabled = false,
                                    ) {
                                        Icon(
                                            modifier = Modifier.size(18.dp),
                                            imageVector = Icons.Outlined.Email,
                                            contentDescription = resendActivationEmailText,
                                        )
                                        Text(
                                            modifier = Modifier.padding(start = 4.dp),
                                            text = "${activationEmailResendCooldownSeconds}s",
                                            maxLines = 1,
                                        )
                                    }
                                } else {
                                    IconButton(
                                        onClick = onResendActivationEmail,
                                        enabled = !isResendingActivationEmail,
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Email,
                                            contentDescription = resendActivationEmailText,
                                            tint = MaterialTheme.colorScheme.secondary,
                                        )
                                    }
                                }
                            }
                        } else {
                            PermissionTooltip(false, noPermissionText) {
                                IconButton(
                                    onClick = onResendActivationEmail,
                                    enabled = false,
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Email,
                                        contentDescription = resendActivationEmailText,
                                        tint = MaterialTheme.colorScheme.secondary,
                                    )
                                }
                            }
                        }
                    }
                    PermissionTooltip(canManage, noPermissionText) {
                        IconButton(onClick = onEdit, enabled = canManage) {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = stringResource(SharedRes.string.edit),
                                tint = MaterialTheme.colorScheme.secondary,
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            SelectionContainer {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    EmployeeInfoPill(
                        icon = Icons.Outlined.Email,
                        label = stringResource(SharedRes.string.email),
                        value = employee.email,
                        isLoading = isLoading,
                    )
                    EmployeeInfoPill(
                        icon = Icons.Outlined.Person,
                        label = stringResource(SharedRes.string.field_username_label),
                        value = employee.username,
                        isLoading = isLoading,
                    )
                    EmployeeInfoPill(
                        icon = Icons.Outlined.Phone,
                        label = stringResource(SharedRes.string.telephone),
                        value = employee.telephone,
                        isLoading = isLoading,
                    )
                    EmployeeInfoPill(
                        icon = Icons.Outlined.Numbers,
                        label = stringResource(SharedRes.string.tax_id),
                        value = employee.taxId,
                        isLoading = isLoading,
                    )
                    EmployeeInfoPill(
                        icon = Icons.Outlined.Badge,
                        label = stringResource(EnterpriseRes.string.employee_user_id),
                        value = employee.userId,
                        isLoading = isLoading,
                    )
                    /*
                    EmployeeInfoPill(
                        icon = Icons.Outlined.Work,
                        label = stringResource(EnterpriseRes.string.employee_role),
                        value = employee.role.displayName(),
                        isLoading = isLoading,
                    )
                    */
                }
            }

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                PermissionTooltip(canManage, noPermissionText) {
                    BusinessOutlinedDeleteButton(
                        enabled = canManage,
                        onDelete = onDelete,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EmployeeActionTooltip(
    text: String,
    content: @Composable () -> Unit,
) {
    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
            TooltipAnchorPosition.Above,
        ),
        tooltip = {
            PlainTooltip {
                SelectionContainer {
                    Text(text)
                }
            }
        },
        state = rememberTooltipState(),
    ) {
        content()
    }
}

@Composable
fun EmployeeStatusChip(status: EmployeeStatus, modifier: Modifier = Modifier) {
    val colors = employeeStatusChipColors(status)
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = colors.container,
        border = BorderStroke(0.5.dp, colors.content.copy(alpha = 0.24f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Surface(
                modifier = Modifier.size(5.dp),
                shape = MaterialTheme.shapes.extraLarge,
                color = colors.content,
                content = {},
            )
            Text(
                text = status.displayName(),
                style = MaterialTheme.typography.labelSmall,
                color = colors.content,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun EmployeeInlineInfo(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            modifier = Modifier.placeholderWithShimmer(isLoading),
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun EmployeeInfoPill(
    icon: ImageVector,
    label: String,
    value: String?,
    isLoading: Boolean,
) {
    Surface(
        modifier = Modifier.placeholderWithShimmer(isLoading),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "$label: ${value?.takeIf { it.isNotBlank() } ?: stringResource(SharedRes.string.not_set)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private data class EmployeeStatusChipColors(
    val container: Color,
    val content: Color,
)

@Composable
private fun employeeStatusChipColors(status: EmployeeStatus): EmployeeStatusChipColors =
    when (status) {
        EmployeeStatus.PENDING_VERIFICATION,
        EmployeeStatus.PENDING_REVIEW -> EmployeeStatusChipColors(
            container = MaterialTheme.colorScheme.tertiaryContainer,
            content = MaterialTheme.colorScheme.onTertiaryContainer,
        )

        EmployeeStatus.ACTIVE,
        EmployeeStatus.APPROVED -> EmployeeStatusChipColors(
            container = MaterialTheme.colorScheme.primaryContainer,
            content = MaterialTheme.colorScheme.onPrimaryContainer,
        )

        EmployeeStatus.INACTIVE -> EmployeeStatusChipColors(
            container = MaterialTheme.colorScheme.surfaceVariant,
            content = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        EmployeeStatus.BANNED -> EmployeeStatusChipColors(
            container = MaterialTheme.colorScheme.errorContainer,
            content = MaterialTheme.colorScheme.onErrorContainer,
        )
    }
