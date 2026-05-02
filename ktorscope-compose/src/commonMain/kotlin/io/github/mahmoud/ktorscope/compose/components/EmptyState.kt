/**
 * Created by Mahmoud kamal El-Din on 02/05/2026
 */
package io.github.mahmoud.ktorscope.compose.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
internal fun EmptyState(modifier: Modifier = Modifier) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
            EmptyIllustration()
            Text("No transactions yet", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                "Run a request and KtorScope will capture it here.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun EmptyIllustration() {
    val primary = MaterialTheme.colorScheme.primary
    val outline = MaterialTheme.colorScheme.outline
    Canvas(Modifier.size(112.dp)) {
        drawCircle(primary.copy(alpha = 0.12f), radius = size.minDimension / 2f)
        drawCircle(outline.copy(alpha = 0.6f), radius = size.minDimension / 3.2f, center = center)
        drawLine(primary, Offset(size.width * 0.28f, size.height * 0.52f), Offset(size.width * 0.72f, size.height * 0.52f), strokeWidth = 6f)
        drawLine(primary, Offset(size.width * 0.52f, size.height * 0.30f), Offset(size.width * 0.72f, size.height * 0.52f), strokeWidth = 6f)
        drawLine(primary, Offset(size.width * 0.52f, size.height * 0.74f), Offset(size.width * 0.72f, size.height * 0.52f), strokeWidth = 6f)
    }
}
