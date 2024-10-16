package com.example.photonest.ui.theme

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import com.example.photonest.R

val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

val fontName = GoogleFont("Poppins")

val fontFamily = FontFamily(
    Font(
        googleFont = fontName,
        fontProvider = provider,
        weight = FontWeight.Bold,
        style = FontStyle.Italic
    ),
    Font(
        googleFont = fontName,
        fontProvider = provider,
        weight = FontWeight.Medium,
        style = FontStyle.Italic
    ),
    Font(
        googleFont = fontName,
        fontProvider = provider,
        weight = FontWeight.Normal,
        style = FontStyle.Italic
    ),
    Font(
        googleFont = fontName,
        fontProvider = provider,
        weight = FontWeight.Thin,
        style = FontStyle.Italic
    ),
    Font(
        googleFont = fontName,
        fontProvider = provider,
        weight = FontWeight.SemiBold,
        style = FontStyle.Italic
    ),
)