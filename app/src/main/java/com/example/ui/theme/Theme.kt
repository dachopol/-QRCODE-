package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = PromptPayBlue,
    secondary = CyanLight,
    tertiary = PromptPayEmerald,
    background = Color(0xFF0F172A),
    surface = Color(0xFF1E293B),
    onPrimary = Color.White,
    onSurface = Color(0xFFE2E8F0),
    onSurfaceVariant = Color(0xFFCBD5E1),
    surfaceVariant = Color(0xFF334155),
    outlineVariant = Color(0xFF475569),
    onBackground = Color(0xFFE2E8F0)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = PromptPayNavy,
    secondary = PromptPayBlue,
    tertiary = PromptPayEmerald,
    background = BackgroundLight,
    surface = SurfaceLight,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF0F172A),
    onSurfaceVariant = Color(0xFF475569),
    surfaceVariant = Color(0xFFF1F5F9),
    outlineVariant = Color(0xFFCBD5E1)
  )

@Composable
fun appTextFieldColors(
    focusedBorderColor: Color = Color(0xFF0B2853),
    unfocusedBorderColor: Color = Color(0xFFCBD5E1),
    containerColor: Color = Color.White
): TextFieldColors {
    return OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color.Black,
        unfocusedTextColor = Color.Black,
        focusedContainerColor = containerColor,
        unfocusedContainerColor = containerColor,
        disabledContainerColor = containerColor,
        cursorColor = focusedBorderColor,
        focusedBorderColor = focusedBorderColor,
        unfocusedBorderColor = unfocusedBorderColor,
        focusedPlaceholderColor = Color(0xFF64748B),
        unfocusedPlaceholderColor = Color(0xFF64748B),
        focusedLeadingIconColor = Color(0xFF0284C7),
        unfocusedLeadingIconColor = Color(0xFF64748B),
        focusedTrailingIconColor = Color(0xFF0284C7),
        unfocusedTrailingIconColor = Color(0xFF64748B)
    )
}

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Keep brand palette crisp and predictable
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
