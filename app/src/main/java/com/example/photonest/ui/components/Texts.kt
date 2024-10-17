package com.example.photonest.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.compose.grey
import com.example.compose.kelly_green
import com.example.compose.nickel
import com.example.photonest.ui.theme.fontFamily

@Composable
fun Heading1(
    text: String,
    fontWeight: FontWeight = FontWeight.SemiBold,
    fontColor: Color = kelly_green,
    textDecoration: TextDecoration = TextDecoration.None,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = TextStyle(
            fontSize = 40.sp,
            fontFamily = fontFamily,
            lineHeight = 54.sp,
            fontWeight = fontWeight,
            textDecoration = textDecoration,
            color = fontColor
        ),
        modifier = modifier
    )
}

@Composable
fun Heading2(
    text: String,
    fontWeight: FontWeight = FontWeight.SemiBold,
    fontColor: Color = kelly_green,
    textDecoration: TextDecoration = TextDecoration.None,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = TextStyle(
            fontSize = 32.sp,
            fontFamily = fontFamily,
            lineHeight = 54.sp,
            fontWeight = fontWeight,
            textDecoration = textDecoration,
            color = fontColor
        ),
        modifier = modifier
    )
}

@Composable
fun NormalText(
    text: String,
    fontWeight: FontWeight = FontWeight.Medium,
    fontColor: Color = grey,
    textDecoration: TextDecoration = TextDecoration.None,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = TextStyle(
            fontSize = 16.sp,
            fontFamily = fontFamily,
            lineHeight = 25.sp,
            fontWeight = fontWeight,
            textDecoration = textDecoration,
            color = fontColor
        ),
        modifier = modifier
    )
}

@Composable
fun AnnotatedText(
    text1: String,
    txt1Color: Color = nickel,
    text2: String,
    onClickTxt2: () -> Unit = {},
    txt2Color: Color = kelly_green,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text1,
            style = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = txt1Color
            )
        )
        TextButton(
            onClick = { onClickTxt2() },
            contentPadding = PaddingValues(2.dp),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .defaultMinSize(minHeight = 24.dp)
        ) {
            Text(
                text = text2,
                style = TextStyle(
                    fontFamily = fontFamily,
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
    text1Color: Color = nickel,
    text2: String,
    text2Color: Color = kelly_green
): AnnotatedString {
    return buildAnnotatedString {
        pushStyle(
            SpanStyle(
                fontFamily = fontFamily,
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
                fontFamily = fontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = text2Color
            )
        )
        append(text2)
        pop()
    }
}