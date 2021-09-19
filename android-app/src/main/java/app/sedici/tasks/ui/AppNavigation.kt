/*
 * Copyright 2021 Răzvan Roșu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.sedici.tasks.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navArgument
import androidx.navigation.navigation
import app.sedici.tasks.model.TaskId
import app.sedici.tasks.ui.createtask.CreateTask
import app.sedici.tasks.ui.taskdetails.TaskDetails
import app.sedici.tasks.ui.tasks.Tasks

internal sealed class Screen(val route: String) {
    object Tasks : Screen("tasks")
}

private sealed class LeafScreen(private val route: String) {
    fun createRoute(root: Screen) = "${root.route}/$route"

    object CreateTask : LeafScreen("create_task")
    object Tasks : LeafScreen("tasks")

    object TaskDetails : LeafScreen("task/{taskId}") {
        fun createRoute(root: Screen, taskId: TaskId): String =
            "${root.route}/task/$taskId"
    }
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
    }
}

private fun NavGraphBuilder.addTasksTopLevel(navController: NavController) {
    navigation(
        route = Screen.Tasks.route,
        startDestination = LeafScreen.Tasks.createRoute(Screen.Tasks)
    ) {
        addTasks(navController = navController, root = Screen.Tasks)
        addCreateTask(navController = navController, root = Screen.Tasks)
        addTaskDetails(navController = navController, root = Screen.Tasks)
    }
}

private fun NavGraphBuilder.addTasks(
    navController: NavController,
    root: Screen,
) {
    composable(route = LeafScreen.Tasks.createRoute(root)) {
        Tasks(
            openCreateTask = {
                navController.navigate(LeafScreen.CreateTask.createRoute(root))
            },
            openTaskDetails = { taskId ->
                navController.navigate(
                    LeafScreen.TaskDetails.createRoute(
                        root = root,
                        taskId = taskId
                    )
                )
            }
        )
    }
}

private fun NavGraphBuilder.addCreateTask(
    navController: NavController,
    root: Screen,
) {
    composable(route = LeafScreen.CreateTask.createRoute(root)) {
        CreateTask(onBack = navController::navigateUp)
    }
}

private fun NavGraphBuilder.addTaskDetails(
    navController: NavController,
    root: Screen,
) {
    composable(
        route = LeafScreen.TaskDetails.createRoute(root),
        arguments = listOf(
            navArgument("taskId") {
                type = NavType.StringType
            }
        )
    ) {
        TaskDetails(
            onBack = navController::navigateUp
        )
    }
}
