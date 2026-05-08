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
    val persistHistory: Boolean = false,
    val maxHistoryRecords: Int = 500,
    val maxBodyPreviewSize: Long = DEFAULT_MAX_BODY_PREVIEW_SIZE,
    val captureWebSocketFrames: Boolean = true,
    val maxWebSocketFramePreviewSize: Long = DEFAULT_MAX_WEBSOCKET_FRAME_PREVIEW_SIZE,
    val largeBodyFileThreshold: Long = DEFAULT_LARGE_BODY_FILE_THRESHOLD,
    val redactHeaders: Set<String> = DEFAULT_REDACT_HEADERS,
    val store: KtorScopeStore = KtorScopeStore.shared,
    val persistence: KtorScopeHistoryPersistence = NoOpKtorScopeHistoryPersistence,
) {
    val maxBodySize: Int
        get() = maxBodyPreviewSize.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()

    companion object {
        const val DEFAULT_MAX_BODY_SIZE: Int = 250_000
        const val DEFAULT_MAX_BODY_PREVIEW_SIZE: Long = 250_000
        const val DEFAULT_MAX_WEBSOCKET_FRAME_PREVIEW_SIZE: Long = 64_000
        const val DEFAULT_LARGE_BODY_FILE_THRESHOLD: Long = 250_000

        val DEFAULT_REDACT_HEADERS: Set<String> = setOf(
            "Authorization",
            "Cookie",
            "Set-Cookie",
            "X-Api-Key",
            "Api-Key",
            "access_token",
            "refresh_token",
        )
    }
}

typealias NetworkInspectorConfig = KtorScopeConfig
