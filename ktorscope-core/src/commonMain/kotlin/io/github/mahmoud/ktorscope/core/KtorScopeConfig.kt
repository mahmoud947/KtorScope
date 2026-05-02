/**
 * Created by Mahmoud kamal El-Din on 02/05/2026
 */
package io.github.mahmoud.ktorscope.core

/**
 * Shared KtorScope configuration used by the Ktor plugin and UI.
 */
data class KtorScopeConfig(
    val enabled: Boolean = true,
    val captureBodies: Boolean = true,
    val maxBodySize: Int = DEFAULT_MAX_BODY_SIZE,
    val redactHeaders: Set<String> = DEFAULT_REDACT_HEADERS,
    val store: KtorScopeStore = KtorScopeStore.shared,
) {
    companion object {
        const val DEFAULT_MAX_BODY_SIZE: Int = 250_000

        val DEFAULT_REDACT_HEADERS: Set<String> = setOf(
            "Authorization",
            "Cookie",
            "Set-Cookie",
            "X-Api-Key",
        )
    }
}
