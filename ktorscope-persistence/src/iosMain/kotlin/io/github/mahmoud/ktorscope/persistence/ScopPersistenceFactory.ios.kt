package io.github.mahmoud.ktorscope.persistence

/**
 * Created by Mahmoud kamal El-Din on 04/05/2026
 */
actual class ScopPersistenceFactory {
    actual fun create(
        databaseName: String,
        bodyDirectoryName: String
    ): KtorScopePersistence {
        val bodyFileStore = createIosNetworkBodyFileStore(
            directoryName = bodyDirectoryName,
        )
        return KtorScopePersistence(
            historyPersistence = createRoomKtorScopeHistoryPersistence(
                databaseName = databaseName,
                bodyFileStore = bodyFileStore,
            ),
            bodyFileStore = bodyFileStore,
        )
    }
}