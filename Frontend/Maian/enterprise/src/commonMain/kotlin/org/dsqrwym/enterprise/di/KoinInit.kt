package org.dsqrwym.enterprise.di

import org.dsqrwym.enterprise.di.auth.enterpriseAuthModule
import org.dsqrwym.shared.di.sharedInitKoin

fun enterpriseInitKoin(){
    sharedInitKoin{
        modules(standardModule)
        modules(enterpriseAuthModule)
    }
}