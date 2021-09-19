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

import androidx.compose.runtime.Stable
import app.sedici.tasks.model.Task

@Stable
internal data class UiState(
    val task: Task?,
    val loading: Boolean,
    val showDeleteTaskConfirmDialog: Boolean,
) {
    companion object {
        val Empty = UiState(
            task = null,
            loading = false,
            showDeleteTaskConfirmDialog = false,
        )
    }
}
