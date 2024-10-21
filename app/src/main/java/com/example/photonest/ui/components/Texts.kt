package com.example.photonest.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.photonest.ui.theme.bodyFontFamily

@Composable
fun Heading1(
    modifier: Modifier = Modifier,
    text: String,
    fontSize: TextUnit = 40.sp,
    lineHeight: TextUnit = 48.sp,
    textAlign: TextAlign = TextAlign.Left,
    fontWeight: FontWeight = FontWeight.SemiBold,
    fontColor: Color = MaterialTheme.colorScheme.primary,
    textDecoration: TextDecoration = TextDecoration.None
) {
    Text(
        text = text, style = TextStyle(
            fontSize = fontSize,
            fontFamily = bodyFontFamily,
            lineHeight = lineHeight,
            fontWeight = fontWeight,
            textDecoration = textDecoration,
            color = fontColor,
            textAlign = textAlign
        ), modifier = modifier
    )
}

@Composable
fun Heading2(
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 32.sp,
    text: String,
    textAlign: TextAlign = TextAlign.Left,
    fontWeight: FontWeight = FontWeight.SemiBold,
    lineHeight: TextUnit = 54.sp,
    fontColor: Color = MaterialTheme.colorScheme.primary,
    textDecoration: TextDecoration = TextDecoration.None
) {
    Text(
        text = text, style = TextStyle(
            fontSize = fontSize,
            fontFamily = bodyFontFamily,
            lineHeight = lineHeight,
            fontWeight = fontWeight,
            textDecoration = textDecoration,
            color = fontColor,
            textAlign = textAlign
        ), modifier = modifier
    )
}

@Composable
fun NormalText(
    modifier: Modifier = Modifier,
    text: String,
    fontSize: TextUnit = 16.sp,
    lineHeight: TextUnit = 25.sp,
    textAlign: TextAlign = TextAlign.Left,
    fontWeight: FontWeight = FontWeight.Medium,
    fontColor: Color = MaterialTheme.colorScheme.onBackground,
    textDecoration: TextDecoration = TextDecoration.None
) {
    Text(
        text = text, style = TextStyle(
            fontSize = fontSize,
            fontFamily = bodyFontFamily,
            lineHeight = lineHeight,
            fontWeight = fontWeight,
            textDecoration = textDecoration,
            color = fontColor,
            textAlign = textAlign
        ), modifier = modifier
    )
}

@Composable
fun AnnotatedText(
    modifier: Modifier = Modifier,
    text1: String,
    txt1Color: Color = MaterialTheme.colorScheme.onBackground,
    text2: String,
    onClickTxt2: () -> Unit = {},
    txt2Color: Color = MaterialTheme.colorScheme.primary,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text1, style = TextStyle(
                fontFamily = bodyFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = txt1Color
            )
        )
        TextButton(
            onClick = { onClickTxt2() },
            contentPadding = PaddingValues(2.dp),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.defaultMinSize(minHeight = 24.dp)
        ) {
            Text(
                text = text2, style = TextStyle(
                    fontFamily = bodyFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = txt2Color
                )
            )
        }
    }
}

@Composable
fun annotatedText(
    text1: String,
    text1Color: Color = MaterialTheme.colorScheme.onBackground,
    text2: String,
    text2Color: Color = MaterialTheme.colorScheme.primary
): AnnotatedString {
    return buildAnnotatedString {
        pushStyle(
            SpanStyle(
                fontFamily = bodyFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = text1Color
            )
        )
        append(text1)
        pop()
        append(" ")
        pushStyle(
            SpanStyle(
                fontFamily = bodyFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = text2Color
            )
        )
        append(text2)
        pop()
    }
}