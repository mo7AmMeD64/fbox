package com.fkbox.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

/**
 * Main theme entry point - uses DesignTokens for all visual decisions.
 * Single source of truth for colors, typography, shapes, spacing, motion.
 */
@Composable
fun FkboxTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = FkboxColorScheme(),
        shapes = FkboxShapes(),
        typography = FkboxTypography(),
        content = content,
    )
}

/**
 * Access DesignTokens anywhere in composition without importing the object.
 * Provides consistent spacing, sizing, elevation, motion tokens.
 */
@Composable
fun DesignTokensProvider(content: @Composable () -> Unit) {
    content()
}