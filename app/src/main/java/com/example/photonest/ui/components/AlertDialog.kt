package com.example.photonest.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.photonest.ui.theme.PhotoNestTheme

@Composable
fun MyAlertDialog(
    modifier: Modifier = Modifier,
    shouldShowDialog: Boolean,
    onDismissRequest: () -> Unit,
    title: String,
    text: String,
    confirmButtonText: String,
    onConfirmClick: () -> Unit,
    dismissButtonText: String? = null,
    onDismissClick: (() -> Unit)? = null,
) {
    if (shouldShowDialog) {
        AlertDialog(
            modifier = modifier,
            onDismissRequest = onDismissRequest,
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            title = {
                Heading2(
                    text = title,
                    fontSize = 24.sp,
                    lineHeight = 28.sp,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                NormalText(
                    text = text,
                    fontColor = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Left
                )
            },
            confirmButton = {
                ButtonOnboarding(
                    buttonText = confirmButtonText,
                    onClick = onConfirmClick,
                    shape = RoundedCornerShape(8.dp),
                    )
            },
            dismissButton = dismissButtonText?.let {
                {
                    ButtonOnboarding(
                        buttonColors = ButtonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer,
                            disabledContainerColor = MaterialTheme.colorScheme.errorContainer,
                            disabledContentColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        shape = RoundedCornerShape(8.dp),
                        buttonText = it,
                        onClick = onDismissClick ?: onDismissRequest
                    )
                }
            }
        )
    }
}

//@SuppressLint("UnrememberedMutableState")
//@Preview(
//    showBackground = true,
//    showSystemUi = true
//)
//@Composable
//private fun Prev() {
//    PhotoNestTheme {
//        Box(
//            modifier = Modifier.fillMaxSize(),
//            contentAlignment = Alignment.Center
//        ){
//            MyAlertDialog(
//                title = "Alert Dialog",
//                shouldShowDialog = true,
//                dismissButtonText = "Close",
//                onDismissRequest = {},
//                text = "This is a custom error.This is a custom error.This is a custom error.This is a custom error.",
//                confirmButtonText = "Continue",
//                onConfirmClick = {}
//            )
//        }
//    }
//}