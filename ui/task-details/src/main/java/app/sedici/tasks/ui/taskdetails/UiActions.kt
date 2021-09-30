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

import java.time.LocalDate

sealed interface TaskDetailsUiAction {
    object ShowConfirmDeleteDialog : TaskDetailsUiAction
    object Delete : TaskDetailsUiAction
    object DismissConfirmDeleteDialog : TaskDetailsUiAction

    data class EditDescription(val description: String) : TaskDetailsUiAction

    object NavigateUp : TaskDetailsUiAction

    data class EditIsChecked(val checked: Boolean) : TaskDetailsUiAction

    object ShowExpirationDatePicker : TaskDetailsUiAction
    object DismissExpirationDatePicker : TaskDetailsUiAction
    data class EditExpirationDate(val expirationDate: LocalDate?) : TaskDetailsUiAction

    data class EditTitle(val title: String) : TaskDetailsUiAction
}
