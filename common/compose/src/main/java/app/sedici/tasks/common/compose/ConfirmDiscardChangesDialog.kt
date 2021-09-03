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

package app.sedici.tasks.common.compose

import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

@Composable
fun ConfirmDiscardChangesDialog(
    title: @Composable () -> Unit = {
        Text(text = stringResource(R.string.confirm_discard_changes_dialog_title))
    },
    text: @Composable () -> Unit = {
        Text(text = stringResource(R.string.confirm_discard_changes_dialog_message))
    },
    onDiscardClick: () -> Unit,
    onDismissClick: () -> Unit,
) {
    AlertDialog(
        title = title,
        text = text,
        onDismissRequest = onDismissClick,
        dismissButton = {
            TextButton(onClick = onDismissClick) {
                Text(text = stringResource(R.string.confirm_discard_changes_dialog_cancel_button))
            }
        },
        confirmButton = {
            Button(onClick = onDiscardClick) {
                Text(text = stringResource(R.string.confirm_discard_changes_dialog_discard_button))
            }
        }
    )
}
