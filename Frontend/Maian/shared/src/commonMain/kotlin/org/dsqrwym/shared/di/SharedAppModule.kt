package org.dsqrwym.shared.di

import org.dsqrwym.shared.network.HttpClientProvider
import org.dsqrwym.shared.ui.viewmodels.MySnackbarViewModel
import org.koin.dsl.module

/**
 * Koin module that wires up shared-layer singletons.
 * 定义共享层所需的 Koin 单例依赖。
 */
val sharedModule = module {
    single {
        // Provide singleton HttpClient for network calls
        // 提供网络请求所用的 HttpClient（单例）
        HttpClientProvider.client
    }
    single {
        // ViewModel responsible for cross-platform snackbar messages
        // 负责跨平台 Snackbar 消息的 ViewModel
        MySnackbarViewModel()
    }
}