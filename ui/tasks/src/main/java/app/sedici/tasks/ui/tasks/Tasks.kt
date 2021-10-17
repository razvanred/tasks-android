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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.sedici.tasks.common.compose.TaskBottomBar
import app.sedici.tasks.common.compose.collectInLaunchedEffect
import app.sedici.tasks.common.compose.rememberFlowWithLifecycle
import app.sedici.tasks.model.Task
import app.sedici.tasks.model.TaskId
import app.sedici.tasks.ui.tasks.internal.TasksViewModel
import app.sedici.tasks.ui.tasks.internal.UiAction
import app.sedici.tasks.ui.tasks.internal.UiDestination
import app.sedici.tasks.ui.tasks.internal.UiState

@Composable
fun Tasks(
    openCreateTask: () -> Unit,
    openTaskDetails: (TaskId) -> Unit,
) {
    Tasks(
        viewModel = hiltViewModel(),
        openCreateTask = openCreateTask,
        openTaskDetails = openTaskDetails,
    )
}

@Composable
internal fun Tasks(
    viewModel: TasksViewModel,
    openCreateTask: () -> Unit,
    openTaskDetails: (TaskId) -> Unit,
) {
    val uiState by rememberFlowWithLifecycle(flow = viewModel.uiState)
        .collectAsState(initial = UiState.Empty)

    val pendingUiDestinationFlow = rememberFlowWithLifecycle(flow = viewModel.pendingUiDestination)

    val actioner = viewModel::submitUiAction

    pendingUiDestinationFlow.collectInLaunchedEffect { destination ->
        when (destination) {
            is UiDestination.TaskDetails -> openTaskDetails(destination.taskId)
        }
    }

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
        content = { contentPadding ->
            TasksContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding),
                tasks = uiState.tasks,
                actioner = actioner,
            )
        },
        bottomBar = {
            TasksBottomBar(
                modifier = Modifier.fillMaxWidth(),
                openCreateTask = openCreateTask
            )
        }
    )
}

@Composable
private fun TasksContent(
    modifier: Modifier = Modifier,
    tasks: List<Task>,
    actioner: (UiAction) -> Unit,
) {
    if (tasks.isEmpty()) {
        AllTasksDone(
            modifier = modifier
        )
    } else {
        TaskList(
            modifier = modifier,
            tasks = tasks,
            actioner = actioner
        )
    }
}

@Composable
private fun TaskList(
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
                modifier = Modifier
                    .clickable(
                        onClick = {
                            actioner(UiAction.ShowTaskDetails(taskId = task.id))
                        }
                    )
                    .fillMaxWidth(),
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
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                if (task.description.isNotBlank()) {
                    Text(text = task.description, style = MaterialTheme.typography.body2)
                }
            }
        }
    }
}

@Composable
private fun TasksBottomBar(
    modifier: Modifier = Modifier,
    openCreateTask: () -> Unit,
) {
    TaskBottomBar(modifier = modifier) {
        Button(
            modifier = Modifier.fillMaxWidth(),
            content = {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = stringResource(R.string.tasks_screen_button_add_task))
            },
            onClick = openCreateTask
        )
    }
}

@Composable
private fun AllTasksDone(
    modifier: Modifier = Modifier
) {
    LazyColumn(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                CompositionLocalProvider(
                    LocalContentColor provides MaterialTheme.colors.primary
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp)
                    )

                    Text(
                        text = stringResource(R.string.tasks_screen_all_tasks_done_title),
                        style = MaterialTheme.typography.h4
                    )
                }

                CompositionLocalProvider(
                    LocalContentAlpha provides ContentAlpha.medium
                ) {
                    Text(
                        text = stringResource(R.string.tasks_screen_all_tasks_done_message),
                        style = MaterialTheme.typography.subtitle2,
                    )
                }
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
