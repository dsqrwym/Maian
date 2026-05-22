package org.dsqrwym.enterprise.ui.screens.employees

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import maian.enterprise.generated.resources.EnterpriseRes
import maian.enterprise.generated.resources.add_employee
import maian.enterprise.generated.resources.employee_delete_message
import maian.enterprise.generated.resources.employee_role
import maian.enterprise.generated.resources.employee_search_placeholder
import maian.enterprise.generated.resources.no_employees_found
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.all
import maian.shared.generated.resources.close
import maian.shared.generated.resources.confirm_delete_title
import maian.shared.generated.resources.error_no_permission
import maian.shared.generated.resources.filter
import maian.shared.generated.resources.sort
import org.dsqrwym.enterprise.data.employee.EmployeeRole
import org.dsqrwym.enterprise.data.employee.displayName
import org.dsqrwym.enterprise.data.employee.employeeSortFields
import org.dsqrwym.enterprise.domain.employee.Employee
import org.dsqrwym.enterprise.permissions.canManageEnterpriseEmployees
import org.dsqrwym.enterprise.ui.components.employees.EmployeeCard
import org.dsqrwym.enterprise.ui.viewmodels.employees.EmployeesListViewModel
import org.dsqrwym.shared.data.OrderDir
import org.dsqrwym.shared.data.user.UserRole
import org.dsqrwym.shared.paging.hasLoadError
import org.dsqrwym.shared.paging.isAppendingOrPrepending
import org.dsqrwym.shared.paging.isEmptyResult
import org.dsqrwym.shared.paging.isRefreshing
import org.dsqrwym.shared.ui.components.buttons.SharedCloseButton
import org.dsqrwym.shared.ui.components.containers.UiState
import org.dsqrwym.shared.ui.components.dialog.SharedConfirmDeleteDialog
import org.dsqrwym.shared.ui.components.icon.SharedCloseIcon
import org.dsqrwym.shared.ui.components.input.SharedSingleLinePlaceholderText
import org.dsqrwym.shared.ui.components.placeholder.SharedNotFoundPlaceholder
import org.dsqrwym.shared.ui.components.product.SharedProductSortDirectionLabel
import org.dsqrwym.shared.ui.components.row.SharedFilterChipsRow
import org.dsqrwym.shared.ui.components.scaffold.SharedTransparentScaffold
import org.dsqrwym.shared.ui.components.scaffold.SharedTransparentScaffoldFabButtonState
import org.dsqrwym.shared.ui.overlay.rememberResizeSafeDismissRequest
import org.dsqrwym.shared.ui.overlay.transparentDialogProperties
import org.dsqrwym.shared.util.lazygrid.SharedLazyGridLayout
import org.dsqrwym.shared.util.lazygrid.SharedLazyGridLayout.appendErrorRetry
import org.dsqrwym.shared.util.lazygrid.SharedLazyGridLayout.appendLoadingIndicator
import org.dsqrwym.shared.util.modifier.paddingWithoutTop
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeesListScreen(
    viewModel: EmployeesListViewModel = koinViewModel(),
    userRole: UserRole? = null,
    onNavigateToCreate: () -> Unit = {},
    onNavigateToEdit: (Employee) -> Unit = {},
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val pagingItems = viewModel.pagedEmployees.collectAsLazyPagingItems()
    val canManageEmployees = userRole?.canManageEnterpriseEmployees() == true
    val noPermissionText = stringResource(SharedRes.string.error_no_permission)

    SharedTransparentScaffold(
        topBarScrollBehavior = scrollBehavior,
        showOverlayDialog = viewModel.showFilterDialog ||
                viewModel.showSortDialog ||
                viewModel.deleteEmployee != null,
        overlayContent = {
            viewModel.deleteEmployee?.let { employee ->
                EmployeeConfirmDeleteDialog(
                    employee = employee,
                    onDismiss = { viewModel.updateDeleteEmployee(null) },
                    onConfirm = { viewModel.deleteEmployee(employee) },
                )
            }
            if (viewModel.showFilterDialog) {
                EmployeeFilterDialog(viewModel)
            }
            if (viewModel.showSortDialog) {
                EmployeeSortDialog(viewModel)
            }
        },
        title = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    SearchBarDefaults.InputField(
                        modifier = Modifier.weight(0.8f),
                        query = viewModel.searchQuery,
                        onQueryChange = viewModel::updateSearchQuery,
                        onSearch = { viewModel.refresh() },
                        expanded = false,
                        onExpandedChange = {},
                        placeholder = {
                            SharedSingleLinePlaceholderText(
                                stringResource(EnterpriseRes.string.employee_search_placeholder)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.Search,
                                stringResource(EnterpriseRes.string.employee_search_placeholder),
                            )
                        },
                        trailingIcon = {
                            if (viewModel.searchQuery.isNotEmpty()) {
                                SharedCloseButton { viewModel.updateSearchQuery("") }
                            }
                        },
                    )
                    IconButton(onClick = { viewModel.updateShowFilterDialog(true) }) {
                        Icon(Icons.Outlined.FilterList, stringResource(SharedRes.string.filter))
                    }
                    IconButton(onClick = { viewModel.updateShowSortDialog(true) }) {
                        Icon(Icons.Outlined.SwapVert, stringResource(SharedRes.string.sort))
                    }
                }

                EmployeeFilterChipsRow(viewModel = viewModel, modifier = Modifier.fillMaxWidth())
            }
        },
        fabButtonState = SharedTransparentScaffoldFabButtonState(
            buttonState = UiState.Idle,
            buttonEnabled = canManageEmployees,
            onButtonClick = onNavigateToCreate,
            buttonText = stringResource(EnterpriseRes.string.add_employee),
            buttonIcon = Icons.Filled.Add,
            buttonIconDescription = stringResource(EnterpriseRes.string.add_employee),
            disabledTooltipText = noPermissionText,
        ),
    ) { padding, topBarScrollBehavior ->
        val isRefreshing = pagingItems.isRefreshing || viewModel.isDeleting
        val pullRefreshState = rememberPullToRefreshState()

        PullToRefreshBox(
            modifier = Modifier
                .fillMaxSize()
                .paddingWithoutTop(padding),
            isRefreshing = isRefreshing,
            state = pullRefreshState,
            onRefresh = { pagingItems.refresh() },
            indicator = {
                PullToRefreshDefaults.Indicator(
                    modifier = Modifier.padding(top = padding.calculateTopPadding()).align(Alignment.TopCenter),
                    isRefreshing = isRefreshing,
                    state = pullRefreshState,
                )
            },
        ) {
            if (pagingItems.isEmptyResult) {
                SharedNotFoundPlaceholder(stringResource(EnterpriseRes.string.no_employees_found))
            } else {
                LazyVerticalGrid(
                    modifier = Modifier
                        .fillMaxSize()
                        .nestedScroll(topBarScrollBehavior.nestedScrollConnection)
                        .padding(horizontal = SharedLazyGridLayout.Padding),
                    columns = GridCells.Adaptive(minSize = 399.dp),
                    contentPadding = PaddingValues(bottom = 28.dp),
                    horizontalArrangement = SharedLazyGridLayout.arrangement,
                    verticalArrangement = SharedLazyGridLayout.arrangement,
                ) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Spacer(Modifier.height(padding.calculateTopPadding()))
                    }

                    if (pagingItems.hasLoadError) {
                        appendErrorRetry { pagingItems.retry() }
                    } else {
                        items(
                            count = pagingItems.itemCount,
                            key = pagingItems.itemKey { it.id },
                        ) { index ->
                            pagingItems[index]?.let { employee ->
                                EmployeeCard(
                                    modifier = Modifier.animateItem(),
                                    employee = employee,
                                    isLoading = isRefreshing,
                                    canManage = canManageEmployees,
                                    noPermissionText = noPermissionText,
                                    onEdit = { onNavigateToEdit(employee) },
                                    onDelete = { viewModel.updateDeleteEmployee(employee) },
                                )
                            }
                        }
                    }

                    if (pagingItems.isAppendingOrPrepending) {
                        appendLoadingIndicator()
                    }
                }
            }
        }
    }
}

@Composable
private fun EmployeeFilterDialog(
    viewModel: EmployeesListViewModel,
) {
    val resizeSafeDismiss = rememberResizeSafeDismissRequest(
        onDismissRequest = { viewModel.updateShowFilterDialog(false) },
    )

    AlertDialog(
        properties = transparentDialogProperties(),
        onDismissRequest = resizeSafeDismiss,
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Work, contentDescription = null)
                    Text(
                        modifier = Modifier.padding(start = 8.dp),
                        text = stringResource(EnterpriseRes.string.employee_role),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ElevatedFilterChip(
                        selected = viewModel.filterRole == null,
                        onClick = { viewModel.updateFilterRole(null) },
                        label = { Text(stringResource(SharedRes.string.all)) },
                    )
                    EmployeeRole.entries.forEach { role ->
                        ElevatedFilterChip(
                            selected = viewModel.filterRole == role,
                            onClick = { viewModel.updateFilterRole(role) },
                            label = { Text(role.displayName()) },
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { viewModel.updateShowFilterDialog(false) }) {
                Text(stringResource(SharedRes.string.close))
            }
        },
    )
}

@Composable
private fun EmployeeSortDialog(
    viewModel: EmployeesListViewModel,
) {
    val resizeSafeDismiss = rememberResizeSafeDismissRequest(
        onDismissRequest = { viewModel.updateShowSortDialog(false) },
    )

    AlertDialog(
        properties = transparentDialogProperties(),
        onDismissRequest = resizeSafeDismiss,
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.SwapVert, contentDescription = null)
                    Text(
                        modifier = Modifier.padding(start = 8.dp),
                        text = stringResource(SharedRes.string.sort),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    employeeSortFields.forEach { field ->
                        val selected = viewModel.sortBy == field
                        val label = field.displayName()
                        ElevatedFilterChip(
                            selected = selected,
                            onClick = { viewModel.toggleSort(field) },
                            label = {
                                if (selected) {
                                    SharedProductSortDirectionLabel(label = label, sortDir = viewModel.sortDir)
                                } else {
                                    Text(label)
                                }
                            },
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { viewModel.updateShowSortDialog(false) }) {
                Text(stringResource(SharedRes.string.close))
            }
        },
    )
}

@Composable
private fun EmployeeFilterChipsRow(
    viewModel: EmployeesListViewModel,
    modifier: Modifier = Modifier,
) {
    SharedFilterChipsRow(modifier = modifier) {
        viewModel.filterRole?.let { role ->
            ElevatedFilterChip(
                selected = true,
                onClick = { viewModel.updateFilterRole(null) },
                label = { Text("${stringResource(EnterpriseRes.string.employee_role)}: ${role.displayName()}") },
                trailingIcon = { SharedCloseIcon() },
            )
        }
        viewModel.sortBy?.let { sortBy ->
            ElevatedFilterChip(
                selected = true,
                onClick = {
                    viewModel.updateSortDir(
                        if (viewModel.sortDir == OrderDir.ASC) OrderDir.DESC else OrderDir.ASC,
                    )
                },
                label = {
                    SharedProductSortDirectionLabel(
                        label = "${stringResource(SharedRes.string.sort)}: ${sortBy.displayName()}",
                        sortDir = viewModel.sortDir,
                    )
                },
            )
        }
    }
}

@Composable
private fun EmployeeConfirmDeleteDialog(
    employee: Employee,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    SharedConfirmDeleteDialog(
        title = stringResource(SharedRes.string.confirm_delete_title),
        text = stringResource(EnterpriseRes.string.employee_delete_message, employee.fullName, employee.email),
        onDismissRequest = onDismiss,
        onConfirm = onConfirm,
    )
}
