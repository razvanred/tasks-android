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

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.sedici.tasks.base.android.ui.ObservableLoadingCounter
import app.sedici.tasks.base.common.InvokeError
import app.sedici.tasks.base.common.InvokeStarted
import app.sedici.tasks.base.common.InvokeSuccess
import app.sedici.tasks.domain.DeleteTaskById
import app.sedici.tasks.domain.ObserveTaskById
import app.sedici.tasks.domain.SetTaskDescriptionById
import app.sedici.tasks.domain.SetTaskExpirationDateById
import app.sedici.tasks.domain.SetTaskIsCheckedById
import app.sedici.tasks.domain.SetTaskTitleById
import app.sedici.tasks.model.TaskId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@OptIn(FlowPreview::class)
class TaskDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeTaskById: ObserveTaskById,
    private val deleteTaskById: DeleteTaskById,
    private val setTaskIsCheckedById: SetTaskIsCheckedById,
    private val setTaskDescriptionById: SetTaskDescriptionById,
    private val setTaskExpirationDateById: SetTaskExpirationDateById,
    private val setTaskTitleById: SetTaskTitleById,
) : ViewModel() {
    private val taskId = TaskId(value = savedStateHandle.get<String>("taskId")!!)

    private val showConfirmDeleteDialog = MutableStateFlow(false)
    private val showEditDescriptionDialog = MutableStateFlow(false)
    private val showExpirationDatePicker = MutableStateFlow(false)

    private val pendingUiAction = MutableSharedFlow<TaskDetailsUiAction>()

    private val _pendingUiDestination = MutableSharedFlow<TaskDetailsDestination>()
    val pendingUiDestination = _pendingUiDestination.asSharedFlow()

    private val loadingState = ObservableLoadingCounter()

    private val _pendingSnackbarError = MutableSharedFlow<TaskDetailsSnackbarError>()
    val pendingSnackbarError = _pendingSnackbarError.asSharedFlow()

    val uiState = combine(
        observeTaskById.flow,
        showConfirmDeleteDialog,
        loadingState.observable,
        showEditDescriptionDialog,
        showExpirationDatePicker,
    ) { task, showConfirmDeleteDialog, loading, showEditDescriptionDialog, showExpirationDatePicker ->
        TaskDetailsUiState(
            showConfirmDeleteDialog = showConfirmDeleteDialog,
            loading = loading,
            task = task,
            showEditDescriptionDialog = showEditDescriptionDialog,
            showExpirationDatePicker = showExpirationDatePicker,
        )
    }

    init {
        viewModelScope.launch {
            pendingUiAction.collect(::handleUiAction)
        }

        observeTaskById(id = taskId)
    }

    private suspend fun handleUiAction(uiAction: TaskDetailsUiAction) {
        when (uiAction) {
            TaskDetailsUiAction.ShowConfirmDeleteDialog -> {
                showConfirmDeleteDialog.emit(true)
            }
            TaskDetailsUiAction.Delete -> {
                deleteTaskById(taskId = taskId).collect { status ->
                    when (status) {
                        InvokeStarted -> loadingState.addLoader()
                        InvokeSuccess -> {
                            loadingState.removeLoader()
                            _pendingUiDestination.emit(TaskDetailsDestination.Up)
                        }
                        is InvokeError -> {
                            _pendingSnackbarError.emit(TaskDetailsSnackbarError.ErrorWhileDeleting)
                            loadingState.removeLoader()
                        }
                    }
                }
            }
            TaskDetailsUiAction.DismissConfirmDeleteDialog -> {
                showConfirmDeleteDialog.emit(false)
            }
            TaskDetailsUiAction.ShowEditDescriptionDialog -> {
                showEditDescriptionDialog.emit(true)
            }
            TaskDetailsUiAction.DismissEditDescriptionDialog -> {
                showEditDescriptionDialog.emit(false)
            }
            TaskDetailsUiAction.NavigateUp -> {
                _pendingUiDestination.emit(TaskDetailsDestination.Up)
            }
            TaskDetailsUiAction.ShowExpirationDatePicker -> {
                showExpirationDatePicker.emit(true)
            }
            TaskDetailsUiAction.DismissExpirationDatePicker -> {
                showExpirationDatePicker.emit(false)
            }
            is TaskDetailsUiAction.EditExpirationDate -> {
                setTaskExpirationDateById(
                    id = taskId,
                    expirationDate = uiAction.expirationDate
                ).collect { status ->
                    when (status) {
                        InvokeStarted -> loadingState.addLoader()
                        InvokeSuccess -> loadingState.removeLoader()
                        is InvokeError -> {
                            loadingState.removeLoader()
                            _pendingSnackbarError.emit(TaskDetailsSnackbarError.UnknownError)
                        }
                    }
                }
            }
            is TaskDetailsUiAction.EditIsChecked -> {
                setTaskIsCheckedById(id = taskId, isChecked = uiAction.checked).collect { status ->
                    when (status) {
                        InvokeStarted -> loadingState.addLoader()
                        InvokeSuccess -> {
                            loadingState.removeLoader()
                            if (uiAction.checked) {
                                _pendingUiDestination.emit(TaskDetailsDestination.Up)
                            }
                        }
                        is InvokeError -> {
                            loadingState.removeLoader()
                            _pendingSnackbarError.emit(TaskDetailsSnackbarError.UnknownError)
                        }
                    }
                }
            }
            is TaskDetailsUiAction.EditDescription -> {
                setTaskDescriptionById(
                    id = taskId,
                    description = uiAction.description
                ).collect { status ->
                    when (status) {
                        InvokeStarted -> loadingState.addLoader()
                        InvokeSuccess -> loadingState.removeLoader()
                        is InvokeError -> {
                            loadingState.removeLoader()
                            _pendingSnackbarError.emit(TaskDetailsSnackbarError.UnknownError)
                        }
                    }
                }
            }
            is TaskDetailsUiAction.EditTitle -> {
                setTaskTitleById(
                    id = taskId,
                    title = uiAction.title
                )
                    .collect { status ->
                        when (status) {
                            InvokeStarted -> loadingState.addLoader()
                            InvokeSuccess -> loadingState.removeLoader()
                            is InvokeError -> {
                                loadingState.removeLoader()
                                _pendingSnackbarError.emit(TaskDetailsSnackbarError.UnknownError)
                            }
                        }
                    }
            }
        }
    }

    fun submitUiAction(uiAction: TaskDetailsUiAction) {
        viewModelScope.launch {
            pendingUiAction.emit(uiAction)
        }
    }
}
