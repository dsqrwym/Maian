package org.dsqrwym.business.ui.workspace

import androidx.compose.runtime.Composable
@Composable
expect fun BusinessAuxiliaryHost(
    workspaceState: BusinessAuxiliaryWorkspaceState,
    mainContent: @Composable () -> Unit,
    auxiliaryContent: @Composable (BusinessAuxiliarySurface) -> Unit,
)
