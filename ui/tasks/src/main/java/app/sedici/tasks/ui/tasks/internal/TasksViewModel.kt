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

package app.sedici.tasks.ui.tasks.internal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.sedici.tasks.base.android.ui.ObservableLoadingCounter
import app.sedici.tasks.base.common.InvokeError
import app.sedici.tasks.base.common.InvokeStarted
import app.sedici.tasks.base.common.InvokeStatus
import app.sedici.tasks.base.common.InvokeSuccess
import app.sedici.tasks.domain.ObserveTasks
import app.sedici.tasks.domain.SetTaskIsCheckedById
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class TasksViewModel @Inject constructor(
    observeTasks: ObserveTasks,
    setTaskIsCheckedById: SetTaskIsCheckedById,
) : ViewModel() {

    private val pendingActions = MutableSharedFlow<UiAction>()

    private val loadingState = ObservableLoadingCounter()

    val uiState = combine(
        observeTasks.flow,
        loadingState.observable
    ) { tasks, loading ->
        UiState(tasks = tasks, loading = loading)
    }

    init {
        viewModelScope.launch {
            pendingActions.collect { uiAction ->
                when (uiAction) {
                    is UiAction.EditTaskIsChecked -> {
                        setTaskIsCheckedById(
                            id = uiAction.taskId,
                            isChecked = uiAction.checked
                        ).watchStatus()
                    }
                }
            }
        }

        observeTasks()
    }

    private fun Flow<InvokeStatus>.watchStatus() = viewModelScope.launch { collectStatus() }

    private suspend fun Flow<InvokeStatus>.collectStatus() = collect { status ->
        when (status) {
            InvokeStarted -> {}
            InvokeSuccess -> {}
            is InvokeError -> {
                // TODO show snackbar error
            }
        }
    }

    fun submitUiAction(uiAction: UiAction) {
        viewModelScope.launch {
            pendingActions.emit(uiAction)
        }
    }
}
