package fr.miage.geotrouvetou.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import fr.miage.geotrouvetou.App
import fr.miage.geotrouvetou.R
import fr.miage.geotrouvetou.ui.components.atoms.Toast
import fr.miage.geotrouvetou.ui.components.atoms.ToastType
import fr.miage.geotrouvetou.ui.auth.LoginScreen
import fr.miage.geotrouvetou.ui.auth.RegisterScreen
import fr.miage.geotrouvetou.ui.components.molecules.NavBar
import fr.miage.geotrouvetou.ui.components.molecules.NavTab
import fr.miage.geotrouvetou.ui.admin.AdminScreen
import fr.miage.geotrouvetou.ui.profile.EditPasswordScreen
import fr.miage.geotrouvetou.ui.profile.EditProfileScreen
import fr.miage.geotrouvetou.ui.events.EventFormScreen
import fr.miage.geotrouvetou.ui.events.EventDetailScreen
import fr.miage.geotrouvetou.ui.map.MapScreen
import fr.miage.geotrouvetou.ui.params.ParamsScreen
import fr.miage.geotrouvetou.ui.profile.ProfileScreen

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val MAP = "map"
    const val PROFILE = "profile"
    const val PARAMS = "params"
    const val ADMIN = "admin"
    const val EDIT_PROFILE = "editProfile"
    const val EDIT_PASSWORD = "editPassword"
    const val CREATE_EVENT = "createEvent"
    const val EVENT_DETAIL = "eventDetail/{eventId}"

    fun eventDetail(eventId: String) = "eventDetail/$eventId"
}

private data class NavToast(
    @param:StringRes val title: Int,
    @param:StringRes val description: Int,
    val type: ToastType = ToastType.Success,
)

private fun NavGraphBuilder.slideComposable(
    route: String,
    content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit,
) = composable(
    route = route,
    enterTransition = { slideInHorizontally { it } },
    exitTransition = { slideOutHorizontally { -it } },
    popEnterTransition = { slideInHorizontally { -it } },
    popExitTransition = { slideOutHorizontally { it } },
    content = content,
)

@Composable
fun NavGraph(navController: NavHostController) {
    val context = LocalContext.current
    val app = context.applicationContext as? App

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    val selectedTab = when (currentRoute) {
        Routes.PROFILE, Routes.EVENT_DETAIL -> NavTab.Profil
        Routes.CREATE_EVENT -> NavTab.Ajouter
        else -> NavTab.Carte
    }

    var toastTick by remember { mutableIntStateOf(0) }
    var pendingToast by remember { mutableStateOf<NavToast?>(null) }
    fun showToast(toast: NavToast) {
        pendingToast = toast
        toastTick++
    }

    fun navigateIfLoggedIn(destination: String) {
        val isLoggedIn = runCatching { app?.authService?.isLoggedIn() == true }.getOrDefault(false)
        navController.navigate(if (isLoggedIn) destination else Routes.LOGIN)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = Routes.MAP,
            modifier = Modifier.weight(1f),
        ) {
            composable(Routes.LOGIN) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Routes.MAP) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                        showToast(NavToast(R.string.toast_login_title, R.string.toast_welcome_desc))
                    },
                    onRegisterClick = { navController.navigate(Routes.REGISTER) },
                )
            }

            composable(Routes.REGISTER) {
                RegisterScreen(
                    onRegisterSuccess = {
                        navController.navigate(Routes.MAP) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                        showToast(NavToast(R.string.toast_register_title, R.string.toast_welcome_desc))
                    },
                    onBackClick = { navController.popBackStack() },
                )
            }

            composable(Routes.MAP) {
                MapScreen(onLoginClick = { navController.navigate(Routes.LOGIN) })
            }

            composable(Routes.PROFILE) {
                ProfileScreen(
                    onLogout = {
                        navController.navigate(Routes.MAP) {
                            popUpTo(Routes.MAP) { inclusive = true }
                        }
                    },
                    onSettingsClick = { navController.navigate(Routes.PARAMS) },
                    onEventClick = { eventId -> navController.navigate(Routes.eventDetail(eventId)) }
                )
            }

            composable(Routes.EVENT_DETAIL) { backStackEntry ->
                EventDetailScreen(
                    eventId = backStackEntry.arguments?.getString("eventId"),
                    onBackClick = { navController.popBackStack() },
                    onLoginClick = { navController.navigate(Routes.LOGIN) },
                    onEventDeleted = {
                        navController.popBackStack()
                        showToast(NavToast(R.string.event_delete_toast_title, R.string.event_delete_toast_desc))
                    },
                )
            }

            slideComposable(Routes.PARAMS) {
                ParamsScreen(
                    onBackClick = { navController.popBackStack() },
                    onLogout = {
                        navController.navigate(Routes.MAP) {
                            popUpTo(0) { inclusive = true }
                        }
                        showToast(NavToast(R.string.toast_logout_title, R.string.toast_logout_desc))
                    },
                    onEditProfileClick = { navController.navigate(Routes.EDIT_PROFILE) },
                    onEditPasswordClick = { navController.navigate(Routes.EDIT_PASSWORD) },
                    onAdminClick = { navController.navigate(Routes.ADMIN) },
                )
            }

            slideComposable(Routes.EDIT_PASSWORD) {
                EditPasswordScreen(onBackClick = { navController.popBackStack() })
            }

            slideComposable(Routes.EDIT_PROFILE) {
                EditProfileScreen(
                    onBackClick = { navController.popBackStack() },
                    onAccountDeleted = {
                        navController.navigate(Routes.MAP) {
                            popUpTo(0) { inclusive = true }
                        }
                        showToast(NavToast(R.string.toast_account_deleted_title, R.string.toast_account_deleted_desc))
                    },
                )
            }

            slideComposable(Routes.ADMIN) {
                AdminScreen(onBackClick = { navController.popBackStack() })
            }

            composable(Routes.CREATE_EVENT) {
                EventFormScreen(
                    onSaved = {
                        navController.navigate(Routes.MAP) {
                            popUpTo(Routes.MAP) { inclusive = false }
                            launchSingleTop = true
                        }
                        showToast(NavToast(R.string.toast_event_created_title, R.string.toast_event_created_desc))
                    },
                )
            }
        }

        pendingToast?.let { toast ->
            Toast(
                title = stringResource(toast.title),
                description = stringResource(toast.description),
                type = toast.type,
                key = toastTick,
            )
        }

        val hideNavBar = currentRoute in setOf(Routes.PARAMS, Routes.EDIT_PROFILE, Routes.EDIT_PASSWORD, Routes.ADMIN)
        if (!hideNavBar) {
            NavBar(
                selectedTab = selectedTab,
                onTabSelected = { tab ->
                    when (tab) {
                        NavTab.Carte -> navController.navigate(Routes.MAP) {
                            popUpTo(Routes.MAP) { inclusive = false }
                            launchSingleTop = true
                        }
                        NavTab.Profil -> navigateIfLoggedIn(Routes.PROFILE)
                        NavTab.Ajouter -> navigateIfLoggedIn(Routes.CREATE_EVENT)
                    }
                },
                modifier = Modifier
                    .background(colorResource(R.color.white))
                    .navigationBarsPadding(),
            )
        }
    }
}
