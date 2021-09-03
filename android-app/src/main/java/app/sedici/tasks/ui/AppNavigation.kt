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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import app.sedici.tasks.ui.createtask.CreateTask
import app.sedici.tasks.ui.tasks.Tasks

internal sealed class Screen(val route: String) {
    object Tasks : Screen("tasks")
    object CreateTask : Screen("new_task")
}

@Composable
internal fun AppNavigation(
    navController: NavHostController,
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Tasks.route
    ) {
        addTasks(navController = navController)
        addCreateTask(navController = navController)
    }
}

private fun NavGraphBuilder.addTasks(
    navController: NavController,
) {
    composable(route = Screen.Tasks.route) {
        Tasks(
            openCreateTask = {
                navController.navigate(Screen.CreateTask.route)
            }
        )
    }
}

private fun NavGraphBuilder.addCreateTask(
    navController: NavController,
) {
    composable(route = Screen.CreateTask.route) {
        CreateTask(onBack = navController::navigateUp)
    }
}
