package org.dsqrwym.standard.navigation.navhost

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.focus.FocusManager
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
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
import org.dsqrwym.shared.ui.viewmodels.auth.SharedResetPasswordViewModel
import org.dsqrwym.shared.util.log.SharedLog
import org.dsqrwym.shared.util.navigation.navigateWithKeyboardDismiss
import org.dsqrwym.shared.util.navigation.popBackStackWithKeyboardDismiss
import org.dsqrwym.standard.navigation.RegisterScreen
import org.dsqrwym.shared.ui.screens.auth.ResetPasswordScreen
import org.dsqrwym.standard.ui.viewmodels.auth.LoginViewModel
import org.dsqrwym.standard.ui.viewmodels.auth.RegisterViewModel
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
            onRegisterClick = {
                navController.navigateWithKeyboardDismiss(
                    route = RegisterScreen,
                    focusManager = focusManager
                )
            },
            onLoginClick = {
                navController.navigateWithKeyboardDismiss(
                    route = LoginScreen(),
                    focusManager = focusManager
                )
            },
        )
    }

    composable<RegisterScreen>(
        enterTransition = { DefaultEnterTransition },
        exitTransition = { DefaultExitTransition }
    ) {
        val registerViewModel = currentKoinScope().get<RegisterViewModel>()
        CheckIsPermitted(navController)
        org.dsqrwym.standard.ui.screens.auth.RegisterScreen(
            onBackButtonClick = {
                navController.navigateWithKeyboardDismiss(InitialScreen, focusManager = focusManager)
            },
            registerViewModel = registerViewModel
        )
    }
    composable<LoginScreen>(
        enterTransition = { DefaultEnterTransition },
        exitTransition = { DefaultExitTransition }
    ) { backStackEntry ->
        val email = backStackEntry.toRoute<LoginScreen>().email
        val loginViewModel = currentKoinScope().get<LoginViewModel>()
        email?.let {
            loginViewModel.updateEmail(it)
        }
        CheckIsPermitted(navController)
        org.dsqrwym.standard.ui.screens.auth.LoginScreen(
            onBackButtonClick = {
                navController.navigateWithKeyboardDismiss(InitialScreen, focusManager = focusManager)
            },
            loginViewModel = loginViewModel
        )
    }
    composable<ForgotPasswordScreen>(
        enterTransition = { DefaultEnterTransition },
        exitTransition = { DefaultExitTransition }
    ) { navBackStackEntry ->
        val resetPasswordViewModel =
            currentKoinScope().get<SharedResetPasswordViewModel>()
        val email = navBackStackEntry.toRoute<ForgotPasswordScreen>().email
        email?.let {
            resetPasswordViewModel.updateEmail(it)
        }
        ResetPasswordScreen(
            onBackButtonClick = {
                navController.navigateWithKeyboardDismiss(LoginScreen(), focusManager = focusManager)
            },
            resetPasswordViewModel = resetPasswordViewModel
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