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

package app.sedici.tasks.ui.tasks

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExtendedFloatingActionButton
import androidx.compose.material.FabPosition
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import app.sedici.tasks.ui.tasks.internal.TasksViewModel

@Composable
fun Tasks(
    openCreateTask: () -> Unit,
) {
    Tasks(
        viewModel = hiltViewModel(),
        openCreateTask = openCreateTask,
    )
}

@Composable
internal fun Tasks(
    viewModel: TasksViewModel,
    openCreateTask: () -> Unit,
) {
    Scaffold(
        topBar = {
            TasksAppBar()
        },
        modifier = Modifier.fillMaxSize(),
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = {
                    Text(text = stringResource(R.string.tasks_screen_button_add_task))
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null
                    )
                },
                onClick = {
                    openCreateTask()
                }
            )
        },
        content = {
            Box(modifier = Modifier.fillMaxSize()) {
                Text(text = "Hello from ${stringResource(R.string.tasks_screen_title)}!")
            }
        }
    )
}

@Composable
private fun TasksAppBar(
    modifier: Modifier = Modifier
) {
    TopAppBar(
        modifier = modifier,
        title = { Text(text = stringResource(R.string.tasks_screen_title)) },
    )
}

@Preview
@Composable
private fun TasksAppBarPreview() {
    TasksAppBar()
}
