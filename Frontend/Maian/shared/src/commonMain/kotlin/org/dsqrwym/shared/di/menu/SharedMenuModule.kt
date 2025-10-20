package org.dsqrwym.shared.di.menu

import org.dsqrwym.shared.ui.viewmodels.menu.SharedMenuViewModel
import org.koin.dsl.module

val sharedMenuModule = module {
    single { SharedMenuViewModel() }
}