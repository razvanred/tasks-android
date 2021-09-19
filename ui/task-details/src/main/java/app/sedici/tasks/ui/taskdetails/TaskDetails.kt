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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.sedici.tasks.common.compose.TaskBottomBar
import app.sedici.tasks.common.compose.collectInLaunchedEffect
import app.sedici.tasks.common.compose.rememberFlowWithLifecycle
import app.sedici.tasks.model.Task
import java.time.LocalDate

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
    val uiState by rememberFlowWithLifecycle(flow = viewModel.uiState)
        .collectAsState(initial = TaskDetailsUiState.Empty)

    val pendingUiDestinationFlow = rememberFlowWithLifecycle(flow = viewModel.pendingUiDestination)
    val pendingSnackbarErrorFlow = rememberFlowWithLifecycle(flow = viewModel.pendingSnackbarError)

    val errorWhileDeletingMessage =
        stringResource(R.string.task_details_error_while_deleting_message)
    val unknownErrorMessage = stringResource(R.string.task_details_unknown_error_message)

    val scaffoldState = rememberScaffoldState()

    pendingSnackbarErrorFlow.collectInLaunchedEffect { error ->
        when (error) {
            TaskDetailsSnackbarError.UnknownError -> {
                scaffoldState.snackbarHostState.showSnackbar(
                    message = unknownErrorMessage,
                    duration = SnackbarDuration.Short
                )
            }
            TaskDetailsSnackbarError.ErrorWhileDeleting -> {
                scaffoldState.snackbarHostState.showSnackbar(
                    message = errorWhileDeletingMessage,
                    duration = SnackbarDuration.Short
                )
            }
        }
    }

    val actioner = viewModel::submitUiAction

    pendingUiDestinationFlow.collectInLaunchedEffect { destination ->
        when (destination) {
            TaskDetailsDestination.Up -> onBack()
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
                val task = uiState.task

                if (task != null) {
                    TaskDetailsContent(
                        task = task,
                        actioner = actioner,
                    )
                }
            }
        },
        bottomBar = {
            val task = uiState.task

            if (task != null) {
                TaskDetailsBottomBar(
                    checked = task.isChecked,
                    onCheckedChange = { checked ->
                        actioner(
                            TaskDetailsAction.EditTaskIsChecked(checked = checked)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    )
}

@Composable
private fun TaskDetailsContent(
    modifier: Modifier = Modifier,
    task: Task,
    actioner: (TaskDetailsAction) -> Unit,
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            Text(
                text = task.title,
                style = MaterialTheme.typography.h5,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        item {
            Column {
                TaskDescriptionItem(
                    modifier = Modifier
                        .clickable(onClick = {})
                        .fillMaxWidth(),
                    description = task.description,
                )
                Divider()
                TaskExpirationDateItem(
                    modifier = Modifier
                        .clickable(onClick = {})
                        .fillMaxWidth(),
                    expiresOn = task.expiresOn
                )
                Divider()
            }
        }

        item {
            Spacer(Modifier)
        }
    }
}

@Composable
private fun TaskDescriptionItem(
    modifier: Modifier = Modifier,
    description: String
) {
    TaskDetailsItem(
        modifier = modifier,
        icon = {
            Icon(
                imageVector = Icons.Default.Notes,
                contentDescription = null
            )
        },
        text = {
            Text(
                text = description.ifBlank {
                    stringResource(R.string.task_details_no_description_provided_message)
                }
            )
        },
        contentAlpha = if (description.isBlank()) {
            ContentAlpha.medium
        } else {
            ContentAlpha.high
        }
    )
}

@Composable
private fun TaskExpirationDateItem(
    modifier: Modifier = Modifier,
    expiresOn: LocalDate?,
) {
    TaskDetailsItem(
        modifier = modifier,
        icon = {
            Icon(
                imageVector = Icons.Default.Event,
                contentDescription = null
            )
        },
        text = {
            Text(
                text = expiresOn?.toString()
                    ?: stringResource(R.string.task_details_no_expiration_date_provided_message)
            )
        },
        contentAlpha = if (expiresOn == null) {
            ContentAlpha.medium
        } else {
            ContentAlpha.high
        }
    )
}

@Composable
private fun TaskDetailsItem(
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    text: @Composable () -> Unit,
    contentAlpha: Float,
) {
    Row(
        modifier = modifier.padding(vertical = 16.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (icon != null) {
            Row(
                modifier = Modifier.size(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CompositionLocalProvider(
                    LocalContentAlpha provides contentAlpha,
                    content = icon
                )
            }
        }
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProvideTextStyle(value = MaterialTheme.typography.body1) {
                CompositionLocalProvider(
                    LocalContentAlpha provides contentAlpha,
                    content = text
                )
            }
        }
    }
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

@Composable
private fun TaskDetailsBottomBar(
    modifier: Modifier = Modifier,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    TaskBottomBar(modifier = modifier) {
        if (checked) {
            MarkAsNotCompletedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onCheckedChange(false) }
            )
        } else {
            MarkAsCompletedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onCheckedChange(true) }
            )
        }
    }
}

@Composable
private fun MarkAsCompletedButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Button(
        modifier = modifier,
        content = {
            Text(text = stringResource(R.string.task_details_mark_task_as_completed_button))
        },
        onClick = onClick
    )
}

@Composable
private fun MarkAsNotCompletedButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    OutlinedButton(
        modifier = modifier,
        content = {
            Text(text = stringResource(R.string.task_details_mark_task_as_not_completed_button))
        },
        onClick = onClick
    )
}
