package org.dsqrwym.enterprise.di

import org.dsqrwym.enterprise.di.auth.enterpriseAuthModule
import org.dsqrwym.enterprise.di.categories.categoriesModule
import org.dsqrwym.enterprise.di.products.productsModule
import org.dsqrwym.shared.di.sharedInitKoin

fun enterpriseInitKoin(){
    sharedInitKoin{
        modules(enterpriseModule)
        modules(enterpriseAuthModule)
        modules(categoriesModule)
        modules(productsModule)
    }
}