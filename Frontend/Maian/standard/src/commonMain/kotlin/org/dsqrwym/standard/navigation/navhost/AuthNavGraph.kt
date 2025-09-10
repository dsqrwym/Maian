package org.dsqrwym.standard.navigation.navhost

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.focus.FocusManager
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import org.dsqrwym.shared.data.local.UserPreferences
import org.dsqrwym.shared.navigation.*
import org.dsqrwym.shared.ui.animations.SharedAuthAnimation.DefaultEnterTransition
import org.dsqrwym.shared.ui.animations.SharedAuthAnimation.DefaultExitTransition
import org.dsqrwym.shared.ui.animations.SharedAuthAnimation.WebEnterTransition
import org.dsqrwym.shared.ui.animations.SharedAuthAnimation.WebExitTransition
import org.dsqrwym.shared.ui.screens.Agreement.Companion.PRIVACY_POLICY_BASE_URL
import org.dsqrwym.shared.ui.screens.Agreement.Companion.USER_AGREEMENT_BASE_URL
import org.dsqrwym.shared.ui.screens.AgreementScreen
import org.dsqrwym.shared.ui.viewmodels.MySnackbarViewModel
import org.dsqrwym.shared.util.log.SharedLog
import org.dsqrwym.shared.util.navigation.navigateWithKeyboardDismiss
import org.dsqrwym.shared.util.navigation.onLeaveScreen
import org.dsqrwym.shared.util.navigation.popBackStackWithKeyboardDismiss
import org.dsqrwym.standard.ui.viewmodels.auth.AuthViewModel
import org.jetbrains.compose.resources.getString
import org.koin.compose.currentKoinScope
import plataformagestio_ndistribucio_nmayorista.shared.generated.resources.SharedRes
import plataformagestio_ndistribucio_nmayorista.shared.generated.resources.agreement_warning


fun NavGraphBuilder.authNavGraph(
    navController: NavHostController,
    focusManager: FocusManager,
) {
    composable<InitialScreen>(
        enterTransition = { DefaultEnterTransition },
        exitTransition = { DefaultExitTransition }
    ) { _ ->
        org.dsqrwym.standard.ui.screens.auth.InitialScreen(
            onPrivacyPolicyClick = {
                navController.navigateWithKeyboardDismiss(
                    route = PrivacyPolicy,
                    focusManager = focusManager
                )
            },
            onUserAgreementClick = {
                navController.navigateWithKeyboardDismiss(
                    route = UserAgreement,
                    focusManager = focusManager
                )
            },
            onLoginClick = {
                navController.navigateWithKeyboardDismiss(
                    route = LoginScreen,
                    focusManager = focusManager
                )
            },
        )
    }

    composable<LoginScreen>(
        enterTransition = { DefaultEnterTransition },
        exitTransition = { DefaultExitTransition }
    ) {
        val authViewModel = currentKoinScope().get<AuthViewModel>()
        CheckIsPermitted(navController)
        org.dsqrwym.standard.ui.screens.auth.LoginScreen(
            onBackButtonClick = {
                navController.popBackStackWithKeyboardDismiss(focusManager)
            },
            onForgetPasswordClick = {
                navController.navigateWithKeyboardDismiss(
                    route = ForgotPasswordScreen,
                    focusManager = focusManager
                )
            },
            authViewModel = authViewModel
        )
    }
    composable<ForgotPasswordScreen>(
        enterTransition = { DefaultEnterTransition },
        exitTransition = { DefaultExitTransition }
    ) { navBackStackEntry ->
        val authViewModel = currentKoinScope().get<AuthViewModel>()
        CheckIsPermitted(navController)
        LaunchedEffect(Unit) {
            navController.onLeaveScreen(navBackStackEntry.destination.route) {
                authViewModel.resetForgetPassword()
            }
        }
        org.dsqrwym.standard.ui.screens.auth.ForgotPasswordScreen(
            onBackButtonClick = {
                navController.popBackStackWithKeyboardDismiss(focusManager)
            },
            authViewModel = authViewModel
        )
    }

    composable<PrivacyPolicy>(
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

    composable<UserAgreement>(
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


@Composable
        /**
         * CheckIsPermitted
         *
         * EN: Guard to ensure the user has accepted agreements before proceeding to auth screens.
         * If not agreed, redirects to InitialScreen and shows an informational snackbar.
         *
         * ZH: 进入认证页面前的权限校验。若用户未同意协议，则跳转回初始页并弹出提示消息。
         */
fun CheckIsPermitted(
    navController: NavController,
    mySnackbarViewModel: MySnackbarViewModel = currentKoinScope().get()
) {
    LaunchedEffect(Unit) {
        if (!UserPreferences.isUserAgreed()) {
            navController.navigate(InitialScreen)
            mySnackbarViewModel.showInfo(message = getString(SharedRes.string.agreement_warning))
        }
    }
}