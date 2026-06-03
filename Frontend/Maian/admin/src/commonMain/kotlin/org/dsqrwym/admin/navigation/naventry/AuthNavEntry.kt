package org.dsqrwym.admin.navigation.naventry

import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.NavDisplay
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.agreement_warning
import org.dsqrwym.admin.ui.screens.auth.InitialScreen
import org.dsqrwym.admin.ui.screens.auth.LoginScreen
import org.dsqrwym.admin.ui.viewmodels.auth.LoginViewModel
import org.dsqrwym.shared.data.local.SharedUserPreferences
import org.dsqrwym.shared.di.auth.SharedAuthScope
import org.dsqrwym.shared.navigation.SharedInitialScreen
import org.dsqrwym.shared.navigation.SharedLoginScreen
import org.dsqrwym.shared.navigation.SharedPrivacyPolicy
import org.dsqrwym.shared.navigation.SharedResetPasswordScreen
import org.dsqrwym.shared.navigation.SharedUserAgreement
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
import org.dsqrwym.shared.ui.viewmodels.navigation.SharedNavigationState
import org.dsqrwym.shared.util.log.SharedLog
import org.dsqrwym.shared.util.platform.PlatformType
import org.dsqrwym.shared.util.platform.getPlatform
import org.jetbrains.compose.resources.getString
import org.koin.compose.currentKoinScope

fun EntryProviderScope<NavKey>.authNavEntry(navViewModel: SharedNavigationState) {
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
            onPrivacyPolicyClick = {
                if (getPlatform().type != PlatformType.Desktop) {
                    navViewModel.navigate(SharedPrivacyPolicy)
                }
            },
            onUserAgreementClick = {
                if (getPlatform().type != PlatformType.Desktop) {
                    navViewModel.navigate(SharedUserAgreement)
                }
            },
            onLoginClick = { navViewModel.navigate(SharedLoginScreen()) },
        )
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
    ) { key ->
        val email = key.email
        val loginViewModel = SharedAuthScope.scope.get<LoginViewModel>()
        LaunchedEffect(email) {
            email?.let {
                loginViewModel.updateEmail(it)
            }
        }
        CheckIsPermitted(navViewModel)
        LoginScreen(
            onBackButtonClick = {
                if (!navViewModel.popTo(SharedInitialScreen)) {
                    navViewModel.replace(SharedInitialScreen)
                }
            },
            loginViewModel = loginViewModel,
            onNavigate = navViewModel::navigate,
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
    ) { key ->
        val resetPasswordViewModel =
            SharedAuthScope.scope.get<SharedResetPasswordViewModel>()
        val email = key.email
        LaunchedEffect(email) {
            email?.let {
                resetPasswordViewModel.updateEmail(it)
            }
        }
        ResetPasswordScreen(
            onNavigate = { navViewModel.navigate(it) },
            onBackButtonClick = {
                navViewModel.pop()
            },
            resetPasswordViewModel = resetPasswordViewModel,
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
    navController: SharedNavigationState,
    mySnackbarViewModel: MySnackbarViewModel = currentKoinScope().get(),
) {
    LaunchedEffect(Unit) {
        if (!SharedUserPreferences.isUserAgreed()) {
            if (!navController.popTo(SharedInitialScreen)) {
                navController.replace(SharedInitialScreen)
            }
            mySnackbarViewModel.showInfo(message = getString(SharedRes.string.agreement_warning))
        }
    }
}
