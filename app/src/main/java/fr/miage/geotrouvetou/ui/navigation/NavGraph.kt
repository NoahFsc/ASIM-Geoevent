package fr.miage.geotrouvetou.ui.navigation

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
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import fr.miage.geotrouvetou.App
import fr.miage.geotrouvetou.R
import fr.miage.geotrouvetou.ui.components.atoms.Toast
import fr.miage.geotrouvetou.ui.auth.LoginScreen
import fr.miage.geotrouvetou.ui.auth.RegisterScreen
import fr.miage.geotrouvetou.ui.components.molecules.NavBar
import fr.miage.geotrouvetou.ui.components.molecules.NavTab
import fr.miage.geotrouvetou.ui.admin.AdminScreen
import fr.miage.geotrouvetou.ui.profile.EditPasswordScreen
import fr.miage.geotrouvetou.ui.profile.EditProfileScreen
import fr.miage.geotrouvetou.ui.events.CreateEventScreen
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
    const val EVENT_DETAIL = "eventDetail"
}

@Composable
fun NavGraph(navController: NavHostController) {
    val context = LocalContext.current
    val app = context.applicationContext as? App

    var selectedTab by remember { mutableStateOf(NavTab.Carte) }
    var loginToastKey by remember { mutableIntStateOf(0) }
    var registerToastKey by remember { mutableIntStateOf(0) }
    var deleteAccountToastKey by remember { mutableIntStateOf(0) }
    var logoutToastKey by remember { mutableIntStateOf(0) }
    var eventCreatedToastKey by remember { mutableIntStateOf(0) }
    val navBackStackEntryAsState by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntryAsState?.destination?.route

    fun navigateIfLoggedIn(destination: String, tab: NavTab) {
        selectedTab = tab
        val isLoggedIn = runCatching {
            app?.authService?.isLoggedIn() == true
        }.getOrDefault(false)

        if (isLoggedIn) {
            navController.navigate(destination)
        } else {
            navController.navigate(Routes.LOGIN)
        }
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
                        selectedTab = NavTab.Carte
                        navController.navigate(Routes.MAP) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                        loginToastKey++
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
                        registerToastKey++
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
                        selectedTab = NavTab.Carte
                        navController.navigate(Routes.MAP) {
                            popUpTo(Routes.MAP) { inclusive = true }
                        }
                    },
                    onSettingsClick = { navController.navigate(Routes.PARAMS) },
                    onEventClick = { eventId ->
                        navController.navigate("${Routes.EVENT_DETAIL}/$eventId")
                    }
                )
            }

            composable("${Routes.EVENT_DETAIL}/{eventId}") { backStackEntry ->
                val eventId = backStackEntry.arguments?.getString("eventId")
                EventDetailScreen(
                    eventId = eventId,
                    onBackClick = { navController.popBackStack() },
                    onLoginClick = { navController.navigate(Routes.LOGIN) }
                )
            }

            composable(
                route = Routes.PARAMS,
                enterTransition = { slideInHorizontally { it } },
                exitTransition = { slideOutHorizontally { -it } },
                popEnterTransition = { slideInHorizontally { -it } },
                popExitTransition = { slideOutHorizontally { it } },
            ) {
                ParamsScreen(
                    onBackClick = { navController.popBackStack() },
                    onLogout = {
                        selectedTab = NavTab.Carte
                        navController.navigate(Routes.MAP) {
                            popUpTo(0) { inclusive = true }
                        }
                        logoutToastKey++
                    },
                    onEditProfileClick = { navController.navigate(Routes.EDIT_PROFILE) },
                    onEditPasswordClick = { navController.navigate(Routes.EDIT_PASSWORD) },
                    onAdminClick = { navController.navigate(Routes.ADMIN) },
                )
            }

            composable(Routes.EDIT_PASSWORD,
                enterTransition = { slideInHorizontally { it } },
                exitTransition = { slideOutHorizontally { -it } },
                popEnterTransition = { slideInHorizontally { -it } },
                popExitTransition = { slideOutHorizontally { it } },) {
                EditPasswordScreen(onBackClick = { navController.popBackStack() })
            }

            composable(Routes.EDIT_PROFILE,
                enterTransition = { slideInHorizontally { it } },
                exitTransition = { slideOutHorizontally { -it } },
                popEnterTransition = { slideInHorizontally { -it } },
                popExitTransition = { slideOutHorizontally { it } },
                ) {
                EditProfileScreen(
                    onBackClick = { navController.popBackStack() },
                    onAccountDeleted = {
                        selectedTab = NavTab.Carte
                        navController.navigate(Routes.MAP) {
                            popUpTo(0) { inclusive = true }
                        }
                        deleteAccountToastKey++
                    },
                )
            }

            composable(
                route = Routes.ADMIN,
                enterTransition = { slideInHorizontally { it } },
                exitTransition = { slideOutHorizontally { -it } },
                popEnterTransition = { slideInHorizontally { -it } },
                popExitTransition = { slideOutHorizontally { it } },
            ) {
                AdminScreen(onBackClick = { navController.popBackStack() })
            }

            composable(Routes.CREATE_EVENT) {
                CreateEventScreen(
                    onEventCreated = {
                        selectedTab = NavTab.Carte
                        navController.navigate(Routes.MAP) {
                            popUpTo(Routes.CREATE_EVENT) { inclusive = true }
                        }
                        eventCreatedToastKey++
                    },
                )
            }
        }

        if (loginToastKey > 0) {
            Toast(
                title = "Connexion réussie !",
                description = "Bienvenue sur Geo Trouvetou",
                key = loginToastKey,
            )
        }
        if (registerToastKey > 0) {
            Toast(
                title = "Compte créé !",
                description = "Bienvenue sur Geo Trouvetou",
                key = registerToastKey,
            )
        }
        if (deleteAccountToastKey > 0) {
            Toast(
                title = "Compte supprimé",
                description = "Votre compte a bien été supprimé",
                key = deleteAccountToastKey,
            )
        }
        if (logoutToastKey > 0) {
            Toast(
                title = "Déconnexion réussie",
                description = "À bientôt sur Geo Trouvetou",
                key = logoutToastKey,
            )
        }
        if (eventCreatedToastKey > 0) {
            Toast(
                title = "Succès !",
                description = "L'événement a été créé avec succès",
                key = eventCreatedToastKey,
            )
        }

        val hideNavBar = currentRoute in setOf(Routes.PARAMS, Routes.EDIT_PROFILE, Routes.EDIT_PASSWORD, Routes.ADMIN)
        if (!hideNavBar) NavBar(
                selectedTab = selectedTab,
                onTabSelected = { tab ->
                    when (tab) {
                        NavTab.Carte -> {
                            selectedTab = NavTab.Carte
                            navController.navigate(Routes.MAP) {
                                popUpTo(Routes.MAP) { inclusive = false }
                                launchSingleTop = true
                            }
                        }
                        NavTab.Profil -> navigateIfLoggedIn(Routes.PROFILE, NavTab.Profil)
                        NavTab.Ajouter -> navigateIfLoggedIn(Routes.CREATE_EVENT, NavTab.Ajouter)
                    }
                },
                modifier = Modifier
                    .background(colorResource(R.color.white))
                    .navigationBarsPadding(),
            )
    }
}
