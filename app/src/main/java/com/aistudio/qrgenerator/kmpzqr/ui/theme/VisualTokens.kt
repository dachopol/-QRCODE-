package com.aistudio.qrgenerator.kmpzqr.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * QuickQR Business visual system.
 * Glass is intentionally subtle (~5%) and used only as an accent.
 */
val AppCardShape = RoundedCornerShape(28.dp)
val AppSectionShape = RoundedCornerShape(20.dp)
val AppPillShape = CircleShape

val GlassAccent = PromptPayBlue.copy(alpha = 0.05f)
val GlassBorder = PromptPayBlue.copy(alpha = 0.14f)
val GlassHighlight = Color.White.copy(alpha = 0.72f)

val GlassBlurRadius = 40.dp

val SpaceXs = 4.dp
val SpaceSm = 8.dp
val SpaceMd = 16.dp
val SpaceLg = 24.dp
val SpaceXl = 32.dp
