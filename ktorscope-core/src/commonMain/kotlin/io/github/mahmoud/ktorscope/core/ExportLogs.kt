/**
 * Created by Mahmoud kamal El-Din on 02/05/2026
 */
package io.github.mahmoud.ktorscope.core

/**
 * Options for exporting captured KtorScope transactions.
 */
data class KtorScopeExportConfig(
    val title: String = "KtorScope Network Logs",
    val prettyPrintConfig: KtorScopePrettyPrintConfig = KtorScopePrettyPrintConfig(
        includeHeaders = true,
        includeBodies = true,
        includeCurl = true,
        includeGraphQl = true,
        prettyJson = true,
    ),
)

/**
 * Exports the current store transactions as a log-friendly text report.
 */
fun KtorScopeStore.exportLogs(config: KtorScopeExportConfig = KtorScopeExportConfig()): String {
    return transactions.value.exportKtorScopeLogs(config)
}

/**
 * Exports a list of transactions as a log-friendly text report.
 */
fun List<NetworkTransaction>.exportKtorScopeLogs(
    config: KtorScopeExportConfig = KtorScopeExportConfig(),
): String = buildString {
    appendLine(config.title)
    appendLine("=".repeat(config.title.length))
    appendLine("total: ${this@exportKtorScopeLogs.size}")
    appendLine()

    if (this@exportKtorScopeLogs.isEmpty()) {
        appendLine("No transactions captured.")
        return@buildString
    }

    this@exportKtorScopeLogs.forEachIndexed { index, transaction ->
        if (index > 0) {
            appendLine()
            appendLine("-----")
            appendLine()
        }
        appendLine(transaction.prettyPrint(config.prettyPrintConfig))
    }
}.trimEnd()
