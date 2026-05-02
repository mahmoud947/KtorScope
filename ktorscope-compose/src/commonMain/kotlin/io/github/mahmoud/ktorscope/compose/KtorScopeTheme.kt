/**
 * Created by Mahmoud kamal El-Din on 02/05/2026
 */
package io.github.mahmoud.ktorscope.compose

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class KtorScopeThemeMode {
    System,
    Light,
    Dark,
}

@Composable
fun KtorScopeTheme(
    themeMode: KtorScopeThemeMode = KtorScopeThemeMode.System,
    content: @Composable () -> Unit,
) {
    val dark = when (themeMode) {
        KtorScopeThemeMode.System -> isSystemInDarkTheme()
        KtorScopeThemeMode.Light -> false
        KtorScopeThemeMode.Dark -> true
    }
    MaterialTheme(
        colorScheme = if (dark) KtorScopeDarkColors else KtorScopeLightColors,
        content = content,
    )
}

private val KtorScopeLightColors = lightColorScheme(
    primary = Color(0xFF2563EB),
    onPrimary = Color.White,
    secondary = Color(0xFF0F766E),
    tertiary = Color(0xFF7C3AED),
    background = Color(0xFFF6F8FB),
    onBackground = Color(0xFF111827),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF111827),
    surfaceVariant = Color(0xFFEFF3F8),
    onSurfaceVariant = Color(0xFF4B5563),
    outline = Color(0xFFD7DEE8),
    error = Color(0xFFDC2626),
    onError = Color.White,
)

private val KtorScopeDarkColors = darkColorScheme(
    primary = Color(0xFF7AA2FF),
    onPrimary = Color(0xFF061A3D),
    secondary = Color(0xFF5EEAD4),
    tertiary = Color(0xFFC4B5FD),
    background = Color(0xFF0B1020),
    onBackground = Color(0xFFE5E7EB),
    surface = Color(0xFF121A2C),
    onSurface = Color(0xFFE5E7EB),
    surfaceVariant = Color(0xFF1C2740),
    onSurfaceVariant = Color(0xFFB8C0CC),
    outline = Color(0xFF334155),
    error = Color(0xFFF87171),
    onError = Color(0xFF450A0A),
)
