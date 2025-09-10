package org.dsqrwym.standard.di

import org.dsqrwym.shared.di.sharedInitKoin

fun standardInitKoin(){
    sharedInitKoin{
        modules(standardModule)
    }
}