package com.example.photonest.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.photonest.ui.theme.bodyFontFamily

@Composable
fun OnBoardingTextField(
    label: String,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    value: String,
    onValueChange: (String) -> Unit,
    prefix: @Composable (() -> Unit)? = null,
    postfix: @Composable (() -> Unit)? = null,
) {

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
            value = value,
            onValueChange = onValueChange,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 54.dp),
            keyboardOptions = keyboardOptions,
            textStyle = TextStyle(
                fontFamily = bodyFontFamily,
                fontSize = 16.sp,
                lineHeight = 25.sp
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                focusedBorderColor = MaterialTheme.colorScheme.outline,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(0.4f)
            ),
            leadingIcon = prefix,
            trailingIcon = postfix,
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
            }
        )
    }
}

@Composable
fun ShowHidePasswordTextField(
    label: String,
    modifier: Modifier = Modifier,
    password: MutableState<String> = remember { mutableStateOf("") },
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
            value = password.value,
            onValueChange = { newText -> password.value = newText },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = "Password"
                )
            },
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