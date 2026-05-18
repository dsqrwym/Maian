package org.dsqrwym.enterprise.di

import org.dsqrwym.enterprise.di.auth.enterpriseAuthModule
import org.dsqrwym.enterprise.di.categories.categoriesModule
import org.dsqrwym.enterprise.di.employees.employeesModule
import org.dsqrwym.enterprise.di.orders.ordersModule
import org.dsqrwym.enterprise.di.products.productsModule
import org.dsqrwym.enterprise.di.profile.profileModule
import org.dsqrwym.shared.di.sharedInitKoin

fun enterpriseInitKoin(){
    sharedInitKoin{
        modules(enterpriseModule)
        modules(enterpriseAuthModule)
        modules(categoriesModule)
        modules(employeesModule)
        modules(productsModule)
        modules(ordersModule)
        modules(profileModule)
    }
}
