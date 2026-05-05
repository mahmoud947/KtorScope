package io.github.mahmoud.ktorscope.compose.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.mahmoud.ktorscope.compose.KtorScopeTheme
import io.github.mahmoud.ktorscope.compose.KtorScopeThemeMode
import io.github.mahmoud.ktorscope.core.NetworkTransaction
import io.github.mahmoud947.ktorscope_compose.generated.resources.Res
import io.github.mahmoud947.ktorscope_compose.generated.resources.keyboard_arrow_up
import io.github.mahmoud947.ktorscope_compose.generated.resources.search

import org.jetbrains.compose.resources.painterResource

/**
 * Created by Mahmoud kamal El-Din on 02/05/2026
 */
@Composable
internal fun TransactionListPanel(
    transactions: List<NetworkTransaction>,
    selectedId: String?,
    query: String,
    onQueryChange: (String) -> Unit,
    filter: TransactionFilter,
    onFilterChange: (TransactionFilter) -> Unit,
    stats: NetworkStats,
    historyMode: KtorScopeHistoryMode,
    persistHistory: Boolean,
    themeMode: KtorScopeThemeMode,
    onThemeModeChange: (KtorScopeThemeMode) -> Unit,
    onHistoryModeChange: (KtorScopeHistoryMode) -> Unit,
    onBackClicked: (() -> Unit)?,
    onShareLogs: () -> Unit,
    onClear: () -> Unit,
    onClearPersistedHistory: () -> Unit,
    onSelect: (NetworkTransaction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val headerToggleThreshold = with(LocalDensity.current) { 8.dp.toPx() }
    var showHeader by remember { mutableStateOf(true) }
    val headerScrollConnection = remember(headerToggleThreshold) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (source == NestedScrollSource.UserInput) {
                    when {
                        available.y < -headerToggleThreshold -> showHeader = false
                        available.y > headerToggleThreshold -> showHeader = true
                    }
                }
                return Offset.Zero
            }
        }
    }

    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .nestedScroll(headerScrollConnection)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        AnimatedVisibility(
            visible = showHeader,
            enter = fadeIn(animationSpec = tween(durationMillis = 200)) + expandVertically(),
            exit = fadeOut(animationSpec = tween(durationMillis = 200)) + shrinkVertically(),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                TopBar(
                    themeMode = themeMode,
                    onThemeModeChange = onThemeModeChange,
                )

                if (onBackClicked != null) {
                    OutlinedButton(
                        onClick = onBackClicked,
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Text("Back")
                    }
                }

                StatsRow(stats)

                HistoryRow(
                    historyMode = historyMode,
                    persistHistory = persistHistory,
                    onHistoryModeChange = onHistoryModeChange,
                )

                OutlinedTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    label = { Text("Search URL") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                )

                FilterRow(
                    filter = filter,
                    onFilterChange = onFilterChange,
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
                ) {
                    OutlinedButton(
                        onClick = onShareLogs,
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Text("Share logs")
                    }

                    OutlinedButton(
                        onClick = onClear,
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Text("Clear logs")
                    }

                    if (persistHistory) {
                        OutlinedButton(
                            onClick = onClearPersistedHistory,
                            shape = RoundedCornerShape(14.dp),
                        ) {
                            Text("Clear history")
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = !showHeader,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            CollapsedHeaderActions(
                onExpand = { showHeader = true },
            )
        }

        Crossfade(
            targetState = transactions.isEmpty(),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            label = "empty",
        ) { empty ->
            if (empty) {
                EmptyState(Modifier.fillMaxSize())
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(
                        items = transactions,
                        key = { it.id },
                        contentType = { "transaction" },
                    ) { transaction ->
                        TransactionCard(
                            transaction = transaction,
                            selected = selectedId == transaction.id,
                            onClick = { onSelect(transaction) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CollapsedHeaderActions(
    onExpand: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ElevatedCard(
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp),
            shape = RoundedCornerShape(999.dp),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onExpand) {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(Res.drawable.search),
                        contentDescription = "Show search",
                    )
                }

                IconButton(onClick = onExpand) {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(Res.drawable.keyboard_arrow_up),
                        contentDescription = "Show header",
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryRow(
    historyMode: KtorScopeHistoryMode,
    persistHistory: Boolean,
    onHistoryModeChange: (KtorScopeHistoryMode) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = if (persistHistory) "Session history: persisted" else "Session history: memory only",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (persistHistory) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                KtorScopeHistoryMode.entries.forEach { mode ->
                    FilterChip(
                        selected = historyMode == mode,
                        onClick = { onHistoryModeChange(mode) },
                        label = { Text(mode.label) },
                        shape = RoundedCornerShape(999.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun TopBar(
    themeMode: KtorScopeThemeMode,
    onThemeModeChange: (KtorScopeThemeMode) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                text = "KtorScope",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )

            Text(
                text = "Ktor network inspector",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Box {
            OutlinedButton(
                onClick = { expanded = true },
                shape = RoundedCornerShape(14.dp),
            ) {
                Text(themeMode.name)
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                KtorScopeThemeMode.entries.forEach { mode ->
                    DropdownMenuItem(
                        text = { Text(mode.name) },
                        onClick = {
                            expanded = false
                            onThemeModeChange(mode)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun StatsRow(stats: NetworkStats) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        StatPill("Total", stats.total.toString())
        StatPill("Success", stats.success.toString(), SuccessColor)
        StatPill("Error", stats.error.toString(), ErrorColor)
        StatPill("Avg", "${stats.averageDuration}ms")
    }
}

@Composable
private fun StatPill(
    label: String,
    value: String,
    accent: Color = MaterialTheme.colorScheme.primary,
) {
    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(18.dp),
    ) {
        Column(
            modifier = Modifier
                .width(92.dp)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = accent,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun FilterRow(
    filter: TransactionFilter,
    onFilterChange: (TransactionFilter) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TransactionFilter.entries.forEach { item ->
            FilterChip(
                selected = filter == item,
                onClick = { onFilterChange(item) },
                label = { Text(item.label) },
                shape = RoundedCornerShape(999.dp),
            )
        }
    }
}

@Composable
private fun TransactionCard(
    transaction: NetworkTransaction,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val tone = transaction.statusTone()
    val host = remember(transaction.id, transaction.request.url) {
        transaction.request.url.hostPart()
    }
    val path = remember(transaction.id, transaction.request.url) {
        transaction.request.url.pathPart()
    }
    val timestamp = remember(transaction.id, transaction.createdAtMillis) {
        transaction.createdAtMillis.timestampLabel()
    }
    val bodySize = remember(
        transaction.id,
        transaction.request.body,
        transaction.request.bodySizeBytes,
        transaction.response?.body,
        transaction.response?.bodySizeBytes,
    ) {
        transaction.bodySizeLabel()
    }

    val background by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        label = "cardColor",
    )

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.elevatedCardColors(containerColor = background),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = if (selected) 7.dp else 2.dp,
        ),
        shape = RoundedCornerShape(22.dp),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                StatusDot(tone)
                MethodChip(transaction.request.method)
                StatusChip(transaction)

                Spacer(Modifier.weight(1f))

                Text(
                    text = "${transaction.durationMillis ?: 0}ms",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = host,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Text(
                    text = path,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = timestamp,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Text(
                    text = bodySize,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            val error = transaction.error
            if (error != null) {
                Text(
                    text = "Error: ${error.message.orEmpty()}",
                    style = MaterialTheme.typography.labelMedium,
                    color = ErrorColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Preview
@Composable
private fun TransactionListPanelPreview() {
    KtorScopeTheme(KtorScopeThemeMode.Light) {
        TransactionListPanel(
            transactions = KtorScopePreviewData.transactions,
            selectedId = KtorScopePreviewData.transactions.first().id,
            query = "",
            onQueryChange = {},
            filter = TransactionFilter.All,
            onFilterChange = {},
            stats = KtorScopePreviewData.transactions.toStats(),
            historyMode = KtorScopeHistoryMode.CurrentSession,
            persistHistory = true,
            themeMode = KtorScopeThemeMode.Light,
            onThemeModeChange = {},
            onHistoryModeChange = {},
            onBackClicked = {},
            onShareLogs = {},
            onClear = {},
            onClearPersistedHistory = {},
            onSelect = {},
        )
    }
}

@Preview
@Composable
private fun TransactionCardPreview() {
    KtorScopeTheme(KtorScopeThemeMode.Dark) {
        TransactionCard(
            transaction = KtorScopePreviewData.transactions.first(),
            selected = true,
            onClick = {},
        )
    }
}
