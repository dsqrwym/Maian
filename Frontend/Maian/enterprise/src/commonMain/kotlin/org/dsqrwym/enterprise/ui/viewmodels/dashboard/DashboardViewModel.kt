package org.dsqrwym.enterprise.ui.viewmodels.dashboard

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.operation_failed
import org.dsqrwym.enterprise.data.dashboard.DashboardRepository
import org.dsqrwym.enterprise.data.dashboard.dto.DashboardResponse
import org.dsqrwym.shared.network.model.SharedResponseResult
import org.dsqrwym.shared.ui.viewmodels.MySnackbarViewModel
import org.dsqrwym.shared.util.formatter.defaultOrderHistoryDateRange
import org.dsqrwym.shared.util.timing.SharedUiTiming
import org.jetbrains.compose.resources.getString

data class DashboardUiState(
    val startDate: String,
    val endDate: String,
    val topLimit: Int,
    val data: DashboardResponse? = null,
    val initialLoading: Boolean = true,
    val refreshing: Boolean = false,
    val error: Boolean = false,
)

data class DashboardQueryState(
    val startDate: String,
    val endDate: String,
    val topLimit: Int,
)

class DashboardViewModel(
    private val repository: DashboardRepository,
    private val snackbarViewModel: MySnackbarViewModel,
) : ViewModel() {
    var uiState by mutableStateOf(defaultState())
        private set

    init {
        observeFilters()
    }

    @OptIn(FlowPreview::class)
    private fun observeFilters() {
        viewModelScope.launch {
            snapshotFlow { uiState.toQueryState() }
                .distinctUntilChanged()
                .debounce(SharedUiTiming.searchDebounce)
                .collectLatest { queryState ->
                    loadDashboard(queryState)
                }
        }
    }

    fun updateStartDate(value: String) {
        uiState = uiState.copy(
            startDate = value,
            endDate = if (value > uiState.endDate) value else uiState.endDate,
        )
    }

    fun updateEndDate(value: String) {
        uiState = uiState.copy(
            startDate = if (value < uiState.startDate) value else uiState.startDate,
            endDate = value,
        )
    }

    fun updateTopLimit(value: Int?) {
        uiState = uiState.copy(topLimit = (value ?: 5).coerceIn(5, 20))
    }

    fun refresh() {
        if (uiState.initialLoading || uiState.refreshing) return
        viewModelScope.launch {
            loadDashboard(uiState.toQueryState())
        }
    }

    private suspend fun loadDashboard(queryState: DashboardQueryState) {
        val hadData = uiState.data != null
        uiState = uiState.copy(
            initialLoading = !hadData,
            refreshing = hadData,
            error = false,
        )

        when (
            val result = repository.getDashboard(
                startDate = queryState.startDate,
                endDate = queryState.endDate,
                topLimit = queryState.topLimit,
            )
        ) {
            is SharedResponseResult.Success -> {
                uiState = uiState.copy(
                    data = result.data,
                    initialLoading = false,
                    refreshing = false,
                    error = false,
                )
            }

            is SharedResponseResult.Error -> {
                uiState = uiState.copy(
                    initialLoading = false,
                    refreshing = false,
                    error = true,
                )
                showDashboardError(result)
            }
        }
    }

    private suspend fun showDashboardError(result: SharedResponseResult.Error) {
        val fallback = getString(SharedRes.string.operation_failed)
        if (SharedResponseResult.shouldShowToUser(result.type)) {
            snackbarViewModel.showError(result.message ?: fallback)
        } else {
            snackbarViewModel.showError(fallback)
        }
    }

    private fun defaultState(): DashboardUiState {
        val (startDate, endDate) = defaultOrderHistoryDateRange()
        return DashboardUiState(
            startDate = startDate,
            endDate = endDate,
            topLimit = 5,
        )
    }
}

fun DashboardUiState.toQueryState(): DashboardQueryState =
    DashboardQueryState(
        startDate = startDate,
        endDate = endDate,
        topLimit = topLimit,
    )
