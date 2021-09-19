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

package app.sedici.tasks.ui.createtask.internal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.sedici.tasks.base.android.ui.ObservableLoadingCounter
import app.sedici.tasks.base.common.InvokeError
import app.sedici.tasks.base.common.InvokeStarted
import app.sedici.tasks.base.common.InvokeStatus
import app.sedici.tasks.base.common.InvokeSuccess
import app.sedici.tasks.base.common.extensions.combine
import app.sedici.tasks.domain.SaveNewTask
import app.sedici.tasks.model.NewTask
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class CreateTaskViewModel @Inject constructor(
    private val saveNewTask: SaveNewTask,
) : ViewModel() {

    private val title = MutableStateFlow(UiState.Empty.title)
    private val description = MutableStateFlow(UiState.Empty.description)
    private val expiresOn = MutableStateFlow(UiState.Empty.expiresOn)
    private val showExpirationDatePicker = MutableStateFlow(UiState.Empty.showExpirationDatePicker)
    private val showConfirmDiscardChangesDialog = MutableStateFlow(
        UiState.Empty.showConfirmDiscardChangesDialog
    )

    private val loadingState = ObservableLoadingCounter()

    val uiState = combine(
        title,
        description,
        expiresOn,
        showExpirationDatePicker,
        showConfirmDiscardChangesDialog,
        loadingState.observable,
    ) { title, description, expiresOn, showExpirationDatePicker, showConfirmDiscardChangesDialog, loading ->
        UiState(
            title = title,
            description = description,
            expiresOn = expiresOn,
            showExpirationDatePicker = showExpirationDatePicker,
            showConfirmDiscardChangesDialog = showConfirmDiscardChangesDialog,
            loading = loading,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
        initialValue = UiState.Empty
    )

    private val pendingUiAction = MutableSharedFlow<UiAction>()

    private val _pendingDestination = MutableSharedFlow<UiDestination>()
    val pendingDestination = _pendingDestination.asSharedFlow()

    private val _pendingSnackbarError = MutableSharedFlow<SnackbarError>()
    val pendingSnackbarError = _pendingSnackbarError.asSharedFlow()

    init {
        viewModelScope.launch {
            pendingUiAction.collect { uiAction ->
                when (uiAction) {
                    is UiAction.SaveTask -> {
                        val uiState = uiState.first()
                        val newTask = NewTask(
                            title = uiState.title.text,
                            description = uiState.description.text,
                            expiresOn = uiState.expiresOn,
                        )
                        saveNewTask(newTask).watchStatus()
                    }
                    is UiAction.EditTitle -> title.emit(uiAction.newValue)
                    is UiAction.EditDescription -> description.emit(uiAction.newValue)
                    is UiAction.SetExpirationDate -> expiresOn.emit(uiAction.newValue)
                    UiAction.DismissExpirationDatePicker -> {
                        showExpirationDatePicker.emit(false)
                    }
                    UiAction.OpenExpirationDatePicker -> {
                        showExpirationDatePicker.emit(true)
                    }
                    UiAction.ClearExpirationDate -> expiresOn.emit(null)
                    UiAction.NavigateUp -> {
                        val uiState = uiState.first()
                        if (uiState.shouldShowConfirmDiscardChangesDialog) {
                            showConfirmDiscardChangesDialog.emit(true)
                        } else {
                            _pendingDestination.emit(UiDestination.Up)
                        }
                    }
                    UiAction.CancelDiscardChanges -> {
                        showConfirmDiscardChangesDialog.emit(false)
                    }
                    UiAction.ConfirmDiscardChanges -> {
                        showConfirmDiscardChangesDialog.emit(false)
                        _pendingDestination.emit(UiDestination.Up)
                    }
                }
            }
        }
    }

    private fun Flow<InvokeStatus>.watchStatus() = viewModelScope.launch { collectStatus() }

    private suspend fun Flow<InvokeStatus>.collectStatus() = collect { status ->
        when (status) {
            InvokeStarted -> loadingState.addLoader()
            InvokeSuccess -> {
                loadingState.removeLoader()
                _pendingDestination.emit(UiDestination.Up)
            }
            is InvokeError -> {
                loadingState.removeLoader()
                _pendingSnackbarError.emit(SnackbarError.ErrorWhileSaving)
            }
        }
    }

    fun submitUiAction(uiAction: UiAction) {
        viewModelScope.launch {
            pendingUiAction.emit(uiAction)
        }
    }
}
