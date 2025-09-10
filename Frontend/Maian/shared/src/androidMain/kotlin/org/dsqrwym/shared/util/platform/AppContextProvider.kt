package org.dsqrwym.shared.util.platform

import android.content.Context

object AppContextProvider {
    private lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    fun get(): Context {
        if (!::appContext.isInitialized) {
            throw IllegalStateException("AppContextProvider is not initialized. Call init() in Application.onCreate()")
        }
        return appContext
    }
}