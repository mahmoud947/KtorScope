/**
 * Created by Mahmoud kamal El-Din on 02/05/2026
 */
package io.github.mahmoud.ktorscope.core

data class KtorScopeHistoryPersistenceConfig(
    val enabled: Boolean = false,
    val maxRecords: Int = 500,
    val maxBodyPreviewSize: Long = KtorScopeConfig.DEFAULT_MAX_BODY_PREVIEW_SIZE,
    val largeBodyFileThreshold: Long = KtorScopeConfig.DEFAULT_LARGE_BODY_FILE_THRESHOLD,
    val persistence: KtorScopeHistoryPersistence = NoOpKtorScopeHistoryPersistence,
)
