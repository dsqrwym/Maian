package org.dsqrwym.enterprise.ui.viewmodels.employees

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import maian.enterprise.generated.resources.EnterpriseRes
import maian.enterprise.generated.resources.employee_activation_email_resend_failed
import maian.enterprise.generated.resources.employee_activation_email_resent
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
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.ExperimentalTime

data class EmployeesQuery(
    val search: String,
    val role: EmployeeRole?,
    val sortBy: EmployeeSortField?,
    val sortDir: OrderDir,
)

@OptIn(ExperimentalTime::class)
class EmployeesListViewModel(
    private val repository: EmployeeRepository,
    private val mySnackbarViewModel: MySnackbarViewModel,
) : ViewModel() {
    companion object {
        private const val ACTIVATION_EMAIL_RESEND_COOLDOWN_SECONDS = 60
        private val activationEmailResendCooldownEndsAtSeconds = mutableMapOf<String, Long>()
    }

    private val pageSize = 20
    private val activationEmailResendCooldownJobs = mutableMapOf<String, Job>()

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
    var resendingActivationEmailEmployeeId by mutableStateOf<String?>(null)
        private set
    var activationEmailResendCooldowns by mutableStateOf(currentActivationEmailResendCooldowns())
        private set

    init {
        resumeActivationEmailResendCooldowns()
    }

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

    fun resendActivationEmail(employee: Employee) {
        if (resendingActivationEmailEmployeeId != null) return
        if (getActivationEmailResendCooldown(employee.id) > 0) {
            return
        }
        viewModelScope.launch {
            resendingActivationEmailEmployeeId = employee.id
            try {
                when (val result = repository.resendActivationEmail(employee.id)) {
                    is SharedResponseResult.Success -> {
                        mySnackbarViewModel.showSuccess(
                            getString(EnterpriseRes.string.employee_activation_email_resent)
                        )
                        startActivationEmailResendCooldown(employee.id)
                    }

                    is SharedResponseResult.Error -> {
                        if (result.type == HttpStatusCode.TooManyRequests) {
                            startActivationEmailResendCooldown(employee.id)
                        }
                        showResendActivationEmailError(result)
                    }
                }
            } finally {
                resendingActivationEmailEmployeeId = null
            }
        }
    }

    fun getActivationEmailResendCooldown(employeeId: String): Int {
        return activationEmailResendCooldowns[employeeId] ?: 0
    }

    private suspend fun showResendActivationEmailError(result: SharedResponseResult.Error) {
        val message = if (SharedResponseResult.shouldShowToUser(result.type)) {
            result.message
        } else {
            null
        } ?: getString(EnterpriseRes.string.employee_activation_email_resend_failed)

        if (result.type == HttpStatusCode.TooManyRequests) {
            mySnackbarViewModel.showInfo(message)
        } else {
            mySnackbarViewModel.showError(message)
        }
    }

    private fun startActivationEmailResendCooldown(employeeId: String) {
        activationEmailResendCooldownEndsAtSeconds[employeeId] =
            Clock.System.now().epochSeconds + ACTIVATION_EMAIL_RESEND_COOLDOWN_SECONDS
        activationEmailResendCooldowns = currentActivationEmailResendCooldowns()
        ensureActivationEmailResendCooldownJob(employeeId)
    }

    private fun resumeActivationEmailResendCooldowns() {
        activationEmailResendCooldowns = currentActivationEmailResendCooldowns()
        activationEmailResendCooldowns.keys.forEach(::ensureActivationEmailResendCooldownJob)
    }

    private fun ensureActivationEmailResendCooldownJob(employeeId: String) {
        if (activationEmailResendCooldownJobs[employeeId]?.isActive == true) return
        activationEmailResendCooldownJobs[employeeId] = viewModelScope.launch {
            while (true) {
                val remainingSeconds = currentActivationEmailResendCooldown(employeeId)
                if (remainingSeconds <= 0) {
                    activationEmailResendCooldowns = activationEmailResendCooldowns - employeeId
                    activationEmailResendCooldownJobs.remove(employeeId)
                    break
                }
                activationEmailResendCooldowns =
                    activationEmailResendCooldowns + (employeeId to remainingSeconds)
                delay(1000.milliseconds)
            }
        }
    }

    private fun currentActivationEmailResendCooldown(employeeId: String): Int {
        val endsAtSeconds = activationEmailResendCooldownEndsAtSeconds[employeeId] ?: return 0
        val remainingSeconds = (endsAtSeconds - Clock.System.now().epochSeconds).coerceAtLeast(0)
        if (remainingSeconds <= 0) {
            activationEmailResendCooldownEndsAtSeconds.remove(employeeId)
        }
        return remainingSeconds.toInt()
    }

    private fun currentActivationEmailResendCooldowns(): Map<String, Int> {
        return activationEmailResendCooldownEndsAtSeconds.keys
            .toList()
            .mapNotNull { employeeId ->
                val remainingSeconds = currentActivationEmailResendCooldown(employeeId)
                if (remainingSeconds > 0) {
                    employeeId to remainingSeconds
                } else {
                    null
                }
            }
            .toMap()
    }
}
