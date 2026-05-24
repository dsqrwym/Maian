package org.dsqrwym.enterprise.ui.screens.dashboard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import maian.enterprise.generated.resources.EnterpriseRes
import maian.enterprise.generated.resources.dashboard_refresh
import org.dsqrwym.enterprise.ui.viewmodels.dashboard.DashboardViewModel
import org.dsqrwym.shared.ui.components.buttons.SharedRetryButton
import org.dsqrwym.shared.ui.components.progressindicators.SharedLoadingDotsIndicator
import org.dsqrwym.shared.ui.components.scaffold.SharedTransparentScaffold
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = koinViewModel(),
) {
    val state = viewModel.uiState
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    var datePickerTarget by remember { mutableStateOf<DashboardDatePickerTarget?>(null) }

    SharedTransparentScaffold(
        topBarScrollBehavior = scrollBehavior,
        showOverlayDialog = datePickerTarget != null,
        overlayContent = {
            datePickerTarget?.let { target ->
                DashboardDatePickerDialog(
                    target = target,
                    state = state,
                    onDismiss = { datePickerTarget = null },
                    onDateSelected = { date ->
                        when (target) {
                            DashboardDatePickerTarget.Start -> viewModel.updateStartDate(date)
                            DashboardDatePickerTarget.End -> viewModel.updateEndDate(date)
                        }
                        datePickerTarget = null
                    },
                )
            }
        },
        title = {
            DashboardTopBarFilters(
                state = state,
                onStartDateClick = { datePickerTarget = DashboardDatePickerTarget.Start },
                onEndDateClick = { datePickerTarget = DashboardDatePickerTarget.End },
                onTopLimitChange = viewModel::updateTopLimit,
            )
        },
        actions = {
            IconButton(
                enabled = !state.initialLoading && !state.refreshing,
                onClick = viewModel::refresh,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Refresh,
                    contentDescription = stringResource(EnterpriseRes.string.dashboard_refresh),
                )
            }
        },
    ) { padding, topBarScrollBehavior ->
        if (state.data != null) {
            DashboardContent(
                data = state.data,
                padding = padding,
                scrollBehavior = topBarScrollBehavior,
                isRefreshing = state.refreshing
            )
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                when {
                    state.initialLoading -> SharedLoadingDotsIndicator()
                    state.error -> SharedRetryButton(viewModel::refresh)
                    else -> SharedLoadingDotsIndicator()
                }
            }
        }
    }
}

