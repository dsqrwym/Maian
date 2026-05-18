package org.dsqrwym.enterprise.di.employees

import org.dsqrwym.enterprise.data.employee.EmployeeApi
import org.dsqrwym.enterprise.data.employee.EmployeeRepository
import org.dsqrwym.enterprise.ui.viewmodels.employees.EmployeeCreateViewModel
import org.dsqrwym.enterprise.ui.viewmodels.employees.EmployeeEditViewModel
import org.dsqrwym.enterprise.ui.viewmodels.employees.EmployeesListViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val employeesModule = module {
    single { EmployeeApi(get()) }
    single { EmployeeRepository(get()) }
    viewModel<EmployeesListViewModel> { EmployeesListViewModel(get(), get()) }
    viewModel<EmployeeCreateViewModel> { EmployeeCreateViewModel(get(), get(), get(), get(), get()) }
    viewModel<EmployeeEditViewModel> { EmployeeEditViewModel(get(), get(), get(), get(), get()) }
}
