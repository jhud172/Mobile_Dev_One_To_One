package uk.ac.cardiff.trainerhub.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import uk.ac.cardiff.trainerhub.data.reminders.ReminderScheduler
import uk.ac.cardiff.trainerhub.data.repository.TrainerHubRepository
import uk.ac.cardiff.trainerhub.ui.clients.AddClientScreen
import uk.ac.cardiff.trainerhub.ui.clients.ClientDetailScreen
import uk.ac.cardiff.trainerhub.ui.clients.ClientsScreen
import uk.ac.cardiff.trainerhub.ui.clients.InvoiceEditorScreen
import uk.ac.cardiff.trainerhub.ui.clients.PlanEditorScreen
import uk.ac.cardiff.trainerhub.ui.clients.SessionEditorScreen
import uk.ac.cardiff.trainerhub.ui.dashboard.DashboardScreen
import uk.ac.cardiff.trainerhub.ui.profile.TrainerProfileScreen
import uk.ac.cardiff.trainerhub.ui.settings.SettingsScreen

private data class TopLevelDestination(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
)

private val topLevelDestinations = listOf(
    TopLevelDestination("dashboard", "Dashboard", Icons.Outlined.Home),
    TopLevelDestination("clients", "Clients", Icons.Outlined.People),
    TopLevelDestination("settings", "Settings", Icons.Outlined.Settings),
)

@Composable
fun TrainerHubApp(
    repository: TrainerHubRepository,
    reminderScheduler: ReminderScheduler,
) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val remindersEnabled by repository.remindersEnabled.collectAsStateWithLifecycle(initialValue = true)
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    LaunchedEffect(Unit) {
        repository.ensureSeeded()
    }

    LaunchedEffect(remindersEnabled) {
        reminderScheduler.update(remindersEnabled)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            val route = currentDestination?.route ?: ""
            val showBottomBar = route == "dashboard" || route == "clients" || route == "settings"

            if (showBottomBar) {
                NavigationBar {
                    for (destination in topLevelDestinations) {
                        val selected = currentDestination?.hierarchy?.any { it.route == destination.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(destination.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = destination.icon,
                                    contentDescription = destination.label,
                                )
                            },
                            label = { Text(destination.label) },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "dashboard",
        ) {
            composable("dashboard") {
                DashboardScreen(
                    repository = repository,
                    snackbarHostState = snackbarHostState,
                    contentPadding = innerPadding,
                    onOpenTrainerProfile = {
                        navController.navigate("trainer-profile")
                    },
                )
            }

            composable("clients") {
                ClientsScreen(
                    repository = repository,
                    snackbarHostState = snackbarHostState,
                    contentPadding = innerPadding,
                    onOpenClient = { clientId ->
                        navController.navigate("client/$clientId")
                    },
                    onAddClient = {
                        navController.navigate("add-client")
                    },
                )
            }

            composable(
                route = "client/{clientId}",
                arguments = listOf(
                    navArgument("clientId") {
                        type = NavType.StringType
                    },
                ),
            ) { backStackEntry ->
                ClientDetailScreen(
                    repository = repository,
                    snackbarHostState = snackbarHostState,
                    contentPadding = innerPadding,
                    clientId = backStackEntry.arguments?.getString("clientId").orEmpty(),
                    onBack = { navController.popBackStack() },
                    onCreatePlan = { selectedClientId ->
                        navController.navigate("create-plan/$selectedClientId")
                    },
                    onCreateSession = { selectedClientId ->
                        navController.navigate("create-session/$selectedClientId")
                    },
                    onCreateInvoice = { selectedClientId ->
                        navController.navigate("create-invoice/$selectedClientId")
                    },
                )
            }

            composable("add-client") {
                AddClientScreen(
                    repository = repository,
                    snackbarHostState = snackbarHostState,
                    contentPadding = innerPadding,
                    onBack = { navController.popBackStack() },
                    onSaved = {
                        navController.popBackStack()
                    },
                )
            }

            composable("settings") {
                SettingsScreen(
                    repository = repository,
                    snackbarHostState = snackbarHostState,
                    contentPadding = innerPadding,
                )
            }

            composable(
                route = "create-plan/{clientId}",
                arguments = listOf(
                    navArgument("clientId") {
                        type = NavType.StringType
                    },
                ),
            ) { backStackEntry ->
                PlanEditorScreen(
                    repository = repository,
                    snackbarHostState = snackbarHostState,
                    contentPadding = innerPadding,
                    clientId = backStackEntry.arguments?.getString("clientId").orEmpty(),
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() },
                )
            }

            composable(
                route = "create-session/{clientId}",
                arguments = listOf(
                    navArgument("clientId") {
                        type = NavType.StringType
                    },
                ),
            ) { backStackEntry ->
                SessionEditorScreen(
                    repository = repository,
                    snackbarHostState = snackbarHostState,
                    contentPadding = innerPadding,
                    clientId = backStackEntry.arguments?.getString("clientId").orEmpty(),
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() },
                )
            }

            composable(
                route = "create-invoice/{clientId}",
                arguments = listOf(
                    navArgument("clientId") {
                        type = NavType.StringType
                    },
                ),
            ) { backStackEntry ->
                InvoiceEditorScreen(
                    repository = repository,
                    snackbarHostState = snackbarHostState,
                    contentPadding = innerPadding,
                    clientId = backStackEntry.arguments?.getString("clientId").orEmpty(),
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() },
                )
            }

            composable("trainer-profile") {
                TrainerProfileScreen(
                    repository = repository,
                    contentPadding = innerPadding,
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}
