package org.dsqrwym.standard.navigation.navhost

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.focus.FocusManager
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import org.dsqrwym.shared.data.local.UserPreferences
import org.dsqrwym.shared.language.SharedLanguageMap
import org.dsqrwym.shared.ui.animations.SharedAuthAnimation.DefaultEnterTransition
import org.dsqrwym.shared.ui.animations.SharedAuthAnimation.DefaultExitTransition
import org.dsqrwym.shared.ui.animations.SharedAuthAnimation.WebEnterTransition
import org.dsqrwym.shared.ui.animations.SharedAuthAnimation.WebExitTransition
import org.dsqrwym.shared.ui.components.containers.SharedAuthContainer
import org.dsqrwym.shared.ui.screens.SharedAgreement.Companion.PRIVACY_POLICY_BASE_URL
import org.dsqrwym.shared.ui.screens.SharedAgreement.Companion.USER_AGREEMENT_BASE_URL
import org.dsqrwym.shared.ui.screens.SharedAgreementScreen
import org.dsqrwym.shared.ui.viewmodels.SharedSnackbarViewModel
import org.dsqrwym.shared.util.log.SharedLog
import org.dsqrwym.shared.util.navigation.navigateWithKeyboardDismiss
import org.dsqrwym.shared.util.navigation.onLeaveScreen
import org.dsqrwym.shared.util.navigation.popBackStackWithKeyboardDismiss
import org.dsqrwym.standard.navigation.*
import org.dsqrwym.standard.ui.viewmodels.auth.AuthViewModel
import org.koin.compose.currentKoinScope
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AuthNavHost(
    navController: NavHostController,
    focusManager: FocusManager,
    authViewModel: AuthViewModel = koinViewModel<AuthViewModel>()
) {
    SharedAuthContainer {
        NavHost(navController = navController, startDestination = InitialScreen) {
            composable<InitialScreen>(
                enterTransition = { DefaultEnterTransition },
                exitTransition = { DefaultExitTransition }
            ) { navBackStackEntry ->
                org.dsqrwym.standard.ui.screens.auth.InitialScreen(
                    onPrivacyPolicyClick = {
                        navController.navigateWithKeyboardDismiss(route = PrivacyPolicy, focusManager = focusManager)
                    },
                    onUserAgreementClick = {
                        navController.navigateWithKeyboardDismiss(route = UserAgreement, focusManager = focusManager)
                    },
                    onLoginClick = {
                        navController.navigateWithKeyboardDismiss(route = LoginScreen, focusManager = focusManager)
                    },
                )
            }

            composable<LoginScreen>(
                enterTransition = { DefaultEnterTransition },
                exitTransition = { DefaultExitTransition }
            ) {
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
                CheckIsPermitted(navController)
                LaunchedEffect(Unit) {
                    navController.onLeaveScreen(navBackStackEntry.destination.route){
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
                SharedAgreementScreen(
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
                SharedAgreementScreen(
                    baseUrl = USER_AGREEMENT_BASE_URL,
                    getVersion = {
                        SharedLog.log(message = "Version: $it")
                    }
                ) {
                    navController.popBackStackWithKeyboardDismiss(focusManager)
                }
            }
        }
    }
}


@Composable
fun CheckIsPermitted(navController: NavController) {
    val sharedSnackbarViewModel: SharedSnackbarViewModel = currentKoinScope().get()
    LaunchedEffect(Unit) {
        if (!UserPreferences.isUserAgreed()) {
            navController.navigate(InitialScreen)
            sharedSnackbarViewModel.showMessage(message = SharedLanguageMap.currentStrings.value.initial_screen_agreement_warning /*"请先同意用户协议才能继续"*/)
        }
    }
}