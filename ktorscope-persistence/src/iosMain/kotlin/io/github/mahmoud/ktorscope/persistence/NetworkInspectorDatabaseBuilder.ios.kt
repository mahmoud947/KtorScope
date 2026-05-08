/**
 * Created by Mahmoud kamal El-Din on 04/05/2026
 */
package io.github.mahmoud.ktorscope.persistence

import androidx.room.RoomDatabase
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.github.mahmoud.ktorscope.core.KtorScopeConfig
import io.github.mahmoud.ktorscope.core.KtorScopeHistoryPersistence
import kotlinx.coroutines.Dispatchers
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask
import platform.Foundation.NSURL

fun createNetworkInspectorDatabaseBuilder(
    databaseName: String = "network_inspector.db",
): RoomDatabase.Builder<NetworkInspectorDatabase> {
    return Room.databaseBuilder<NetworkInspectorDatabase>(
        name = defaultNetworkInspectorDirectoryPath() + "/$databaseName",
    )
}

fun createNetworkInspectorDatabase(
    databaseName: String = "network_inspector.db",
): NetworkInspectorDatabase {
    return createNetworkInspectorDatabaseBuilder(databaseName)
        .setDriver(BundledSQLiteDriver())
        .addMigrations(NetworkInspectorMigration1To2)
        .setQueryCoroutineContext(Dispatchers.Default)
        .build()
}

fun createIosNetworkBodyFileStore(
    directoryName: String = "network_inspector_bodies",
): NetworkBodyFileStore {
    return PlatformNetworkBodyFileStore(
        rootDirectoryPath = defaultNetworkInspectorDirectoryPath() + "/$directoryName",
    )
}

fun createRoomKtorScopeHistoryPersistence(
    databaseName: String = "network_inspector.db",
    maxBodyPreviewSize: Long = KtorScopeConfig.DEFAULT_MAX_BODY_PREVIEW_SIZE,
    largeBodyFileThreshold: Long = KtorScopeConfig.DEFAULT_LARGE_BODY_FILE_THRESHOLD,
    bodyFileStore: NetworkBodyFileStore = createIosNetworkBodyFileStore(),
): KtorScopeHistoryPersistence {
    return createRoomKtorScopeHistoryPersistence(
        database = createNetworkInspectorDatabase(databaseName),
        maxBodyPreviewSize = maxBodyPreviewSize,
        largeBodyFileThreshold = largeBodyFileThreshold,
        bodyFileStore = bodyFileStore,
    )
}


private fun defaultNetworkInspectorDirectoryPath(): String {
    val manager = NSFileManager.defaultManager
    val documents = manager.URLsForDirectory(NSDocumentDirectory, NSUserDomainMask).firstOrNull() as? NSURL
        ?: error("Documents directory is unavailable")
    return documents.path.orEmpty()
}
