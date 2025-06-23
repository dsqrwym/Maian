package org.dsqrwym.shared.di

import org.koin.core.context.startKoin

fun sharedInitKoin(){
    startKoin {
        modules(sharedModule)
    }
}