package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val EbraKhaitColorScheme = lightColorScheme(
  primary = DeepPlumPrimary,
  onPrimary = Color.White,
  primaryContainer = SoftBeige,
  onPrimaryContainer = DeepPlumPrimary,
  secondary = MutedBlushRose,
  onSecondary = Color.White,
  secondaryContainer = SoftRoseLight,
  onSecondaryContainer = DeepPlumPrimary,
  tertiary = GoldAccentIqd,
  onTertiary = Color.White,
  background = IvoryBackground,
  onBackground = TextMain,
  surface = CreamSurface,
  onSurface = TextMain,
  surfaceVariant = SoftBeige,
  onSurfaceVariant = TextMuted,
  error = VelvetRoseError,
  onError = Color.White
)

@Composable
fun EbraKhaitTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = EbraKhaitColorScheme,
    typography = Typography,
    content = content
  )
}
