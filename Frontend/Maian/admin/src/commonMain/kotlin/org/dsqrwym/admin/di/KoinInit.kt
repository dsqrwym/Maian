package org.dsqrwym.admin.di

import org.dsqrwym.admin.di.auth.adminAuthModule
import org.dsqrwym.admin.di.categories.categoriesModule
import org.dsqrwym.shared.di.sharedInitKoin

fun adminInitKoin(){
    sharedInitKoin{
        modules(adminAuthModule)
        modules(categoriesModule)
    }
}