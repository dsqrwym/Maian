package org.dsqrwym.enterprise.ui.viewmodels.employees

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.delete_failed
import maian.shared.generated.resources.delete_success
import org.dsqrwym.enterprise.data.employee.EmployeeRepository
import org.dsqrwym.enterprise.data.employee.EmployeeRole
import org.dsqrwym.enterprise.data.employee.EmployeeSortField
import org.dsqrwym.enterprise.domain.employee.Employee
import org.dsqrwym.shared.data.OrderDir
import org.dsqrwym.shared.network.model.SharedResponseResult
import org.dsqrwym.shared.paging.data.createPager
import org.dsqrwym.shared.ui.viewmodels.MySnackbarViewModel
import org.dsqrwym.shared.util.timing.SharedUiTiming
import org.jetbrains.compose.resources.getString

data class EmployeesQuery(
    val search: String,
    val role: EmployeeRole?,
    val sortBy: EmployeeSortField?,
    val sortDir: OrderDir,
)

class EmployeesListViewModel(
    private val repository: EmployeeRepository,
    private val mySnackbarViewModel: MySnackbarViewModel,
) : ViewModel() {
    private val pageSize = 20

    var searchQuery by mutableStateOf("")
        private set
    var filterRole by mutableStateOf<EmployeeRole?>(null)
        private set
    var sortBy by mutableStateOf<EmployeeSortField?>(EmployeeSortField.FIRST_NAME)
        private set
    var sortDir by mutableStateOf(OrderDir.ASC)
        private set
    var showFilterDialog by mutableStateOf(false)
        private set
    var showSortDialog by mutableStateOf(false)
        private set
    var deleteEmployee by mutableStateOf<Employee?>(null)
        private set
    var isDeleting by mutableStateOf(false)
        private set

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private val employeeQuery = combine(
        snapshotFlow { searchQuery }
            .debounce(SharedUiTiming.searchDebounce)
            .distinctUntilChanged(),
        snapshotFlow { filterRole }.distinctUntilChanged(),
        snapshotFlow { sortBy }.distinctUntilChanged(),
        snapshotFlow { sortDir }.distinctUntilChanged(),
    ) { search, role, sortBy, sortDir ->
        EmployeesQuery(search, role, sortBy, sortDir)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val pagedEmployees: Flow<PagingData<Employee>> = combine(
        employeeQuery,
        repository.updateEvents.onStart { emit(Unit) },
    ) { query, _ -> query }
        .flatMapLatest { query ->
            createPager(query = query.search, pageSize = pageSize) { page, size, search ->
                when (
                    val result = repository.getEmployees(
                        search = search,
                        role = query.role,
                        sortBy = query.sortBy,
                        sortOrder = query.sortDir,
                        page = page,
                        limit = size,
                    )
                ) {
                    is SharedResponseResult.Success -> result.data?.items ?: emptyList()
                    is SharedResponseResult.Error -> {
                        if (SharedResponseResult.shouldShowToUser(result.type)) {
                            result.message?.let { mySnackbarViewModel.showError(it) }
                        }
                        emptyList()
                    }
                }
            }.flow
        }
        .cachedIn(viewModelScope)

    fun updateSearchQuery(value: String) {
        searchQuery = value
    }

    fun updateFilterRole(value: EmployeeRole?) {
        filterRole = value
    }

    fun updateSortBy(value: EmployeeSortField?) {
        sortBy = value
    }

    fun updateSortDir(value: OrderDir) {
        sortDir = value
    }

    fun toggleSort(field: EmployeeSortField) {
        if (sortBy == field) {
            sortDir = if (sortDir == OrderDir.ASC) OrderDir.DESC else OrderDir.ASC
        } else {
            sortBy = field
            sortDir = OrderDir.ASC
        }
    }

    fun updateShowFilterDialog(value: Boolean) {
        showFilterDialog = value
    }

    fun updateShowSortDialog(value: Boolean) {
        showSortDialog = value
    }

    fun updateDeleteEmployee(value: Employee?) {
        deleteEmployee = value
    }

    fun refresh() {
        viewModelScope.launch {
            repository.notifyUpdated()
        }
    }

    fun deleteEmployee(employee: Employee) {
        if (isDeleting) return
        viewModelScope.launch {
            isDeleting = true
            when (val result = repository.deleteEmployee(employee.id)) {
                is SharedResponseResult.Success -> {
                    mySnackbarViewModel.showSuccess(getString(SharedRes.string.delete_success))
                    deleteEmployee = null
                }

                is SharedResponseResult.Error -> {
                    val message = if (SharedResponseResult.shouldShowToUser(result.type)) {
                        result.message
                    } else {
                        null
                    } ?: getString(SharedRes.string.delete_failed)
                    mySnackbarViewModel.showError(message)
                }
            }
            isDeleting = false
        }
    }
}
