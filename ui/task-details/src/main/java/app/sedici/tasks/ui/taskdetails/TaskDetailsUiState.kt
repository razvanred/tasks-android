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

import androidx.compose.runtime.Stable
import app.sedici.tasks.model.Task

@Stable
data class TaskDetailsUiState(
    val task: Task?,
    val loading: Boolean,
    val showConfirmDeleteDialog: Boolean,
    val showExpirationDatePicker: Boolean,
) {
    companion object {
        val Empty = TaskDetailsUiState(
            task = null,
            loading = false,
            showConfirmDeleteDialog = false,
            showExpirationDatePicker = false,
        )
    }
}
