/**
 * Created by Mahmoud kamal El-Din on 04/05/2026
 */
package io.github.mahmoud.ktorscope.persistence

import android.content.Context

actual class ScopPersistenceFactory(
    private val context: Context
) {


   actual fun create(
        databaseName: String ,
        bodyDirectoryName: String,
    ): KtorScopePersistence {
        val bodyFileStore = createAndroidNetworkBodyFileStore(
            context = context,
            directoryName = bodyDirectoryName,
        )
        return KtorScopePersistence(
            historyPersistence = createRoomKtorScopeHistoryPersistence(
                context = context,
                databaseName = databaseName,
                bodyFileStore = bodyFileStore,
            ),
            bodyFileStore = bodyFileStore,
        )

    }
}
