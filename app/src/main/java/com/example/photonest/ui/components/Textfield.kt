package com.example.photonest.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.compose.grey
import com.example.compose.light_grey
import com.example.compose.nickel
import com.example.photonest.ui.theme.fontFamily


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
    Column (
        modifier = modifier
    ){
        Text(
            text = label,
            style = TextStyle(
                fontSize = 14.sp,
                fontFamily = fontFamily,
                lineHeight = 22.sp,
                fontWeight = FontWeight.Medium,
                color = nickel
            ),
            modifier = Modifier.padding(start = 5.dp, bottom = 5.dp)
        )
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
                                Text(
                                    text = label, color = grey,
                                    style = TextStyle(
                                        fontFamily = MaterialTheme.typography.bodySmall.fontFamily,
                                        fontSize = 16.sp,
                                        lineHeight = 25.sp
                                    )
                                )
                            }

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
}

@Composable
fun ShowHidePasswordTextField(
    label: String,
    modifier: Modifier = Modifier,
    password: MutableState<String> =  mutableStateOf(value = ""),
) {
    var showPassword by remember { mutableStateOf(value = false) }
    Column (
        modifier = modifier
    ){
        Text(
            text = label,
            style = TextStyle(
                fontSize = 14.sp,
                fontFamily = fontFamily,
                lineHeight = 22.sp,
                fontWeight = FontWeight.Medium,
                color = nickel
            ),
            modifier = Modifier.padding(start = 5.dp, bottom = 5.dp)
        )

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 54.dp),
            value = password.value,
            onValueChange = { newText ->
                password.value = newText
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = "hide_password",
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedLeadingIconColor = nickel,
                unfocusedLeadingIconColor = light_grey,
                unfocusedBorderColor = light_grey,
                focusedBorderColor = nickel,
                unfocusedTextColor = light_grey,
                focusedTextColor = nickel,
                focusedPlaceholderColor = nickel,
                unfocusedPlaceholderColor = light_grey,
                cursorColor = nickel,
                focusedLabelColor = nickel,
                disabledLabelColor = light_grey,
                disabledTrailingIconColor = light_grey,
                focusedTrailingIconColor = nickel
            ),
            placeholder = {
                Text(
                    text = label,
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontFamily = fontFamily,
                        lineHeight = 25.sp,
                        fontWeight = FontWeight.Medium),
                )
            },
            shape = RoundedCornerShape(percent = 20),
            visualTransformation = if (showPassword) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                if (showPassword) {
                    IconButton(onClick = { showPassword = false }) {
                        Icon(
                            imageVector = Icons.Filled.Visibility,
                            contentDescription = "hide_password",
                        )
                    }
                } else {
                    IconButton(
                        onClick = { showPassword = true }) {
                        Icon(
                            imageVector = Icons.Filled.VisibilityOff,
                            contentDescription = "hide_password"
                        )
                    }
                }
            }
        )
    }
    
}