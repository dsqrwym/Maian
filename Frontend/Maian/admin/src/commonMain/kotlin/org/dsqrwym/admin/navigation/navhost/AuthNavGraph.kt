package org.dsqrwym.admin.navigation.navhost

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.focus.FocusManager
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.agreement_warning
import org.dsqrwym.admin.ui.screens.auth.InitialScreen
import org.dsqrwym.admin.ui.screens.auth.LoginScreen
import org.dsqrwym.admin.ui.viewmodels.auth.LoginViewModel
import org.dsqrwym.shared.data.local.SharedUserPreferences
import org.dsqrwym.shared.di.auth.SharedAuthScope
import org.dsqrwym.shared.navigation.*
import org.dsqrwym.shared.ui.animations.SharedAuthAnimation.DefaultEnterTransition
import org.dsqrwym.shared.ui.animations.SharedAuthAnimation.DefaultExitTransition
import org.dsqrwym.shared.ui.animations.SharedAuthAnimation.WebEnterTransition
import org.dsqrwym.shared.ui.animations.SharedAuthAnimation.WebExitTransition
import org.dsqrwym.shared.ui.screens.Agreement.Companion.PRIVACY_POLICY_BASE_URL
import org.dsqrwym.shared.ui.screens.Agreement.Companion.USER_AGREEMENT_BASE_URL
import org.dsqrwym.shared.ui.screens.AgreementScreen
import org.dsqrwym.shared.ui.screens.auth.ResetPasswordScreen
import org.dsqrwym.shared.ui.viewmodels.MySnackbarViewModel
import org.dsqrwym.shared.ui.viewmodels.auth.SharedResetPasswordViewModel
import org.dsqrwym.shared.util.log.SharedLog
import org.dsqrwym.shared.util.navigation.navigateWithKeyboardDismiss
import org.dsqrwym.shared.util.navigation.popBackStackWithKeyboardDismiss
import org.jetbrains.compose.resources.getString
import org.koin.compose.currentKoinScope


fun NavGraphBuilder.authNavGraph(
    navController: NavHostController,
    focusManager: FocusManager,
) {
    composable<SharedInitialScreen>(
        enterTransition = { DefaultEnterTransition },
        exitTransition = { DefaultExitTransition }
    ) { _ ->
        InitialScreen(
            onPrivacyPolicyClick = {
                navController.navigateWithKeyboardDismiss(
                    route = SharedPrivacyPolicy,
                    focusManager = focusManager
                )
            },
            onUserAgreementClick = {
                navController.navigateWithKeyboardDismiss(
                    route = SharedUserAgreement,
                    focusManager = focusManager
                )
            },
            onLoginClick = {
                navController.navigateWithKeyboardDismiss(
                    route = SharedLoginScreen(),
                    focusManager = focusManager
                )
            },
        )
    }

    composable<SharedLoginScreen>(
        enterTransition = { DefaultEnterTransition },
        exitTransition = { DefaultExitTransition }
    ) { backStackEntry ->
        val email = backStackEntry.toRoute<SharedLoginScreen>().email
        val loginViewModel = SharedAuthScope.scope.get<LoginViewModel>()
        email?.let {
            loginViewModel.updateEmail(it)
        }
        CheckIsPermitted(navController)
        LoginScreen(
            onBackButtonClick = {
                navController.navigateWithKeyboardDismiss(SharedInitialScreen, focusManager = focusManager)
            },
            loginViewModel = loginViewModel
        )
    }
    composable<SharedResetPasswordScreen>(
        enterTransition = { DefaultEnterTransition },
        exitTransition = { DefaultExitTransition }
    ) { navBackStackEntry ->
        val resetPasswordViewModel =
            SharedAuthScope.scope.get<SharedResetPasswordViewModel>()
        val email = navBackStackEntry.toRoute<SharedResetPasswordScreen>().email
        email?.let {
            resetPasswordViewModel.updateEmail(it)
        }
        ResetPasswordScreen(
            onBackButtonClick = {
                navController.navigateWithKeyboardDismiss(SharedLoginScreen(), focusManager = focusManager)
            },
            resetPasswordViewModel = resetPasswordViewModel
        )
    }

    composable<SharedPrivacyPolicy>(
        enterTransition = { WebEnterTransition },
        exitTransition = { WebExitTransition }
    ) {
        AgreementScreen(
            baseUrl = PRIVACY_POLICY_BASE_URL,
            getVersion = { version ->
                SharedLog.log(message = "Version: $version")
            }
        ) {
            navController.popBackStackWithKeyboardDismiss(focusManager)
        }
    }

    composable<SharedUserAgreement>(
        enterTransition = { WebEnterTransition },
        exitTransition = { WebExitTransition }
    ) {
        AgreementScreen(
            baseUrl = USER_AGREEMENT_BASE_URL,
            getVersion = {
                SharedLog.log(message = "Version: $it")
            }
        ) {
            navController.popBackStackWithKeyboardDismiss(focusManager)
        }

    }
}


/**
 * CheckIsPermitted
 *
 * EN: Guard to ensure the user has accepted agreements before proceeding to auth screens.
 * If not agreed, redirects to SharedInitialScreen and shows an informational snackbar.
 *
 * ZH: 进入认证页面前的权限校验。若用户未同意协议，则跳转回初始页并弹出提示消息。
 */
@Composable
fun CheckIsPermitted(
    navController: NavController,
    mySnackbarViewModel: MySnackbarViewModel = currentKoinScope().get()
) {
    LaunchedEffect(Unit) {
        if (!SharedUserPreferences.isUserAgreed()) {
            navController.navigate(SharedInitialScreen)
            mySnackbarViewModel.showInfo(message = getString(SharedRes.string.agreement_warning))
        }
    }
}