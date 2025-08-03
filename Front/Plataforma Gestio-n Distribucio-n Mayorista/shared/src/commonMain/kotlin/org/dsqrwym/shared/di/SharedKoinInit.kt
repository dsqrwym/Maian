package org.dsqrwym.shared.di

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin

fun sharedInitKoin(callback: (KoinApplication.() -> Unit)? = null) {
    startKoin {
        modules(sharedModule)
        callback?.invoke(this)
    }
}