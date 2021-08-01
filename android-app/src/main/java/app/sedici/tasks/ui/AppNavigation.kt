package app.sedici.tasks.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import app.sedici.tasks.ui.stats.Stats
import app.sedici.tasks.ui.tasks.Tasks

internal sealed class Screen(val route: String) {
    object Tasks : Screen("tasksroot")
    object Stats : Screen("statsroot")
}

private sealed class LeafScreen(val route: String) {
    object Tasks : LeafScreen("tasks")
    object Stats : LeafScreen("stats")
}

@Composable
internal fun AppNavigation(
    navController: NavHostController,
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Tasks.route
    ) {
        addTasksTopLevel(navController = navController)
        addStatsTopLevel(navController = navController)
    }
}

private fun NavGraphBuilder.addTasksTopLevel(
    navController: NavController,
) {
    navigation(
        route = Screen.Tasks.route,
        startDestination = LeafScreen.Tasks.route
    ) {
        addTasks()
    }
}

private fun NavGraphBuilder.addTasks() {
    composable(route = LeafScreen.Tasks.route) {
        Tasks()
    }
}

private fun NavGraphBuilder.addStatsTopLevel(
    navController: NavController,
) {
    navigation(
        route = Screen.Stats.route,
        startDestination = LeafScreen.Stats.route
    ) {
        addStats()
    }
}

private fun NavGraphBuilder.addStats() {
    composable(route = LeafScreen.Stats.route) {
        Stats()
    }
}
