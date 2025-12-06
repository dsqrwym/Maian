package org.dsqrwym.shared.di

import org.dsqrwym.shared.di.auth.sharedAuthModule
import org.dsqrwym.shared.di.category.sharedCategoryModule
import org.dsqrwym.shared.di.location.sharedLocationModule
import org.dsqrwym.shared.di.menu.sharedMenuModule
import org.dsqrwym.shared.di.product.sharedProductModule
import org.dsqrwym.shared.di.user.sharedUserModule
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin

/**
 * sharedInitKoin
 *
 * EN: Initialize Koin for shared module, installing base and auth modules. Optionally allows
 * a callback to extend the KoinApplication (e.g., for platform-specific modules).
 *
 * ZH: 初始化共享模块的 Koin，安装基础与认证相关模块。可选提供回调以扩展 KoinApplication（如平台特定模块）。
 */
fun sharedInitKoin(callback: (KoinApplication.() -> Unit)? = null) {
    startKoin {
        modules(sharedModule)
        modules(sharedAuthModule)
        modules(sharedMenuModule)
        modules(sharedLocationModule)
        modules(sharedCategoryModule)
        modules(sharedUserModule)
        modules(sharedProductModule)
        callback?.invoke(this)
    }
}