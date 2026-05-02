/**
 * Created by Mahmoud kamal El-Din on 02/05/2026
 */
package io.github.mahmoud.ktorscope.core

/**
 * Redacts sensitive header values before transactions are stored.
 */
object Redactor {
    private const val REDACTED = "██"

    fun redactHeaders(
        headers: Map<String, List<String>>,
        sensitiveHeaderNames: Set<String> = KtorScopeConfig.DEFAULT_REDACT_HEADERS,
    ): Map<String, List<String>> {
        if (headers.isEmpty()) return headers
        val sensitive = sensitiveHeaderNames.map { it.lowercase() }.toSet()
        return headers.mapValues { (name, values) ->
            if (name.lowercase() in sensitive) {
                values.map { REDACTED }
            } else {
                values
            }
        }
    }
}
