package org.dsqrwym.enterprise.navigation.naventry

import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.NavDisplay
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.agreement_warning
import org.dsqrwym.enterprise.navigation.RegisterScreen
import org.dsqrwym.enterprise.ui.screens.auth.InitialScreen
import org.dsqrwym.enterprise.ui.screens.auth.LoginScreen
import org.dsqrwym.enterprise.ui.screens.auth.RegisterScreen
import org.dsqrwym.enterprise.ui.viewmodels.auth.LoginViewModel
import org.dsqrwym.enterprise.ui.viewmodels.auth.RegisterViewModel
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
import org.dsqrwym.shared.ui.viewmodels.navigation.SharedNavigationViewModel
import org.dsqrwym.shared.util.log.SharedLog
import org.jetbrains.compose.resources.getString
import org.koin.compose.currentKoinScope

fun EntryProviderScope<NavKey>.authNavEntry(navViewModel: SharedNavigationViewModel) {
    entry<SharedInitialScreen>(
        metadata =
            NavDisplay.transitionSpec {
                DefaultEnterTransition togetherWith DefaultExitTransition
            } + NavDisplay.popTransitionSpec {
                DefaultEnterTransition togetherWith DefaultExitTransition
            } + NavDisplay.predictivePopTransitionSpec {
                DefaultEnterTransition togetherWith DefaultExitTransition
            }
    ) {
        InitialScreen(
            onPrivacyPolicyClick = { navViewModel.navigate(SharedPrivacyPolicy) },
            onUserAgreementClick = { navViewModel.navigate(SharedUserAgreement) },
            onRegisterClick = { navViewModel.navigate(RegisterScreen) },
            onLoginClick = { navViewModel.navigate(SharedLoginScreen()) }
        )
    }
    entry<RegisterScreen>(
        metadata =
            NavDisplay.transitionSpec {
                DefaultEnterTransition togetherWith DefaultExitTransition
            } + NavDisplay.popTransitionSpec {
                DefaultEnterTransition togetherWith DefaultExitTransition
            } + NavDisplay.predictivePopTransitionSpec {
                DefaultEnterTransition togetherWith DefaultExitTransition
            }
    ) {
        CheckIsPermitted(navViewModel)
        val registerViewModel = SharedAuthScope.scope.get<RegisterViewModel>()
        RegisterScreen(
            onBackButtonClick = { navViewModel.navigate(SharedInitialScreen) },
            registerViewModel = registerViewModel,
            onNavigate = { route ->
                navViewModel.navigate(route)
            })
    }
    entry<SharedLoginScreen>(
        metadata =
            NavDisplay.transitionSpec {
                DefaultEnterTransition togetherWith DefaultExitTransition
            } + NavDisplay.popTransitionSpec {
                DefaultEnterTransition togetherWith DefaultExitTransition
            } + NavDisplay.predictivePopTransitionSpec {
                DefaultEnterTransition togetherWith DefaultExitTransition
            }
    ) {
        CheckIsPermitted(navViewModel)
        val loginViewModel = SharedAuthScope.scope.get<LoginViewModel>()
        it.email?.let { email -> loginViewModel.updateEmail(email) }
        LoginScreen(
            onBackButtonClick = { navViewModel.navigate(SharedInitialScreen) },
            loginViewModel = loginViewModel,
            onNavigate = { route -> navViewModel.navigate(route) }
        )
    }
    entry<SharedPrivacyPolicy>(
        metadata =
            NavDisplay.transitionSpec {
                WebEnterTransition togetherWith WebExitTransition
            } + NavDisplay.popTransitionSpec {
                WebEnterTransition togetherWith WebExitTransition
            } + NavDisplay.predictivePopTransitionSpec {
                WebEnterTransition togetherWith WebExitTransition
            }
    ) {
        AgreementScreen(
            baseUrl = PRIVACY_POLICY_BASE_URL,
            getVersion = { version ->
                SharedLog.log(message = "Version: $version")
            }
        ) { navViewModel.pop() }
    }
    entry<SharedUserAgreement>(
        metadata =
            NavDisplay.transitionSpec {
                WebEnterTransition togetherWith WebExitTransition
            } + NavDisplay.popTransitionSpec {
                WebEnterTransition togetherWith WebExitTransition
            }
    ) {
        AgreementScreen(
            baseUrl = USER_AGREEMENT_BASE_URL,
            getVersion = {
                SharedLog.log(message = "Version: $it")
            }
        ) { navViewModel.pop() }
    }

    entry<SharedResetPasswordScreen>(
        metadata =
            NavDisplay.transitionSpec {
                DefaultEnterTransition togetherWith DefaultExitTransition
            } + NavDisplay.popTransitionSpec {
                DefaultEnterTransition togetherWith DefaultExitTransition
            } + NavDisplay.predictivePopTransitionSpec {
                DefaultEnterTransition togetherWith DefaultExitTransition
            }
    ) { route ->
        val resetPasswordViewModel = SharedAuthScope.scope.get<SharedResetPasswordViewModel>()
        val email = route.email
        email?.let {
            resetPasswordViewModel.updateEmail(it)
        }
        ResetPasswordScreen(
            onNavigate = { navViewModel.navigate(it) },
            onBackButtonClick = { navViewModel.navigate(SharedLoginScreen()) },
            resetPasswordViewModel = resetPasswordViewModel
        )
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
    navViewModel: SharedNavigationViewModel,
    mySnackbarViewModel: MySnackbarViewModel = currentKoinScope().get()
) {
    LaunchedEffect(Unit) {
        if (!SharedUserPreferences.isUserAgreed()) {
            navViewModel.replace(SharedInitialScreen)
            mySnackbarViewModel.showInfo(message = getString(SharedRes.string.agreement_warning))
        }
    }
}