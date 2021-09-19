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

package app.sedici.tasks.ui.taskdetails.internal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

internal class TaskDetailsViewModel @Inject constructor() : ViewModel() {

    private val showDeleteTaskConfirmDialog = MutableStateFlow(false)

    private val pendingUiAction = MutableSharedFlow<UiAction>()

    private val _pendingUiDestination = MutableSharedFlow<UiDestination>()
    val pendingUiDestination = _pendingUiDestination.asSharedFlow()

    val uiState = showDeleteTaskConfirmDialog.map { showDeleteTaskConfirmDialog ->
        UiState(
            showDeleteTaskConfirmDialog = showDeleteTaskConfirmDialog,
            loading = false,
            task = null,
        )
    }

    init {
        viewModelScope.launch {
            pendingUiAction.collect(::handleUiAction)
        }
    }

    private suspend fun handleUiAction(uiAction: UiAction) {
        when (uiAction) {
            UiAction.DeleteTask -> showDeleteTaskConfirmDialog.emit(true)
            is UiAction.AnswerConfirmDeleteTask -> {
                handleConfirmDeleteTaskAnswer(uiAction)
            }
            UiAction.EditTask -> TODO("navigate to edit task screen")
            UiAction.NavigateUp -> {
                _pendingUiDestination.emit(UiDestination.Up)
            }
        }
    }

    private suspend fun handleConfirmDeleteTaskAnswer(answer: UiAction.AnswerConfirmDeleteTask) {
        showDeleteTaskConfirmDialog.emit(false)
        when (answer) {
            UiAction.AnswerConfirmDeleteTask.Cancel -> TODO()
            UiAction.AnswerConfirmDeleteTask.Delete -> TODO("delete task and navigate up")
        }
    }

    fun submitUiAction(uiAction: UiAction) {
        viewModelScope.launch {
            pendingUiAction.emit(uiAction)
        }
    }
}
