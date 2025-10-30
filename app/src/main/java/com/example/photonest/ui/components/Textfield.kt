package com.example.photonest.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.photonest.ui.theme.bodyFontFamily

@Composable
fun OnBoardingTextField(
    modifier: Modifier = Modifier,
    readOnly: Boolean = false,
    maxLines: Int = 1,
    showClearIcon: Boolean = true,
    showLabel: Boolean = true,
    label: String = "",
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    value: String,
    onValueChange: (String) -> Unit,
    isError: Boolean = false,
    onSearch: () -> Unit = {},
    onClearSearch: () -> Unit = {},
    errorMessage:  @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    postfix: @Composable (() -> Unit)? = null,
) {

    val keyboardController = LocalSoftwareKeyboardController.current

    Column(modifier = modifier) {
        if (showLabel){
            Text(
                text = label,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontFamily = bodyFontFamily,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground
                ),
                modifier = Modifier.padding(start = 5.dp, bottom = 5.dp)
            )
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            shape = RoundedCornerShape(12.dp),
            maxLines = maxLines,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 54.dp),
            readOnly = readOnly,
            placeholder = {
                Text(
                    text = label,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = TextStyle(
                        fontFamily = bodyFontFamily,
                        fontSize = 16.sp,
                        lineHeight = 25.sp
                    )
                )
            },
            keyboardOptions = keyboardOptions,
            isError = isError,
            supportingText = if (isError) {
                errorMessage
            } else null,
            textStyle = TextStyle(
                fontFamily = bodyFontFamily,
                fontSize = 16.sp,
                lineHeight = 25.sp
            ),
            keyboardActions = KeyboardActions(
                onSearch = {
                    onSearch()
                    keyboardController?.hide()
                }
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                focusedBorderColor = MaterialTheme.colorScheme.outline,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(0.4f)
            ),
            leadingIcon = prefix,
            trailingIcon = {
                if(postfix != null) {
                    postfix
                } else if (showClearIcon && value.isNotEmpty()) { // Only show if editing
                    IconButton(onClick = onClearSearch) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        )
    }
}

@Composable
fun ShowHidePasswordTextField(
    label: String,
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    isError: Boolean = false,
    errorMessage:  @Composable (() -> Unit)? = null,
) {
    var showPassword by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(
            text = label,
            style = TextStyle(
                fontSize = 14.sp,
                fontFamily = bodyFontFamily,
                lineHeight = 22.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground
            ),
            modifier = Modifier.padding(start = 5.dp, bottom = 5.dp)
        )

        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 54.dp),
            value = value,
            onValueChange = onValueChange,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = "Password"
                )
            },
            isError = isError,
            supportingText = if (isError) errorMessage else null,
            colors = OutlinedTextFieldDefaults.colors(
                focusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                focusedBorderColor = MaterialTheme.colorScheme.outline,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(0.4f)
            ),
            placeholder = {
                Text(
                    text = label,
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontFamily = bodyFontFamily,
                        lineHeight = 25.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            },
            shape = RoundedCornerShape(12.dp),
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = { showPassword = !showPassword }) {
                    Icon(
                        imageVector = if (showPassword) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                        contentDescription = if (showPassword) "Hide password" else "Show password",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        )
    }
}