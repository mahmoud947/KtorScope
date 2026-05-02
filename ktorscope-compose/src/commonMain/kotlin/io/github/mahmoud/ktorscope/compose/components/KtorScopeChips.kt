/**
 * Created by Mahmoud kamal El-Din on 02/05/2026
 */
package io.github.mahmoud.ktorscope.compose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.mahmoud.ktorscope.compose.KtorScopeTheme
import io.github.mahmoud.ktorscope.compose.KtorScopeThemeMode
import io.github.mahmoud.ktorscope.core.NetworkTransaction

@Composable
internal fun MethodChip(method: String) {
    val color = when (method.uppercase()) {
        "GET" -> Color(0xFF2563EB)
        "POST" -> Color(0xFF7C3AED)
        "PUT" -> Color(0xFF0F766E)
        "PATCH" -> Color(0xFFCA8A04)
        "DELETE" -> ErrorColor
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(color = color.copy(alpha = 0.14f), contentColor = color, shape = RoundedCornerShape(999.dp)) {
        Text(
            method.uppercase(),
            Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
internal fun StatusChip(transaction: NetworkTransaction) {
    val status = transaction.response?.statusCode
    val color = transaction.statusTone()
    Surface(color = color.copy(alpha = 0.14f), contentColor = color, shape = RoundedCornerShape(999.dp)) {
        Text(
            status?.toString() ?: "ERR",
            Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
internal fun StatusDot(color: Color) {
    Box(Modifier.size(10.dp).clip(CircleShape).background(color))
}

@Preview
@Composable
private fun KtorScopeChipsPreview() {
    KtorScopeTheme(KtorScopeThemeMode.Light) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatusDot(SuccessColor)
            MethodChip("GET")
            MethodChip("POST")
            MethodChip("DELETE")
            StatusChip(KtorScopePreviewData.transactions.first())
        }
    }
}
