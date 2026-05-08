/**
 * Created by Mahmoud kamal El-Din on 08/05/2026
 */
package io.github.mahmoud.ktorscope.persistence

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection

val NetworkInspectorMigration1To2: Migration = object : Migration(1, 2) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSql("ALTER TABLE network_transactions ADD COLUMN protocol TEXT NOT NULL DEFAULT 'HTTP'")
        connection.execSql("ALTER TABLE network_transactions ADD COLUMN webSocketFrames TEXT")
    }
}

private fun SQLiteConnection.execSql(sql: String) {
    val statement = prepare(sql)
    try {
        statement.step()
    } finally {
        statement.close()
    }
}
