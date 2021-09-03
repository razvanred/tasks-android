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

import androidx.compose.ui.text.input.TextFieldValue
import java.time.LocalDate

data class UiState(
    val title: TextFieldValue,
    val description: TextFieldValue,
    val expiresOn: LocalDate?,
    val showExpirationDatePicker: Boolean,
    val showConfirmDiscardChangesDialog: Boolean,
) {
    val shouldShowConfirmDiscardChangesDialog: Boolean
        get() = title != Empty.title ||
            description != Empty.description ||
            expiresOn != Empty.expiresOn

    companion object {
        val Empty = UiState(
            title = TextFieldValue(text = ""),
            description = TextFieldValue(text = ""),
            expiresOn = null,
            showExpirationDatePicker = false,
            showConfirmDiscardChangesDialog = false,
        )
    }
}
