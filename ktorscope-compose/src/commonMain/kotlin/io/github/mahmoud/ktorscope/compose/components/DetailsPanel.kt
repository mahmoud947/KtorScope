/**
 * Created by Mahmoud kamal El-Din on 02/05/2026
 */
package io.github.mahmoud.ktorscope.compose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.mahmoud.ktorscope.core.NetworkTransaction
import io.github.mahmoud.ktorscope.core.toCurlCommand

@Composable
internal fun DetailsPanel(
    transaction: NetworkTransaction?,
    onBack: (() -> Unit)?,
    onCopy: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (transaction == null) {
        EmptyState(modifier.fillMaxSize())
        return
    }
    var selectedTab by remember(transaction.id) { mutableIntStateOf(0) }
    Column(
        modifier.background(MaterialTheme.colorScheme.background).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        if (onBack != null) {
            TextButton(onClick = onBack) { Text("Back") }
        }
        SummaryCard(transaction, onCopy)
        PrimaryTabRow(selectedTabIndex = selectedTab, containerColor = MaterialTheme.colorScheme.background) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Request") })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Response") })
            Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Error") })
        }
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            when (selectedTab) {
                0 -> {
                    HeadersSection(transaction.request.headers, onCopy)
                    BodySection(transaction.request.body, transaction.request.bodyTruncated, onCopy)
                }
                1 -> {
                    val response = transaction.response
                    if (response == null) {
                        EmptySection("No response captured")
                    } else {
                        SectionCard("Response summary") {
                            Text("${response.statusCode} ${response.statusDescription}", fontWeight = FontWeight.SemiBold)
                        }
                        HeadersSection(response.headers, onCopy)
                        BodySection(response.body, response.bodyTruncated, onCopy)
                    }
                }
                2 -> {
                    val error = transaction.error
                    if (error == null) {
                        EmptySection("No error captured")
                    } else {
                        SectionCard("Error") {
                            Text(error.type, color = ErrorColor, fontWeight = FontWeight.Bold)
                            Text(error.message.orEmpty(), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(transaction: NetworkTransaction, onCopy: (String) -> Unit) {
    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MethodChip(transaction.request.method)
                StatusChip(transaction)
                transaction.durationMillis?.let { AssistChip(onClick = {}, label = { Text("${it}ms") }) }
            }
            Text(transaction.request.url, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { onCopy(transaction.request.url) }, shape = RoundedCornerShape(14.dp)) {
                    Text("Copy URL")
                }
                OutlinedButton(onClick = { onCopy(transaction.request.headers.headersText()) }, shape = RoundedCornerShape(14.dp)) {
                    Text("Copy headers")
                }
                OutlinedButton(onClick = { onCopy(transaction.toCurlCommand()) }, shape = RoundedCornerShape(14.dp)) {
                    Text("Copy cURL")
                }
            }
        }
    }
}

@Composable
private fun HeadersSection(headers: Map<String, List<String>>, onCopy: (String) -> Unit) {
    SectionCard("Headers", action = {
        TextButton(onClick = { onCopy(headers.headersText()) }) { Text("Copy") }
    }) {
        if (headers.isEmpty()) {
            Text("No headers captured", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            headers.forEach { (name, values) ->
                Text("$name: ${values.joinToString()}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun BodySection(body: String?, truncated: Boolean, onCopy: (String) -> Unit) {
    val display = body?.prettyJsonOrSelf()
    SectionCard("Body preview", action = {
        TextButton(enabled = body != null, onClick = { if (body != null) onCopy(body) }) { Text("Copy") }
    }) {
        Text(
            text = when {
                display == null -> "No body captured"
                truncated -> "$display\n\n[truncated]"
                else -> display
            },
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun SectionCard(
    title: String,
    action: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                action?.invoke()
            }
            content()
        }
    }
}

@Composable
private fun EmptySection(text: String) {
    SectionCard(text) {
        Text("Nothing to show here.", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
