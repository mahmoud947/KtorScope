/**
 * Created by Mahmoud kamal El-Din on 04/05/2026
 */
package io.github.mahmoud.ktorscope.persistence

import io.github.mahmoud.ktorscope.core.KtorScopeHistoryPersistence

data class KtorScopePersistence(
    val historyPersistence: KtorScopeHistoryPersistence,
    val bodyFileStore: NetworkBodyFileStore,
)

