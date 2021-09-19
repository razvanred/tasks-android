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

package app.sedici.tasks.ui.taskdetails

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.flowWithLifecycle
import kotlinx.coroutines.flow.collect

@Composable
fun TaskDetails(
    onBack: () -> Unit,
) {
    TaskDetails(
        onBack = onBack,
        viewModel = hiltViewModel()
    )
}

@Composable
internal fun TaskDetails(
    onBack: () -> Unit,
    viewModel: TaskDetailsViewModel,
) {
    val uiStateFlow = viewModel.uiState
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val uiStateFlowLifecycleAware = remember(uiStateFlow, lifecycle) {
        uiStateFlow.flowWithLifecycle(lifecycle)
    }
    val uiState by uiStateFlowLifecycleAware.collectAsState(initial = TaskDetailsUiState.Empty)

    val pendingUiDestinationFlow = viewModel.pendingUiDestination
    val pendingUiDestinationFlowLifecycleAware = remember(pendingUiDestinationFlow, lifecycle) {
        pendingUiDestinationFlow.flowWithLifecycle(lifecycle)
    }

    val pendingSnackbarErrorFlow = viewModel.pendingSnackbarError
    val pendingSnackbarErrorFlowLifecycleAware = remember(pendingSnackbarErrorFlow, lifecycle) {
        pendingSnackbarErrorFlow.flowWithLifecycle(lifecycle)
    }

    val errorWhileDeletingMessage =
        stringResource(R.string.task_details_error_while_deleting_message)

    val scaffoldState = rememberScaffoldState()

    LaunchedEffect(scaffoldState.snackbarHostState) {
        pendingSnackbarErrorFlowLifecycleAware.collect { error ->
            scaffoldState.snackbarHostState.showSnackbar(
                message = when (error) {
                    TaskDetailsSnackbarError.ErrorWhileDeleting -> errorWhileDeletingMessage
                },
                duration = SnackbarDuration.Short
            )
        }
    }

    val actioner = viewModel::submitUiAction

    LaunchedEffect(lifecycle) {
        pendingUiDestinationFlowLifecycleAware.collect { destination ->
            when (destination) {
                TaskDetailsDestination.Up -> onBack()
            }
        }
    }

    BackHandler(onBack = { actioner(TaskDetailsAction.NavigateUp) })

    TaskDetails(
        uiState = uiState,
        actioner = actioner,
        scaffoldState = scaffoldState,
    )
}

@Composable
internal fun TaskDetails(
    uiState: TaskDetailsUiState,
    actioner: (TaskDetailsAction) -> Unit,
    scaffoldState: ScaffoldState,
) {
    if (uiState.showDeleteTaskConfirmDialog) {
        DeleteTaskConfirmDialog(
            onDismissClick = {
                actioner(TaskDetailsAction.AnswerConfirmDeleteTask.Cancel)
            },
            onDeleteClick = {
                actioner(TaskDetailsAction.AnswerConfirmDeleteTask.Delete)
            }
        )
    }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TaskDetailsAppBar(
                navigateUp = { actioner(TaskDetailsAction.NavigateUp) },
                deleteTask = {
                    actioner(TaskDetailsAction.DeleteTask)
                }
            )
            if (uiState.loading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        },
        modifier = Modifier.fillMaxSize(),
        content = { contentPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
            ) {
                Text("Hello ${stringResource(R.string.task_details_screen_title)}!")
            }
        }
    )
}

@Composable
private fun TaskDetailsAppBar(
    modifier: Modifier = Modifier,
    navigateUp: () -> Unit,
    deleteTask: () -> Unit,
) {
    TopAppBar(
        modifier = modifier,
        title = { Text(text = stringResource(R.string.task_details_screen_title)) },
        navigationIcon = {
            IconButton(
                content = {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = stringResource(R.string.cd_navigate_up)
                    )
                },
                onClick = navigateUp
            )
        },
        actions = {
            IconButton(
                content = {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.cd_delete)
                    )
                },
                onClick = deleteTask
            )
        }
    )
}

@Composable
private fun DeleteTaskConfirmDialog(
    onDeleteClick: () -> Unit,
    onDismissClick: () -> Unit,
) {
    AlertDialog(
        title = {
            Text(text = stringResource(R.string.task_details_delete_task_confirm_dialog_title))
        },
        text = {
            Text(text = stringResource(R.string.task_details_delete_task_confirm_dialog_message))
        },
        onDismissRequest = onDismissClick,
        dismissButton = {
            TextButton(onClick = onDismissClick) {
                Text(text = stringResource(R.string.task_details_delete_task_confirm_dialog_cancel_button))
            }
        },
        confirmButton = {
            Button(onClick = onDeleteClick) {
                Text(text = stringResource(R.string.task_details_delete_task_confirm_dialog_delete_button))
            }
        }
    )
}
