/**
 * Created by Mahmoud kamal El-Din on 04/05/2026
 */
package io.github.mahmoud.ktorscope.persistence

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor

@Database(
    entities = [NetworkTransactionEntity::class],
    version = 2,
    exportSchema = true,
)
@ConstructedBy(NetworkInspectorDatabaseConstructor::class)
abstract class NetworkInspectorDatabase : RoomDatabase() {
    abstract fun networkTransactionDao(): NetworkTransactionDao
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object NetworkInspectorDatabaseConstructor : RoomDatabaseConstructor<NetworkInspectorDatabase> {
    override fun initialize(): NetworkInspectorDatabase
}
