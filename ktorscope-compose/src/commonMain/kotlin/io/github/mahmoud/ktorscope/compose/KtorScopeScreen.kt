/**
 * Created by Mahmoud kamal El-Din on 02/05/2026
 */
package io.github.mahmoud.ktorscope.compose

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.mahmoud.ktorscope.compose.components.DetailsPanel
import io.github.mahmoud.ktorscope.compose.components.KtorScopePreviewData
import io.github.mahmoud.ktorscope.compose.components.TransactionFilter
import io.github.mahmoud.ktorscope.compose.components.TransactionListPanel
import io.github.mahmoud.ktorscope.compose.components.toStats
import io.github.mahmoud.ktorscope.core.KtorScopeExportConfig
import io.github.mahmoud.ktorscope.core.KtorScopeStore
import io.github.mahmoud.ktorscope.core.exportKtorScopeLogs

private val DefaultCopyHandler: (String) -> Unit = {}
private val DefaultShareHandler: (String) -> Unit = {}

@Composable
fun KtorScopeScreen(
    store: KtorScopeStore = KtorScopeStore.shared,
    modifier: Modifier = Modifier,
    themeMode: KtorScopeThemeMode = KtorScopeThemeMode.System,
    onBackClicked: (() -> Unit)? = null,
    onThemeModeChange: (KtorScopeThemeMode) -> Unit = {},
    onCopy: (String) -> Unit = DefaultCopyHandler,
    onShare: (String) -> Unit = DefaultShareHandler,
) {
    var currentThemeMode by remember { mutableStateOf(themeMode) }
    val platformCopy = rememberKtorScopeClipboard()
    val platformShare = rememberKtorScopeShare()
    val copyHandler = if (onCopy === DefaultCopyHandler) platformCopy else onCopy
    val shareHandler = if (onShare === DefaultShareHandler) platformShare else onShare

    KtorScopeTheme(currentThemeMode) {
        Scaffold { padding ->
            KtorScopeContent(
                store = store,
                modifier = modifier.padding(padding),
                themeMode = currentThemeMode,
                onThemeModeChange = {
                    currentThemeMode = it
                    onThemeModeChange(it)
                },
                onBackClicked = onBackClicked,
                onCopy = copyHandler,
                onShare = shareHandler,
            )
        }
    }
}

@Preview
@Composable
private fun KtorScopeScreenLightPreview() {
    KtorScopeScreen(
        store = KtorScopeStore(KtorScopePreviewData.transactions),
        themeMode = KtorScopeThemeMode.Light,
        onCopy = {},
        onShare = {},
    )
}

@Preview
@Composable
private fun KtorScopeScreenDarkPreview() {
    KtorScopeScreen(
        store = KtorScopeStore(KtorScopePreviewData.transactions),
        themeMode = KtorScopeThemeMode.Dark,
        onCopy = {},
        onShare = {},
    )
}

@Composable
private fun KtorScopeContent(
    store: KtorScopeStore,
    modifier: Modifier,
    themeMode: KtorScopeThemeMode,
    onThemeModeChange: (KtorScopeThemeMode) -> Unit,
    onBackClicked: (() -> Unit)?,
    onCopy: (String) -> Unit,
    onShare: (String) -> Unit,
) {
    val transactions by store.transactions.collectAsState()
    var selectedId by remember { mutableStateOf<String?>(null) }
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(TransactionFilter.All) }
    val filtered = remember(transactions, query, filter) {
        transactions.filter { transaction ->
            val matchesQuery = query.isBlank() ||
                transaction.request.url.contains(query, ignoreCase = true)
            matchesQuery && filter.matches(transaction)
        }
    }
    val selected = transactions.firstOrNull { it.id == selectedId }
    val stats = remember(transactions) { transactions.toStats() }

    Surface(modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val wide = maxWidth >= 900.dp
            if (wide) {
                Row(Modifier.fillMaxSize()) {
                    TransactionListPanel(
                        transactions = filtered,
                        selectedId = selected?.id,
                        query = query,
                        onQueryChange = { query = it },
                        filter = filter,
                        onFilterChange = { filter = it },
                        stats = stats,
                        themeMode = themeMode,
                        onThemeModeChange = onThemeModeChange,
                        onBackClicked = onBackClicked,
                        onShareLogs = {
                            onShare(filtered.exportKtorScopeLogs(KtorScopeExportConfig()))
                        },
                        onClear = {
                            store.clear()
                            selectedId = null
                        },
                        onSelect = { selectedId = it.id },
                        modifier = Modifier.width(420.dp).fillMaxHeight(),
                    )
                    VerticalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.55f))
                    DetailsPanel(
                        transaction = selected ?: filtered.firstOrNull(),
                        onBack = null,
                        onCopy = onCopy,
                        onShare = onShare,
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                    )
                }
            } else {
                AnimatedContent(targetState = selectedId != null, label = "screen") { showingDetails ->
                    if (showingDetails) {
                        DetailsPanel(
                            transaction = transactions.firstOrNull { it.id == selectedId },
                            onBack = { selectedId = null },
                            onCopy = onCopy,
                            onShare = onShare,
                            modifier = Modifier.fillMaxSize(),
                        )
                    } else {
                        TransactionListPanel(
                            transactions = filtered,
                            selectedId = null,
                            query = query,
                            onQueryChange = { query = it },
                            filter = filter,
                            onFilterChange = { filter = it },
                            stats = stats,
                            themeMode = themeMode,
                            onThemeModeChange = onThemeModeChange,
                            onBackClicked = onBackClicked,
                            onShareLogs = {
                                onShare(filtered.exportKtorScopeLogs(KtorScopeExportConfig()))
                            },
                            onClear = { store.clear() },
                            onSelect = { selectedId = it.id },
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }
        }
    }
}
