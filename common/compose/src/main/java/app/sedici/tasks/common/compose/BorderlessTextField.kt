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

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation

@Composable
fun BorderlessTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean = DefaultParams.enabled,
    readOnly: Boolean = DefaultParams.readOnly,
    textStyle: TextStyle = DefaultParams.textStyle,
    keyboardOptions: KeyboardOptions = DefaultParams.keyboardOptions,
    keyboardActions: KeyboardActions = DefaultParams.keyboardActions,
    singleLine: Boolean = DefaultParams.singleLine,
    maxLines: Int = DefaultParams.maxLines,
    visualTransformation: VisualTransformation = DefaultParams.visualTransformation,
    interactionSource: MutableInteractionSource = DefaultParams.interactionSource,
    placeholder: @Composable () -> Unit = DefaultParams.placeholder,
) {
    var textFieldValueState by remember { mutableStateOf(TextFieldValue(text = value)) }
    val textFieldValue = textFieldValueState.copy(text = value)

    BorderlessTextField(
        modifier = modifier,
        value = textFieldValue,
        onValueChange = {
            textFieldValueState = it
            if (value != it.text) {
                onValueChange(it.text)
            }
        },
        enabled = enabled,
        readOnly = readOnly,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = singleLine,
        maxLines = maxLines,
        visualTransformation = visualTransformation,
        interactionSource = interactionSource,
        textStyle = textStyle,
        placeholder = placeholder,
    )
}

@Composable
fun BorderlessTextField(
    modifier: Modifier = Modifier,
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    enabled: Boolean = DefaultParams.enabled,
    readOnly: Boolean = DefaultParams.readOnly,
    textStyle: TextStyle = DefaultParams.textStyle,
    keyboardOptions: KeyboardOptions = DefaultParams.keyboardOptions,
    keyboardActions: KeyboardActions = DefaultParams.keyboardActions,
    singleLine: Boolean = DefaultParams.singleLine,
    maxLines: Int = DefaultParams.maxLines,
    visualTransformation: VisualTransformation = DefaultParams.visualTransformation,
    interactionSource: MutableInteractionSource = DefaultParams.interactionSource,
    placeholder: @Composable () -> Unit = DefaultParams.placeholder,
) {
    val textColor = MaterialTheme.colors.onSurface
    val textStyleWithColor = textStyle.copy(color = textColor)

    BasicTextField(
        modifier = modifier,
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        readOnly = readOnly,
        textStyle = textStyleWithColor,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = singleLine,
        maxLines = maxLines,
        visualTransformation = visualTransformation,
        onTextLayout = DefaultParams.onTextLayout,
        interactionSource = interactionSource,
        cursorBrush = SolidColor(textColor),
        decorationBox = { innerTextField ->
            Box {
                innerTextField()
                if (value.text.isEmpty()) {
                    CompositionLocalProvider(
                        LocalContentAlpha provides ContentAlpha.medium,
                        LocalTextStyle provides textStyle
                    ) {
                        placeholder()
                    }
                }
            }
        }
    )
}

private object DefaultParams {

    const val enabled = true

    const val readOnly = false

    val textStyle = TextStyle.Default

    val keyboardOptions = KeyboardOptions.Default

    val keyboardActions = KeyboardActions.Default

    const val singleLine = false

    const val maxLines = Int.MAX_VALUE

    val visualTransformation = VisualTransformation.None

    val interactionSource: MutableInteractionSource
        @Composable
        get() = remember { MutableInteractionSource() }

    val onTextLayout: (TextLayoutResult) -> Unit = {}

    val placeholder: @Composable () -> Unit = {}
}
