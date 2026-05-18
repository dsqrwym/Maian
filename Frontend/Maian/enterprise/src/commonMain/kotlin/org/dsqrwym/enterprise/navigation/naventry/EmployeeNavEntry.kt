package org.dsqrwym.enterprise.navigation.naventry

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import org.dsqrwym.enterprise.navigation.EmployeeCreate
import org.dsqrwym.enterprise.navigation.EmployeeEdit
import org.dsqrwym.enterprise.navigation.Employees
import org.dsqrwym.enterprise.ui.screens.employees.EmployeeCreateScreen
import org.dsqrwym.enterprise.ui.screens.employees.EmployeeEditScreen
import org.dsqrwym.enterprise.ui.screens.employees.EmployeesListScreen
import org.dsqrwym.shared.data.user.UserRole
import org.dsqrwym.shared.ui.viewmodels.navigation.SharedNavigationState

fun EntryProviderScope<NavKey>.employeeNavEntry(
    viewModel: SharedNavigationState,
    userRole: UserRole? = null,
) {
    entry<Employees> {
        EmployeesListScreen(
            userRole = userRole,
            onNavigateToCreate = {
                viewModel.navigate(EmployeeCreate)
            },
            onNavigateToEdit = { employee ->
                viewModel.navigate(
                    EmployeeEdit(
                        id = employee.id,
                        email = employee.email,
                        role = employee.role,
                        status = employee.status,
                    ),
                )
            },
        )
    }

    entry<EmployeeCreate> {
        EmployeeCreateScreen(
            onNavigateBack = {
                if (!viewModel.popTo(Employees)) {
                    viewModel.navigateToTopLevel(Employees)
                }
            },
        )
    }

    entry<EmployeeEdit> { route ->
        EmployeeEditScreen(
            id = route.id,
            email = route.email,
            role = route.role,
            status = route.status,
            onNavigateBack = {
                if (!viewModel.popTo(Employees)) {
                    viewModel.navigateToTopLevel(Employees)
                }
            },
        )
    }
}
