package org.dsqrwym.standard.navigation.navhost

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import org.dsqrwym.standard.navigation.InitialScreen
import org.dsqrwym.standard.navigation.LoginScreen
import org.dsqrwym.standard.navigation.PrivacyPolicy
import org.dsqrwym.standard.navigation.UserAgreement
import org.koin.compose.currentKoinScope

@Composable
fun AuthNavHost(navController: NavHostController) {
    SharedAuthContainer {
        NavHost(navController = navController, startDestination = InitialScreen) {
            composable<InitialScreen>(
                enterTransition = { DefaultEnterTransition },
                exitTransition = { DefaultExitTransition }
            ) { navBackStackEntry ->
                org.dsqrwym.standard.ui.screens.InitialScreen(
                    onPrivacyPolicyClick = { navController.navigate(PrivacyPolicy) },
                    onUserAgreementClick = { navController.navigate(UserAgreement) },
                    onLoginClick = { navController.navigate(LoginScreen) },
                )
            }

            composable<LoginScreen>(
                enterTransition = { DefaultEnterTransition },
                exitTransition = { DefaultExitTransition }
            ) {
                CheckIsPermitted(navController)
                org.dsqrwym.standard.ui.screens.LoginScreen(
                    onBackButtonClick = { navController.navigate(InitialScreen) }
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
                    navController.navigate(InitialScreen)
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
                    navController.navigate(InitialScreen)
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