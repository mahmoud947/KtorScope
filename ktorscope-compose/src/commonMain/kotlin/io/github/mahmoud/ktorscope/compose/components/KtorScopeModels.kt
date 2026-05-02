/**
 * Created by Mahmoud kamal El-Din on 02/05/2026
 */
package io.github.mahmoud.ktorscope.compose.components

import androidx.compose.ui.graphics.Color
import io.github.mahmoud.ktorscope.core.NetworkTransaction

internal enum class TransactionFilter(val label: String) {
    All("All"),
    Success("Success"),
    Error("Error"),
    Get("GET"),
    Post("POST"),
    Put("PUT"),
    Delete("DELETE");

    fun matches(transaction: NetworkTransaction): Boolean {
        val status = transaction.response?.statusCode
        return when (this) {
            All -> true
            Success -> transaction.error == null && status in 200..399
            Error -> transaction.error != null || status in 400..599
            Get -> transaction.request.method.equals("GET", ignoreCase = true)
            Post -> transaction.request.method.equals("POST", ignoreCase = true)
            Put -> transaction.request.method.equals("PUT", ignoreCase = true)
            Delete -> transaction.request.method.equals("DELETE", ignoreCase = true)
        }
    }
}

internal data class NetworkStats(
    val total: Int,
    val success: Int,
    val error: Int,
    val averageDuration: Long,
)

internal fun List<NetworkTransaction>.toStats(): NetworkStats {
    val durations = mapNotNull { it.durationMillis }
    return NetworkStats(
        total = size,
        success = count { it.error == null && it.response?.statusCode in 200..399 },
        error = count { it.error != null || it.response?.statusCode in 400..599 },
        averageDuration = if (durations.isEmpty()) 0 else durations.sum() / durations.size,
    )
}

internal fun NetworkTransaction.statusTone(): Color {
    val status = response?.statusCode
    return when {
        error != null -> ErrorColor
        status in 200..399 -> SuccessColor
        status in 400..599 -> ErrorColor
        else -> NeutralColor
    }
}

internal fun NetworkTransaction.bodySizeLabel(): String {
    val requestSize = request.body?.length ?: 0
    val responseSize = response?.body?.length ?: 0
    val total = requestSize + responseSize
    return if (total == 0) "No body" else "$total B"
}

internal fun String.hostPart(): String {
    return substringAfter("://", this).substringBefore("/")
}

internal fun String.pathPart(): String {
    val withoutScheme = substringAfter("://", this)
    val path = withoutScheme.substringAfter("/", missingDelimiterValue = "")
    return if (path.isBlank()) "/" else "/$path"
}

internal fun Long.timestampLabel(): String = "$this ms"

internal fun Map<String, List<String>>.headersText(): String {
    return entries.joinToString("\n") { (name, values) -> "$name: ${values.joinToString()}" }
}

internal fun String.prettyJsonOrSelf(): String {
    val trimmed = trim()
    if (!(trimmed.startsWith("{") && trimmed.endsWith("}")) && !(trimmed.startsWith("[") && trimmed.endsWith("]"))) {
        return this
    }
    val builder = StringBuilder()
    var indent = 0
    var inString = false
    var escaping = false
    trimmed.forEach { char ->
        when {
            escaping -> {
                builder.append(char)
                escaping = false
            }
            char == '\\' && inString -> {
                builder.append(char)
                escaping = true
            }
            char == '"' -> {
                builder.append(char)
                inString = !inString
            }
            inString -> builder.append(char)
            char == '{' || char == '[' -> {
                builder.append(char).append('\n')
                indent++
                builder.appendIndent(indent)
            }
            char == '}' || char == ']' -> {
                builder.append('\n')
                indent = (indent - 1).coerceAtLeast(0)
                builder.appendIndent(indent)
                builder.append(char)
            }
            char == ',' -> {
                builder.append(char).append('\n')
                builder.appendIndent(indent)
            }
            char == ':' -> builder.append(": ")
            !char.isWhitespace() -> builder.append(char)
        }
    }
    return builder.toString()
}

private fun StringBuilder.appendIndent(level: Int) {
    repeat(level) { append("  ") }
}

internal val SuccessColor = Color(0xFF22C55E)
internal val ErrorColor = Color(0xFFEF4444)
internal val NeutralColor = Color(0xFF64748B)
