package org.dsqrwym.standard.navigation.navhost

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.focus.FocusManager
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import org.dsqrwym.shared.data.local.UserPreferences
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
import org.jetbrains.compose.resources.getString
import org.koin.compose.currentKoinScope
import org.koin.compose.viewmodel.koinViewModel
import plataformagestio_ndistribucio_nmayorista.shared.generated.resources.SharedRes
import plataformagestio_ndistribucio_nmayorista.shared.generated.resources.initial_screen_agreement_warning

@Composable
/**
 * AuthNavHost
 *
 * EN: Navigation host for authentication-related screens. Sets up routes for Initial, Login,
 * Forgot Password, and Agreement pages. Handles keyboard dismissal on navigation and provides
 * screen transition animations. Wraps content in SharedAuthContainer to unify styling.
 *
 * ZH: 认证相关页面的导航容器。配置初始页、登录、忘记密码、协议等路由；在导航时处理键盘收起，
 * 并设置页面切换动效。使用 SharedAuthContainer 包裹内容以统一样式。
 */
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
/**
 * CheckIsPermitted
 *
 * EN: Guard to ensure the user has accepted agreements before proceeding to auth screens.
 * If not agreed, redirects to InitialScreen and shows an informational snackbar.
 *
 * ZH: 进入认证页面前的权限校验。若用户未同意协议，则跳转回初始页并弹出提示消息。
 */
fun CheckIsPermitted(navController: NavController) {
    val sharedSnackbarViewModel: SharedSnackbarViewModel = currentKoinScope().get()
    LaunchedEffect(Unit) {
        if (!UserPreferences.isUserAgreed()) {
            navController.navigate(InitialScreen)
            sharedSnackbarViewModel.showInfo(message = getString(SharedRes.string.initial_screen_agreement_warning))
        }
    }
}