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

package app.sedici.tasks.ui.createtask

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.flowWithLifecycle
import app.sedici.tasks.common.compose.ConfirmDiscardChangesDialog
import app.sedici.tasks.ui.createtask.internal.CreateTaskViewModel
import app.sedici.tasks.ui.createtask.internal.SnackbarError
import app.sedici.tasks.ui.createtask.internal.UiAction
import app.sedici.tasks.ui.createtask.internal.UiDestination
import app.sedici.tasks.ui.createtask.internal.UiState
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.flow.collect
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

@Composable
fun CreateTask(
    onBack: () -> Unit,
) {
    CreateTask(
        viewModel = hiltViewModel(),
        onBack = onBack,
    )
}

@Composable
internal fun CreateTask(
    viewModel: CreateTaskViewModel,
    onBack: () -> Unit,
) {
    val uiStateFlow = viewModel.uiState
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val uiStateFlowLifecycleAware = remember(uiStateFlow, lifecycle) {
        uiStateFlow.flowWithLifecycle(lifecycle)
    }

    val uiState by uiStateFlowLifecycleAware.collectAsState(initial = UiState.Empty)

    val actioner = viewModel::submitUiAction

    val pendingUiDestinationFlow = viewModel.pendingUiDestination
    val pendingUiDestinationFlowLifecycleAware = remember(pendingUiDestinationFlow, lifecycle) {
        pendingUiDestinationFlow.flowWithLifecycle(lifecycle)
    }

    val pendingSnackbarErrorFlow = viewModel.pendingSnackbarError
    val pendingSnackbarErrorFlowLifecycleAware = remember(pendingSnackbarErrorFlow, lifecycle) {
        pendingSnackbarErrorFlow.flowWithLifecycle(lifecycle)
    }

    val scaffoldState = rememberScaffoldState()

    LaunchedEffect(scaffoldState.snackbarHostState) {
        pendingSnackbarErrorFlowLifecycleAware.collect { error ->
            scaffoldState.snackbarHostState.showSnackbar(
                message = when (error) {
                    SnackbarError.ErrorWhileSaving -> "error while saving"
                },
                duration = SnackbarDuration.Short
            )
        }
    }

    LaunchedEffect(lifecycle) {
        pendingUiDestinationFlowLifecycleAware.collect { destination ->
            when (destination) {
                UiDestination.Up -> onBack()
            }
        }
    }

    BackHandler(onBack = { actioner(UiAction.NavigateUp) })

    CreateTask(
        uiState = uiState,
        actioner = actioner,
        scaffoldState = scaffoldState,
    )
}

@Composable
internal fun CreateTask(
    uiState: UiState,
    actioner: (UiAction) -> Unit,
    scaffoldState: ScaffoldState,
) {
    val context = LocalContext.current as FragmentActivity

    if (uiState.showExpirationDatePicker) {
        showExpiresOnDatePicker(
            context = context,
            onDismiss = { actioner(UiAction.DismissExpirationDatePicker) },
            selectedDate = uiState.expiresOn,
            onDateChange = { actioner(UiAction.SetExpirationDate(newValue = it)) }
        )
    }

    if (uiState.showConfirmDiscardChangesDialog) {
        ConfirmDiscardChangesDialog(
            onDiscardClick = { actioner(UiAction.ConfirmDiscardChanges) },
            onDismissClick = { actioner(UiAction.CancelDiscardChanges) }
        )
    }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            CreateTaskAppBar(
                navigateUp = { actioner(UiAction.NavigateUp) },
                saveTask = {
                    actioner(UiAction.SaveTask)
                }
            )
        },
        modifier = Modifier.fillMaxSize(),
        content = { contentPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
            ) {
                CreateTaskContent(
                    title = uiState.title,
                    description = uiState.description,
                    expiresOn = uiState.expiresOn,
                    actioner = actioner,
                )
            }
        },
    )
}

@Composable
internal fun CreateTaskContent(
    modifier: Modifier = Modifier,
    title: TextFieldValue,
    description: TextFieldValue,
    expiresOn: LocalDate?,
    actioner: (UiAction) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val titleFocusRequester = remember { FocusRequester() }
    val descriptionFocusRequester = remember { FocusRequester() }

    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(vertical = 16.dp, horizontal = 16.dp)
    ) {
        item {
            OutlinedTextField(
                value = title,
                onValueChange = {
                    actioner(UiAction.EditTitle(newValue = it))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(titleFocusRequester),
                label = {
                    Text(text = stringResource(R.string.create_task_title_field_placeholder))
                },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    autoCorrect = true,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next,
                ),
                keyboardActions = KeyboardActions(
                    onNext = { descriptionFocusRequester.requestFocus() }
                ),
            )
        }

        item {
            OutlinedTextField(
                value = description,
                onValueChange = {
                    actioner(UiAction.EditDescription(newValue = it))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(descriptionFocusRequester),
                label = {
                    Text(
                        text = stringResource(R.string.create_task_description_field_placeholder)
                    )
                },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    autoCorrect = true,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                ),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Notes,
                        contentDescription = null
                    )
                }
            )
        }

        item {
            ExpirationDateCard(
                date = expiresOn,
                changeDate = { actioner(UiAction.OpenExpirationDatePicker) },
                clearDate = { actioner(UiAction.ClearExpirationDate) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun CreateTaskAppBar(
    modifier: Modifier = Modifier,
    navigateUp: () -> Unit,
    saveTask: () -> Unit,
) {
    TopAppBar(
        modifier = modifier,
        title = { Text(text = stringResource(R.string.create_task_screen_title)) },
        navigationIcon = {
            IconButton(
                content = {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.cd_cancel)
                    )
                },
                onClick = navigateUp
            )
        },
        actions = {
            IconButton(
                content = {
                    Icon(
                        imageVector = Icons.Default.Done,
                        contentDescription = stringResource(R.string.cd_save)
                    )
                },
                onClick = saveTask
            )
        }
    )
}

private fun showExpiresOnDatePicker(
    context: FragmentActivity,
    onDismiss: () -> Unit,
    selectedDate: LocalDate?,
    onDateChange: (LocalDate) -> Unit,
) {
    val picker = MaterialDatePicker.Builder.datePicker()
        .setTitleText(R.string.create_task_expires_on_title_date_picker)
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

@Composable
private fun ExpirationDateCard(
    modifier: Modifier = Modifier,
    date: LocalDate?,
    changeDate: () -> Unit,
    clearDate: () -> Unit,
) {
    Card {
        Row(
            modifier = modifier.padding(all = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = stringResource(R.string.create_task_expires_on_field_title),
                    fontWeight = FontWeight.Bold,
                )

                if (date == null) {
                    Text(
                        text = stringResource(R.string.create_task_expires_on_field_no_value_label)
                    )
                } else {
                    Text(
                        text = date.toString() // TODO format date with formatter when available
                    )
                }
            }

            if (date == null) {
                IconButton(
                    content = {
                        Icon(
                            imageVector = Icons.Default.Event,
                            contentDescription = stringResource(R.string.cd_select_date),
                            tint = MaterialTheme.colors.primary
                        )
                    },
                    onClick = changeDate
                )
            } else {
                IconButton(
                    content = {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = stringResource(R.string.cd_clear)
                        )
                    },
                    onClick = clearDate
                )
            }
        }
    }
}

@Preview
@Composable
private fun CreateTaskAppBarPreview() {
    CreateTaskAppBar(
        navigateUp = {},
        saveTask = {},
    )
}
