/**
 * Created by Mahmoud kamal El-Din on 04/05/2026
 */
package io.github.mahmoud.ktorscope.persistence

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(
    tableName = "network_transactions",
    indices = [
        Index(value = ["timestampMs"]),
        Index(value = ["method"]),
        Index(value = ["statusCode"]),
        Index(value = ["host"]),
        Index(value = ["url"]),
    ],
)
data class NetworkTransactionEntity(
    @PrimaryKey val id: String,
    val method: String,
    val url: String,
    val host: String?,
    val path: String?,
    val statusCode: Int?,
    val durationMs: Long?,
    val timestampMs: Long,
    val requestHeadersJson: String?,
    val responseHeadersJson: String?,
    val requestBodyPreview: String?,
    val responseBodyPreview: String?,
    val requestBodyFilePath: String?,
    val responseBodyFilePath: String?,
    val requestBodySizeBytes: Long?,
    val responseBodySizeBytes: Long?,
    val errorMessage: String?,
    val errorType: String?,
    val isFromCache: Boolean,
    @ColumnInfo(defaultValue = "'HTTP'")
    val protocol: String,
    val webSocketFrames: String?,
    val createdAtMs: Long,
)
