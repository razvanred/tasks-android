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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Checkbox
import androidx.compose.material.Divider
import androidx.compose.material.ExtendedFloatingActionButton
import androidx.compose.material.FabPosition
import androidx.compose.material.Icon
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.flowWithLifecycle
import app.sedici.tasks.model.Task
import app.sedici.tasks.ui.tasks.internal.TasksViewModel
import app.sedici.tasks.ui.tasks.internal.UiAction
import app.sedici.tasks.ui.tasks.internal.UiState

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
    openCreateTask: () -> Unit
) {
    val uiStateFlow = viewModel.uiState
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val uiStateFlowLifecycleAware = remember(uiStateFlow, lifecycle) {
        uiStateFlow.flowWithLifecycle(lifecycle)
    }

    val uiState by uiStateFlowLifecycleAware.collectAsState(initial = UiState.Empty)

    val actioner = viewModel::submitUiAction

    Tasks(
        uiState = uiState,
        actioner = actioner,
        openCreateTask = openCreateTask,
    )
}

@Composable
internal fun Tasks(
    uiState: UiState,
    actioner: (UiAction) -> Unit,
    openCreateTask: () -> Unit,
) {
    Scaffold(
        topBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                TasksAppBar()
                if (uiState.loading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
        },
        modifier = Modifier.fillMaxSize(),
        floatingActionButtonPosition = FabPosition.Center,
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
        content = { contentPadding ->
            Tasks(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding),
                tasks = uiState.tasks,
                actioner = actioner,
            )
        }
    )
}

@Composable
internal fun Tasks(
    modifier: Modifier = Modifier,
    tasks: List<Task>,
    actioner: (UiAction) -> Unit,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(bottom = 128.dp)
    ) {
        items(tasks) { task ->
            Task(
                modifier = Modifier.fillMaxWidth(),
                task = task,
                onCheckedChange = { checked ->
                    actioner(
                        UiAction.EditTaskIsChecked(
                            taskId = task.id,
                            checked = checked
                        )
                    )
                }
            )
            Divider()
        }
    }
}

@Composable
internal fun Task(
    modifier: Modifier = Modifier,
    task: Task,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = modifier.padding(all = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Checkbox(
            checked = task.isChecked,
            onCheckedChange = onCheckedChange,
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.weight(1f)
        ) {
            Text(text = task.title)
            if (task.description.isNotBlank()) {
                Text(text = task.description, style = MaterialTheme.typography.body2)
            }
        }
    }
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
