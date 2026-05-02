/**
 * Created by Mahmoud kamal El-Din on 02/05/2026
 */
package io.github.mahmoud.ktorscope.compose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.mahmoud.ktorscope.core.KtorScopeStore
import io.github.mahmoud.ktorscope.core.NetworkTransaction

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun KtorScopeScreen(
    store: KtorScopeStore = KtorScopeStore.shared,
    modifier: Modifier = Modifier,
) {
    val transactions by store.transactions.collectAsState()
    var selectedId by remember { mutableStateOf<String?>(null) }
    var query by remember { mutableStateOf("") }
    var methodFilter by remember { mutableStateOf("All") }
    var statusFilter by remember { mutableStateOf("All") }
    val methods = remember(transactions) {
        listOf("All") + transactions.map { it.request.method }.distinct().sorted()
    }
    val filtered = remember(transactions, query, methodFilter, statusFilter) {
        transactions.filter { transaction ->
            val matchesQuery = query.isBlank() ||
                transaction.request.url.contains(query, ignoreCase = true)
            val matchesMethod = methodFilter == "All" || transaction.request.method == methodFilter
            val status = transaction.response?.statusCode
            val matchesStatus = when (statusFilter) {
                "All" -> true
                "2xx" -> status in 200..299
                "3xx" -> status in 300..399
                "4xx" -> status in 400..499
                "5xx" -> status in 500..599
                "Failed" -> transaction.error != null
                else -> true
            }
            matchesQuery && matchesMethod && matchesStatus
        }
    }
    val selected = filtered.firstOrNull { it.id == selectedId } ?: filtered.firstOrNull()

    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val wide = maxWidth >= 840.dp
            if (wide) {
                Row(Modifier.fillMaxSize()) {
                    TransactionListPane(
                        transactions = filtered,
                        selectedId = selected?.id,
                        query = query,
                        onQueryChange = { query = it },
                        methods = methods,
                        methodFilter = methodFilter,
                        onMethodFilterChange = { methodFilter = it },
                        statusFilter = statusFilter,
                        onStatusFilterChange = { statusFilter = it },
                        onClear = {
                            store.clear()
                            selectedId = null
                        },
                        onSelect = { selectedId = it.id },
                        modifier = Modifier.width(380.dp).fillMaxHeight(),
                    )
                    VerticalDivider(Modifier.fillMaxHeight().width(1.dp))
                    TransactionDetailsPane(
                        transaction = selected,
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                    )
                }
            } else {
                if (selectedId == null) {
                    TransactionListPane(
                        transactions = filtered,
                        selectedId = null,
                        query = query,
                        onQueryChange = { query = it },
                        methods = methods,
                        methodFilter = methodFilter,
                        onMethodFilterChange = { methodFilter = it },
                        statusFilter = statusFilter,
                        onStatusFilterChange = { statusFilter = it },
                        onClear = { store.clear() },
                        onSelect = { selectedId = it.id },
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Column(Modifier.fillMaxSize()) {
                        Button(
                            onClick = { selectedId = null },
                            modifier = Modifier.padding(12.dp),
                        ) {
                            Text("Back")
                        }
                        TransactionDetailsPane(
                            transaction = selected,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionListPane(
    transactions: List<NetworkTransaction>,
    selectedId: String?,
    query: String,
    onQueryChange: (String) -> Unit,
    methods: List<String>,
    methodFilter: String,
    onMethodFilterChange: (String) -> Unit,
    statusFilter: String,
    onStatusFilterChange: (String) -> Unit,
    onClear: () -> Unit,
    onSelect: (NetworkTransaction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("KtorScope", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("${transactions.size} transactions", style = MaterialTheme.typography.bodySmall)
            }
            Button(onClick = onClear) {
                Text("Clear")
            }
        }
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            label = { Text("Search URL") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        ChipRow(values = methods, selected = methodFilter, onSelected = onMethodFilterChange)
        ChipRow(
            values = listOf("All", "2xx", "3xx", "4xx", "5xx", "Failed"),
            selected = statusFilter,
            onSelected = onStatusFilterChange,
        )
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(transactions, key = { it.id }) { transaction ->
                TransactionRow(
                    transaction = transaction,
                    selected = selectedId == transaction.id,
                    onClick = { onSelect(transaction) },
                    modifier = Modifier,
                )
            }
        }
    }
}

@Composable
private fun ChipRow(
    values: List<String>,
    selected: String,
    onSelected: (String) -> Unit,
) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        values.take(6).forEach { value ->
            FilterChip(
                selected = selected == value,
                onClick = { onSelected(value) },
                label = { Text(value, maxLines = 1) },
            )
        }
    }
}

@Composable
private fun TransactionRow(
    transaction: NetworkTransaction,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val container = if (selected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    Column(
        modifier
            .fillMaxWidth()
            .background(container, MaterialTheme.shapes.small)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AssistChip(onClick = onClick, label = { Text(transaction.request.method) })
            AssistChip(
                onClick = onClick,
                label = {
                    Text(transaction.response?.statusCode?.toString() ?: "ERR")
                },
            )
            transaction.durationMillis?.let {
                AssistChip(onClick = onClick, label = { Text("${it}ms") })
            }
        }
        Text(
            transaction.request.url,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun TransactionDetailsPane(
    transaction: NetworkTransaction?,
    modifier: Modifier = Modifier,
) {
    if (transaction == null) {
        Column(modifier.padding(24.dp)) {
            Text("No transactions yet", style = MaterialTheme.typography.titleMedium)
            Text("Trigger a request in the sample app to see it here.")
        }
        return
    }

    var selectedTab by remember(transaction.id) { mutableIntStateOf(0) }
    Column(modifier.padding(16.dp)) {
        Text(transaction.request.url, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AssistChip(onClick = {}, label = { Text(transaction.request.method) })
            AssistChip(onClick = {}, label = { Text(transaction.response?.statusCode?.toString() ?: "Failed") })
            transaction.durationMillis?.let { AssistChip(onClick = {}, label = { Text("${it}ms") }) }
        }
        Spacer(Modifier.height(12.dp))
        PrimaryTabRow(selectedTabIndex = selectedTab) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Request") })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Response") })
        }
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (selectedTab == 0) {
                HeadersSection(transaction.request.headers)
                BodySection(transaction.request.body, transaction.request.bodyTruncated)
            } else {
                val response = transaction.response
                if (response != null) {
                    Text("${response.statusCode} ${response.statusDescription}", style = MaterialTheme.typography.titleSmall)
                    HeadersSection(response.headers)
                    BodySection(response.body, response.bodyTruncated)
                }
                transaction.error?.let {
                    Section("Error") {
                        Text("${it.type}: ${it.message.orEmpty()}", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
private fun HeadersSection(headers: Map<String, List<String>>) {
    Section("Headers") {
        if (headers.isEmpty()) {
            Text("No headers captured", style = MaterialTheme.typography.bodySmall)
        } else {
            headers.forEach { (name, values) ->
                Text("$name: ${values.joinToString()}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun BodySection(body: String?, truncated: Boolean) {
    Section("Body") {
        Text(
            text = when {
                body == null -> "No body captured"
                truncated -> "$body\n\n[truncated]"
                else -> body
            },
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
        )
    }
}

@Composable
private fun Section(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        content()
    }
}
