package org.dsqrwym.business.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.dsqrwym.business.ui.workspace.BusinessAuxiliarySurface

@Serializable
@SerialName("Product-Workspace-Main")
data object ProductWorkspaceMainPane : NavKey

@Serializable
@SerialName("Product-Workspace-Aux")
data class ProductWorkspaceAuxPane(
    val surface: BusinessAuxiliarySurface,
) : NavKey
