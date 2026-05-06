/**
 * Created by Mahmoud kamal El-Din on 02/05/2026
 */
package io.github.mahmoud.ktorscope.compose.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.mahmoud.ktorscope.compose.KtorScopeTheme
import io.github.mahmoud.ktorscope.compose.KtorScopeThemeMode
import io.github.mahmoud.ktorscope.core.KtorScopeExportConfig
import io.github.mahmoud.ktorscope.core.NetworkTransaction
import io.github.mahmoud.ktorscope.core.exportKtorScopeLogs
import io.github.mahmoud.ktorscope.core.graphQlOperation
import io.github.mahmoud.ktorscope.core.toCurlCommand
import kotlinx.coroutines.launch

@Composable
internal fun DetailsPanel(
    transaction: NetworkTransaction?,
    onBack: (() -> Unit)?,
    onCopy: (String) -> Unit,
    onShare: (String) -> Unit,
    onLoadFullBody: suspend (String) -> String? = { null },
    modifier: Modifier = Modifier,
) {
    if (transaction == null) {
        EmptyState(modifier.fillMaxSize())
        return
    }
    var selectedTab by remember(transaction.id) { mutableIntStateOf(0) }
    var fullBodyPreview by remember(transaction.id) { mutableStateOf<BodyPreviewTarget?>(null) }
    fullBodyPreview?.let { target ->
        FullBodyPreviewScreen(
            target = target,
            onBack = { fullBodyPreview = null },
            modifier = modifier,
        )
        return
    }
    Column(
        modifier.background(MaterialTheme.colorScheme.background).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        if (onBack != null) {
            TextButton(onClick = onBack) { Text("Back") }
        }
        SummaryCard(transaction = transaction, onCopy = onCopy, onShare = onShare)
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
                    GraphQlSection(transaction, onCopy)
                    HeadersSection(transaction.request.headers, onCopy)
                    BodySection(
                        title = "Request body",
                        body = transaction.request.body,
                        truncated = transaction.request.bodyTruncated,
                        filePath = transaction.request.bodyFilePath,
                        bodySizeBytes = transaction.request.bodySizeBytes,
                        onCopy = onCopy,
                        onLoadFullBody = onLoadFullBody,
                        onShowMore = { fullBodyPreview = it },
                    )
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
                        BodySection(
                            title = "Response body",
                            body = response.body,
                            truncated = response.bodyTruncated,
                            filePath = response.bodyFilePath,
                            bodySizeBytes = response.bodySizeBytes,
                            onCopy = onCopy,
                            onLoadFullBody = onLoadFullBody,
                            onShowMore = { fullBodyPreview = it },
                        )
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
private fun GraphQlSection(transaction: NetworkTransaction, onCopy: (String) -> Unit) {
    val operation = remember(transaction.id, transaction.request.body) {
        transaction.graphQlOperation()
    } ?: return
    SectionCard("GraphQL", action = {
        TextButton(onClick = { onCopy(operation.query) }) { Text("Copy query") }
    }) {
        Text(
            "${operation.operationType.orEmpty()} ${operation.operationName.orEmpty()}".trim().ifBlank { "Anonymous operation" },
            fontWeight = FontWeight.Bold,
        )
        Text(operation.query, style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
        operation.variables?.takeIf { it.isNotBlank() && it != "null" }?.let { variables ->
            Text("Variables", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Text(variables.prettyJsonOrSelf(), style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
        }
    }
}

@Composable
private fun SummaryCard(
    transaction: NetworkTransaction,
    onCopy: (String) -> Unit,
    onShare: (String) -> Unit,
) {
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
                Spacer(Modifier.weight(1f))
                IconButton(
                    onClick = {
                        onShare(listOf(transaction).exportKtorScopeLogs(KtorScopeExportConfig()))
                    },
                ) {
                    ShareIcon(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
            Text(transaction.request.url, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
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
private fun ShareIcon(
    color: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier) {
        val start = Offset(size.width * 0.30f, size.height * 0.50f)
        val topEnd = Offset(size.width * 0.72f, size.height * 0.28f)
        val bottomEnd = Offset(size.width * 0.72f, size.height * 0.72f)
        val strokeWidth = 2.dp.toPx()

        drawLine(
            color = color,
            start = start,
            end = topEnd,
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = color,
            start = start,
            end = bottomEnd,
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
        drawCircle(color = color, radius = 3.dp.toPx(), center = start)
        drawCircle(color = color, radius = 3.dp.toPx(), center = topEnd)
        drawCircle(color = color, radius = 3.dp.toPx(), center = bottomEnd)
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
private fun BodySection(
    title: String,
    body: String?,
    truncated: Boolean,
    filePath: String?,
    bodySizeBytes: Long?,
    onCopy: (String) -> Unit,
    onLoadFullBody: suspend (String) -> String?,
    onShowMore: (BodyPreviewTarget) -> Unit,
) {
    val scope = rememberCoroutineScope()
    var loadedBody by remember(filePath) { mutableStateOf<String?>(null) }
    var loading by remember(filePath) { mutableStateOf(false) }
    val visibleBody = loadedBody ?: body
    val formatter = remember { FastJsonPreviewFormatter(maxLines = InlineBodyPreviewLines) }
    val previewLines = remember(visibleBody) { mutableStateListOf<JsonLine>() }
    var previewState by remember(visibleBody) {
        mutableStateOf(
            JsonPreviewState(
                isLoading = visibleBody != null,
                lines = previewLines,
            ),
        )
    }
    var hasMoreLines by remember(visibleBody) { mutableStateOf(false) }
    val bodyDescription = remember(filePath, body, bodySizeBytes) {
        when {
            filePath != null -> "Stored as preview + file${bodySizeBytes?.let { " ($it B)" }.orEmpty()}"
            body != null -> "Stored as database preview${bodySizeBytes?.let { " ($it B)" }.orEmpty()}"
            else -> "No stored body"
        }
    }

    LaunchedEffect(visibleBody, truncated, loadedBody) {
        previewLines.clear()
        hasMoreLines = false
        previewState = JsonPreviewState(isLoading = visibleBody != null, lines = previewLines)
        val source = visibleBody
        if (source == null) {
            previewLines.add(JsonLine(0, listOf(JsonToken.Plain("No body captured"))))
            previewState = JsonPreviewState(isLoading = false, lines = previewLines)
            return@LaunchedEffect
        }
        formatter.format(source).collect { chunk ->
            previewLines.addAll(chunk.lines)
            if (chunk.isLimited) hasMoreLines = true
            val truncationLine = if (chunk.isComplete && truncated && loadedBody == null) {
                JsonLine(previewLines.size, listOf(JsonToken.Plain("[truncated]")))
            } else {
                null
            }
            truncationLine?.let(previewLines::add)
            previewState = JsonPreviewState(
                isLoading = !chunk.isComplete,
                lines = previewLines,
                error = chunk.error,
            )
        }
    }

    SectionCard("Body preview", action = {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            if (filePath != null) {
                TextButton(
                    enabled = !loading,
                    onClick = {
                        loading = true
                        scope.launch {
                            loadedBody = onLoadFullBody(filePath)
                            loading = false
                        }
                    },
                ) {
                    Text(if (loading) "Loading" else "Load full body")
                }
            }
            TextButton(enabled = visibleBody != null, onClick = { visibleBody?.let(onCopy) }) { Text("Copy") }
        }
    }) {
        Text(
            text = bodyDescription,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        JsonBodyPreview(state = previewState)
        if (visibleBody != null && visibleBody.isNotEmpty() && (hasMoreLines || truncated)) {
            TextButton(
                onClick = {
                    onShowMore(
                        BodyPreviewTarget(
                            title = title,
                            body = visibleBody,
                            truncated = truncated && loadedBody == null,
                        ),
                    )
                },
            ) {
                Text("Show more")
            }
        }
    }
}

@Composable
private fun FullBodyPreviewScreen(
    target: BodyPreviewTarget,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val formatter = remember { FastJsonPreviewFormatter() }
    val lines = remember(target.body) { mutableStateListOf<JsonLine>() }
    var state by remember(target.body) {
        mutableStateOf(JsonPreviewState(isLoading = true, lines = lines))
    }

    LaunchedEffect(target) {
        lines.clear()
        state = JsonPreviewState(isLoading = true, lines = lines)
        formatter.format(target.body).collect { chunk ->
            lines.addAll(chunk.lines)
            val truncationLine = if (chunk.isComplete && target.truncated) {
                JsonLine(lines.size, listOf(JsonToken.Plain("[truncated]")))
            } else {
                null
            }
            truncationLine?.let(lines::add)
            state = JsonPreviewState(
                isLoading = !chunk.isComplete,
                lines = lines,
                error = chunk.error,
            )
        }
    }

    Column(
        modifier.background(MaterialTheme.colorScheme.background).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = onBack) { Text("Back") }
            Text(target.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        JsonBodyPreview(
            state = state,
            modifier = Modifier.weight(1f),
            maxHeight = null,
        )
    }
}

@Composable
private fun JsonBodyPreview(
    state: JsonPreviewState,
    modifier: Modifier = Modifier,
    maxHeight: Dp? = 420.dp,
) {
    val horizontalScrollState = rememberScrollState()
    val colors = JsonTokenColors(
        key = Color(0xFF38BDF8),
        string = Color(0xFF22C55E),
        number = Color(0xFFF59E0B),
        literal = Color(0xFFA78BFA),
        punctuation = MaterialTheme.colorScheme.onSurfaceVariant,
        plain = MaterialTheme.colorScheme.onSurface,
    )
    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(if (maxHeight != null) Modifier.heightIn(max = maxHeight) else Modifier)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                shape = RoundedCornerShape(10.dp),
            )
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (state.isLoading && state.lines.isEmpty()) {
            Text(
                text = "Formatting body...",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false)
                .horizontalScroll(horizontalScrollState),
        ) {
            items(state.lines, key = { it.index }) { line ->
                JsonLineText(line = line, colors = colors)
            }
        }
        state.error?.let { error ->
            Text(
                text = error,
                style = MaterialTheme.typography.labelSmall,
                color = ErrorColor,
            )
        }
        if (state.isLoading && state.lines.isNotEmpty()) {
            Text(
                text = "Formatting...",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun JsonLineText(
    line: JsonLine,
    colors: JsonTokenColors,
) {
    val text = remember(line, colors) { line.toAnnotatedString(colors) }
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        fontFamily = FontFamily.Monospace,
        softWrap = false,
        overflow = TextOverflow.Clip,
        color = colors.plain,
    )
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

private const val InlineBodyPreviewLines = 10

private data class BodyPreviewTarget(
    val title: String,
    val body: String,
    val truncated: Boolean,
)

private data class JsonTokenColors(
    val key: Color,
    val string: Color,
    val number: Color,
    val literal: Color,
    val punctuation: Color,
    val plain: Color,
)

private fun JsonLine.toAnnotatedString(colors: JsonTokenColors): AnnotatedString = buildAnnotatedString {
    tokens.forEach { token ->
        val color = when (token) {
            is JsonToken.Key -> colors.key
            is JsonToken.StringValue -> colors.string
            is JsonToken.NumberValue -> colors.number
            is JsonToken.BooleanValue -> colors.literal
            JsonToken.NullValue -> colors.literal
            is JsonToken.Symbol -> colors.punctuation
            is JsonToken.Plain -> colors.plain
        }
        withStyle(SpanStyle(color = color)) {
            append(
                when (token) {
                    is JsonToken.Key -> token.text
                    is JsonToken.StringValue -> token.text
                    is JsonToken.NumberValue -> token.text
                    is JsonToken.BooleanValue -> token.text
                    JsonToken.NullValue -> "null"
                    is JsonToken.Symbol -> token.text
                    is JsonToken.Plain -> token.text
                },
            )
        }
    }
}

@Preview
@Composable
private fun DetailsPanelSuccessPreview() {
    KtorScopeTheme(KtorScopeThemeMode.Light) {
        DetailsPanel(
            transaction = KtorScopePreviewData.transactions.first(),
            onBack = {},
            onCopy = {},
            onShare = {},
        )
    }
}

@Preview
@Composable
private fun DetailsPanelGraphQlPreview() {
    KtorScopeTheme(KtorScopeThemeMode.Dark) {
        DetailsPanel(
            transaction = KtorScopePreviewData.transactions[1],
            onBack = {},
            onCopy = {},
            onShare = {},
        )
    }
}
