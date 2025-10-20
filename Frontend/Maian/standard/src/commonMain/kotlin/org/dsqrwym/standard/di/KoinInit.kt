package org.dsqrwym.standard.di

import org.dsqrwym.shared.di.sharedInitKoin
import org.dsqrwym.standard.di.auth.standardAuthModule

fun standardInitKoin(){
    sharedInitKoin{
        modules(standardModule)
        modules(standardAuthModule)
    }
}