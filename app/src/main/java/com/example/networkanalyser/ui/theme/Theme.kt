package com.example.networkanalyser.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

val TealShades = listOf(
    Color(0xFF02B0C0), Color(0xFF03AFBF), Color(0xFF05AEBD), Color(0xFF06ACBC),
    Color(0xFF08ABBA), Color(0xFF0BA9B7), Color(0xFF0CA7B5), Color(0xFF0EA6B4),
    Color(0xFF10A5B2), Color(0xFF189EAA), Color(0xFF2097A2), Color(0xFF28909A),
    Color(0xFF308A92), Color(0xFF38838A), Color(0xFF407C81), Color(0xFF487579),
    Color(0xFF516E71), Color(0xFF596869)
)

val PrimaryColor = TealShades[0]
val PrimaryContainer = TealShades[4]
val SecondaryColor = TealShades[10]

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryColor,
    secondary = SecondaryColor,
    tertiary = TealShades[15],
    background = Color.Black,
    surface = Color.DarkGray,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryColor,
    primaryContainer = PrimaryContainer,
    secondary = SecondaryColor,
    tertiary = TealShades[8],
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black
)

@Composable
fun NetworkAnalyserTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
