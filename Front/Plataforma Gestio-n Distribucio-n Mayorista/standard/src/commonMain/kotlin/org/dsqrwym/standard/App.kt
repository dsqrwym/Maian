package org.dsqrwym.standard

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import org.dsqrwym.shared.AppRoot
import org.dsqrwym.shared.data.local.UserPreferences
import org.dsqrwym.shared.ui.screen.SharedAgreement.Companion.PRIVACY_POLICY_BASE_URL
import org.dsqrwym.shared.ui.screen.SharedAgreement.Companion.USER_AGREEMENT_BASE_URL
import org.dsqrwym.shared.ui.screen.SharedAgreementScreen
import org.dsqrwym.shared.util.log.sharedlog
import org.dsqrwym.standard.navigation.InitialScreen
import org.dsqrwym.standard.navigation.LoginScreen
import org.dsqrwym.standard.navigation.PrivacyPolicy
import org.dsqrwym.standard.navigation.UserAgreement

@Composable
fun App(
    onNavHostReady: suspend (NavController) -> Unit = {}
) {
    val navController = rememberNavController()

    AppRoot {
        NavHost(navController = navController, startDestination = InitialScreen(false)) {
            composable<InitialScreen> {navBackStackEntry ->
                val initialScreen : InitialScreen = navBackStackEntry.toRoute()
                org.dsqrwym.standard.ui.screen.InitialScreen(
                    showAgreementWarning = initialScreen.denied,
                    onPrivacyPolicyClick = {navController.navigate(PrivacyPolicy)},
                    onUserAgreementClick = {navController.navigate(UserAgreement)},
                    onLoginClick = { navController.navigate(LoginScreen) },
                )
            }

            composable<LoginScreen> {
                CheckIsPermitted(navController)
                org.dsqrwym.standard.ui.screen.LoginScreen(
                    onBackButtonClick = { navController.navigate(InitialScreen(false)) }
                )
            }

            composable<PrivacyPolicy> {
                SharedAgreementScreen(
                    baseUrl = PRIVACY_POLICY_BASE_URL,
                    onBackButtonClick = { navController.navigate(InitialScreen(false)) },
                ){
                    sharedlog(message = "version$it")
                }
            }

            composable<UserAgreement> {
                SharedAgreementScreen(
                    baseUrl = USER_AGREEMENT_BASE_URL,
                    onBackButtonClick = { navController.navigate(InitialScreen(false)) },
                ){
                    sharedlog(message = "version$it")
                }
            }
        }

        LaunchedEffect(Unit){
            onNavHostReady(navController)
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