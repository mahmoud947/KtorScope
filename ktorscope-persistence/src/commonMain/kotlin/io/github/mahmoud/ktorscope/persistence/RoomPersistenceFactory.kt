/**
 * Created by Mahmoud kamal El-Din on 04/05/2026
 */
package io.github.mahmoud.ktorscope.persistence

import io.github.mahmoud.ktorscope.core.KtorScopeConfig
import io.github.mahmoud.ktorscope.core.KtorScopeHistoryPersistence

fun createRoomKtorScopeHistoryPersistence(
    database: NetworkInspectorDatabase,
    maxBodyPreviewSize: Long = KtorScopeConfig.DEFAULT_MAX_BODY_PREVIEW_SIZE,
    largeBodyFileThreshold: Long = KtorScopeConfig.DEFAULT_LARGE_BODY_FILE_THRESHOLD,
    bodyFileStore: NetworkBodyFileStore = NoOpNetworkBodyFileStore,
): KtorScopeHistoryPersistence {
    return RoomKtorScopeHistoryPersistence(
        database = database,
        maxBodyPreviewSize = maxBodyPreviewSize,
        largeBodyFileThreshold = largeBodyFileThreshold,
        bodyFileStore = bodyFileStore,
    )
}
