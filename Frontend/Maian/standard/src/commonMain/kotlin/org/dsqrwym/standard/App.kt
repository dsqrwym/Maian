package org.dsqrwym.standard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import org.dsqrwym.shared.AppRoot
import org.dsqrwym.shared.LocalAppFocusManager
import org.dsqrwym.shared.LocalNavHostController
import org.dsqrwym.shared.data.auth.session.AuthState
import org.dsqrwym.shared.navigation.InitialScreen
import org.dsqrwym.shared.navigation.navhost.SharedAppNavHost
import org.dsqrwym.shared.ui.components.containers.AuthContainer
import org.dsqrwym.standard.navigation.navhost.authNavGraph
import org.dsqrwym.standard.ui.viewmodels.auth.LoginViewModel
import org.koin.compose.currentKoinScope

@Composable
        /**
         * App (Standard module)
         *
         * EN: Entry point for the app in the standard flavor. Creates a NavController, initializes
         * AppRoot, and wires the AuthNavHost. Optionally exposes navController via onNavHostReady.
         *
         * ZH: 标准模块的应用入口。创建 NavController，初始化 AppRoot，并接入 AuthNavHost。
         * 可通过 onNavHostReady 回调暴露 navController。
         */
fun App(
    onNavHostReady: suspend (NavController) -> Unit = {}
) {
    AppRoot { authState ->
        val navController = LocalNavHostController.current

        val loginViewModel: LoginViewModel = currentKoinScope().get()

        LaunchedEffect(Unit) {
            onNavHostReady(navController)
        }
        when (authState) {
            is AuthState.Unauthenticated -> {
                // 未登录 → 整个 Auth 流程都包在 AuthContainer 下
                val focusManager = LocalAppFocusManager.current
                AuthContainer {
                    SharedAppNavHost(
                        navController = navController,
                        focusManager = focusManager,
                        startDestination = InitialScreen
                    ) { navController, focusManager ->
                        authNavGraph(navController, focusManager)
                    }
                }
            }

            is AuthState.Authenticated -> {
                // 已登录 → 渲染主业务 Graph
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    ElevatedButton(onClick = {
                        loginViewModel.logout()
                    }) {
                        Text("Logout")
                    }
                }
            }
        }
    }
}
