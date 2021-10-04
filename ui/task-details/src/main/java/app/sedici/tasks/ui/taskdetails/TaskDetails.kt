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
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import app.sedici.tasks.common.compose.BorderlessTextField
import app.sedici.tasks.common.compose.TaskBottomBar
import app.sedici.tasks.common.compose.collectInLaunchedEffect
import app.sedici.tasks.common.compose.rememberFlowWithLifecycle
import app.sedici.tasks.model.Task
import com.google.android.material.datepicker.MaterialDatePicker
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

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

    BackHandler(onBack = { actioner(TaskDetailsUiAction.NavigateUp) })

    TaskDetails(
        uiState = uiState,
        actioner = actioner,
        scaffoldState = scaffoldState,
    )
}

@Composable
internal fun TaskDetails(
    uiState: TaskDetailsUiState,
    actioner: (TaskDetailsUiAction) -> Unit,
    scaffoldState: ScaffoldState,
) {
    val task = uiState.task

    if (uiState.showConfirmDeleteDialog) {
        DeleteTaskConfirmDialog(
            onDismissClick = {
                actioner(TaskDetailsUiAction.DismissConfirmDeleteDialog)
            },
            onDeleteClick = {
                actioner(TaskDetailsUiAction.DismissConfirmDeleteDialog)
                actioner(TaskDetailsUiAction.Delete)
            }
        )
    }

    val context = LocalContext.current as FragmentActivity

    if (uiState.showExpirationDatePicker && task != null) {
        showExpiresOnDatePicker(
            context = context,
            onDismiss = { actioner(TaskDetailsUiAction.DismissExpirationDatePicker) },
            selectedDate = task.expiresOn,
            onDateChange = {
                actioner(TaskDetailsUiAction.EditExpirationDate(expirationDate = it))
                actioner(TaskDetailsUiAction.DismissExpirationDatePicker)
            }
        )
    }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            Column {
                TaskDetailsAppBar(
                    navigateUp = { actioner(TaskDetailsUiAction.NavigateUp) },
                    deleteTask = {
                        actioner(TaskDetailsUiAction.ShowConfirmDeleteDialog)
                    }
                )
                if (uiState.loading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
        },
        modifier = Modifier.fillMaxSize(),
        content = { contentPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
            ) {
                if (task != null) {
                    TaskDetailsContent(
                        task = task,
                        actioner = actioner,
                    )
                }
            }
        },
        bottomBar = {
            if (task != null) {
                TaskDetailsBottomBar(
                    checked = task.isChecked,
                    onCheckedChange = { checked ->
                        actioner(
                            TaskDetailsUiAction.EditIsChecked(checked = checked)
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
    actioner: (TaskDetailsUiAction) -> Unit,
) {
    val onEditExpirationDateClick = {
        actioner(TaskDetailsUiAction.ShowExpirationDatePicker)
    }
    val onClearExpirationDateClick = {
        actioner(TaskDetailsUiAction.EditExpirationDate(expirationDate = null))
    }

    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            val focusManager = LocalFocusManager.current
            var title by rememberSaveable { mutableStateOf(task.title) }

            BorderlessTextField(
                value = title,
                onValueChange = { newValue ->
                    title = newValue
                    actioner(TaskDetailsUiAction.EditTitle(title = title))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    autoCorrect = true,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                ),
                textStyle = MaterialTheme.typography.h5,
                placeholder = {
                    Text(text = stringResource(R.string.task_details_enter_title_placeholder))
                },
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                    }
                )
            )
        }

        item {
            var description by rememberSaveable { mutableStateOf(task.description) }

            Column {
                NotesField(
                    modifier = Modifier.fillMaxWidth(),
                    description = description,
                    onDescriptionChange = {
                        description = it
                        actioner(TaskDetailsUiAction.EditDescription(description))
                    },
                )
                Divider()
                DateField(
                    modifier = Modifier.fillMaxWidth(),
                    expiresOn = task.expiresOn,
                    onEditClick = onEditExpirationDateClick,
                    onClearClick = onClearExpirationDateClick,
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
private fun NotesField(
    modifier: Modifier = Modifier,
    description: String,
    onDescriptionChange: (String) -> Unit,
) {
    TaskDetailsTextFieldItem(
        modifier = modifier,
        icon = {
            Icon(
                imageVector = Icons.Default.Notes,
                contentDescription = null
            )
        },
        value = description,
        onValueChange = onDescriptionChange,
    )
}

@Composable
private fun DateField(
    modifier: Modifier = Modifier,
    expiresOn: LocalDate?,
    onEditClick: () -> Unit,
    onClearClick: () -> Unit,
) {
    TaskDetailsEditableTextItem(
        modifier = modifier,
        icon = {
            Icon(
                imageVector = Icons.Default.Event,
                contentDescription = null
            )
        },
        text = expiresOn?.toString() ?: "",
        placeholder = stringResource(R.string.task_details_add_date_message),
        onEditClick = onEditClick,
        onClearClick = onClearClick
    )
}

@Composable
private fun TaskDetailsEditableTextItem(
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    text: String,
    placeholder: String = "",
    onEditClick: () -> Unit,
    onClearClick: () -> Unit,
) {
    CompositionLocalProvider(
        LocalContentAlpha provides if (text.isEmpty()) ContentAlpha.medium else ContentAlpha.high,
        LocalTextStyle provides MaterialTheme.typography.body1
    ) {
        TaskDetailsItem(
            modifier = modifier.clickable(onClick = onEditClick),
            icon = icon,
        ) {
            Row(modifier = Modifier.weight(1f)) {
                if (text.isEmpty()) {
                    Text(text = placeholder)
                } else {
                    Text(text = text)
                }
            }
            Row(modifier = Modifier.size(24.dp)) {
                if (text.isEmpty()) {
                    IconButton(
                        onClick = onEditClick,
                        content = {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = stringResource(R.string.cd_edit)
                            )
                        },
                    )
                } else {
                    IconButton(
                        onClick = onClearClick,
                        content = {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = stringResource(R.string.cd_clear)
                            )
                        },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun TaskDetailsTextFieldItem(
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    value: String,
    onValueChange: (String) -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    CompositionLocalProvider(
        LocalContentAlpha provides if (value.isEmpty()) ContentAlpha.medium else ContentAlpha.high
    ) {
        TaskDetailsItem(
            modifier = modifier,
            icon = icon
        ) {
            Row(modifier = Modifier.weight(1f)) {
                BorderlessTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        autoCorrect = true,
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    ),
                    singleLine = false,
                    placeholder = {
                        Text(
                            text = stringResource(
                                R.string.task_details_add_notes_message
                            )
                        )
                    },
                    textStyle = MaterialTheme.typography.body1,
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                        }
                    )
                )
            }
            Row(modifier = Modifier.size(24.dp)) {
                if (value.isEmpty()) {
                    IconButton(
                        onClick = {
                            focusRequester.requestFocus()
                            keyboardController?.show()
                        },
                        content = {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = stringResource(R.string.cd_edit)
                            )
                        },
                    )
                } else {
                    IconButton(
                        onClick = {
                            onValueChange("")
                        },
                        content = {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = stringResource(R.string.cd_clear)
                            )
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun TaskDetailsItem(
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    content: @Composable RowScope.() -> Unit,
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
                icon()
            }
        }
        Row(
            modifier = Modifier.weight(1f),
            content = content,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        )
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

private fun showExpiresOnDatePicker(
    context: FragmentActivity,
    onDismiss: () -> Unit,
    selectedDate: LocalDate?,
    onDateChange: (LocalDate) -> Unit,
) {
    val picker = MaterialDatePicker.Builder.datePicker()
        .setTitleText(R.string.task_details_expires_on_title_date_picker)
        .setSelection(
            selectedDate?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli()
                ?: MaterialDatePicker.todayInUtcMilliseconds()
        )
        .build()

    picker.addOnDismissListener { onDismiss() }

    picker.addOnPositiveButtonClickListener { epochMilli ->
        val newDate = Instant.ofEpochMilli(epochMilli).atZone(ZoneId.systemDefault()).toLocalDate()
        onDateChange(newDate)
    }

    picker.show(context.supportFragmentManager, "ExpiresOnDatePicker")
}
