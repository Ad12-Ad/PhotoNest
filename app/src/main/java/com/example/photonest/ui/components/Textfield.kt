package com.example.photonest.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.compose.grey


@Composable
fun OnBoardingTextField(
    label: String,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester,
    interactionSource: MutableInteractionSource = MutableInteractionSource(),
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    onKeyboardAction: KeyboardActionHandler? = null, // Use this instead of keyboardActions
    lineLimits: TextFieldLineLimits = TextFieldLineLimits.SingleLine,
    textFieldState: TextFieldState = rememberTextFieldState(),
    prefix: @Composable () -> Unit = {},
    postfix: @Composable () -> Unit = {},
) {
    val isFocused = interactionSource.collectIsFocusedAsState()
    BasicTextField(
        state = textFieldState,
        interactionSource = interactionSource,
        modifier = modifier
            .heightIn(58.dp)
            .fillMaxWidth()
            .padding(horizontal = 1.dp)
            .clip(RoundedCornerShape(10.dp))
            .focusRequester(focusRequester)
            .border(
                border = BorderStroke(
                    2.dp,
                    color = if (isFocused.value) {
                        MaterialTheme.colorScheme.outlineVariant
                    } else {
                        MaterialTheme.colorScheme.outline
                    }
                ),
                shape = RoundedCornerShape(10.dp)
            ),
        decorator = { innerTextField ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        prefix()
                        if (textFieldState.text.isBlank() && !isFocused.value) {
                        }
                        Text(
                            text = label, color = grey,
                            style = TextStyle(
                                fontFamily = MaterialTheme.typography.bodySmall.fontFamily,
                                fontSize = 16.sp,
                                lineHeight = 25.sp
                            )
                        )
                        innerTextField()
                    }
                    if (textFieldState.text.isNotEmpty()) {
                        Icon(
                            modifier = Modifier.clickable {
                                textFieldState.edit {
                                    this.replace(0, textFieldState.text.length, "")
                                }
                            },
                            imageVector = Icons.Default.Clear,
                            contentDescription = null
                        )
                    }
                    postfix()
                }
            }
        },
        keyboardOptions = keyboardOptions,
        onKeyboardAction = onKeyboardAction,
        lineLimits = lineLimits,
        textStyle = TextStyle(
            fontFamily = MaterialTheme.typography.bodySmall.fontFamily,
            fontSize = 16.sp,
            lineHeight = 25.sp
        )
    )
}