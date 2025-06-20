package org.dsqrwym.standard.navigation.navhost

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import org.dsqrwym.shared.data.local.UserPreferences
import org.dsqrwym.shared.ui.animation.SharedAuthAnimation.DefaultEnterTransition
import org.dsqrwym.shared.ui.animation.SharedAuthAnimation.DefaultExitTransition
import org.dsqrwym.shared.ui.animation.SharedAuthAnimation.WebEnterTransition
import org.dsqrwym.shared.ui.animation.SharedAuthAnimation.WebExitTransition
import org.dsqrwym.shared.ui.component.container.SharedAuthContainer
import org.dsqrwym.shared.ui.screen.SharedAgreement.Companion.PRIVACY_POLICY_BASE_URL
import org.dsqrwym.shared.ui.screen.SharedAgreement.Companion.USER_AGREEMENT_BASE_URL
import org.dsqrwym.shared.ui.screen.SharedAgreementScreen
import org.dsqrwym.shared.util.log.sharedlog
import org.dsqrwym.standard.navigation.InitialScreen
import org.dsqrwym.standard.navigation.LoginScreen
import org.dsqrwym.standard.navigation.PrivacyPolicy
import org.dsqrwym.standard.navigation.UserAgreement

@Composable
fun AuthNavHost(navController: NavHostController, dev: Boolean = false) {
    SharedAuthContainer {
        NavHost(navController = navController, startDestination = InitialScreen(false)) {
            composable<InitialScreen>(
                enterTransition = { DefaultEnterTransition },
                exitTransition = { DefaultExitTransition }
            ) { navBackStackEntry ->
                val initialScreen: InitialScreen = navBackStackEntry.toRoute()
                org.dsqrwym.standard.ui.screen.InitialScreen(
                    dev = dev,
                    showAgreementWarning = initialScreen.denied,
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
                org.dsqrwym.standard.ui.screen.LoginScreen(
                    onBackButtonClick = { navController.navigate(InitialScreen(false)) }
                )
            }

            composable<PrivacyPolicy>(
                enterTransition = { WebEnterTransition },
                exitTransition = { WebExitTransition }
            ) {
                SharedAgreementScreen(
                    baseUrl = PRIVACY_POLICY_BASE_URL,
                    getVersion = {
                        sharedlog(message = "Version: $it")
                    }
                ) {
                    navController.navigate(InitialScreen(false))
                }
            }

            composable<UserAgreement>(
                enterTransition = { WebEnterTransition },
                exitTransition = { WebExitTransition }
            ) {
                SharedAgreementScreen(
                    baseUrl = USER_AGREEMENT_BASE_URL,
                    getVersion = {
                        sharedlog(message = "Version: $it")
                    }
                ) {
                    navController.navigate(InitialScreen(false))
                }
            }
        }
    }
}


@Composable
fun CheckIsPermitted(navController: NavController) {
    LaunchedEffect(Unit) {
        if (!UserPreferences.isUserAgreed()) {
            navController.navigate(InitialScreen(true))
        }
    }
}