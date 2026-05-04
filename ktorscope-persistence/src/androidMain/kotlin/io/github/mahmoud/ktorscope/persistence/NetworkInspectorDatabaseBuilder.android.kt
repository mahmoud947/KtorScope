/**
 * Created by Mahmoud kamal El-Din on 04/05/2026
 */
package io.github.mahmoud.ktorscope.persistence

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.github.mahmoud.ktorscope.core.KtorScopeConfig
import io.github.mahmoud.ktorscope.core.KtorScopeHistoryPersistence
import kotlinx.coroutines.Dispatchers

fun createNetworkInspectorDatabaseBuilder(
    context: Context,
    databaseName: String = "network_inspector.db",
): RoomDatabase.Builder<NetworkInspectorDatabase> {
    return Room.databaseBuilder<NetworkInspectorDatabase>(
        context = context,
        name = context.getDatabasePath(databaseName).absolutePath,
    )
}

fun createNetworkInspectorDatabase(
    context: Context,
    databaseName: String = "network_inspector.db",
): NetworkInspectorDatabase {
    return createNetworkInspectorDatabaseBuilder(context, databaseName)
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.Default)
        .build()
}

 fun createAndroidNetworkBodyFileStore(
    context: Context,
    directoryName: String = "network_inspector_bodies",
): NetworkBodyFileStore {
    return PlatformNetworkBodyFileStore(
        rootDirectoryPath = context.filesDir.resolve(directoryName).absolutePath,
    )
}

fun createRoomKtorScopeHistoryPersistence(
    context: Context,
    databaseName: String = "network_inspector.db",
    maxBodyPreviewSize: Long = KtorScopeConfig.DEFAULT_MAX_BODY_PREVIEW_SIZE,
    largeBodyFileThreshold: Long = KtorScopeConfig.DEFAULT_LARGE_BODY_FILE_THRESHOLD,
    bodyFileStore: NetworkBodyFileStore = createAndroidNetworkBodyFileStore(context),
): KtorScopeHistoryPersistence {
    return createRoomKtorScopeHistoryPersistence(
        database = createNetworkInspectorDatabase(context, databaseName),
        maxBodyPreviewSize = maxBodyPreviewSize,
        largeBodyFileThreshold = largeBodyFileThreshold,
        bodyFileStore = bodyFileStore,
    )
}
