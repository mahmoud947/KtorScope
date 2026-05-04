/**
 * Created by Mahmoud kamal El-Din on 04/05/2026
 */
package io.github.mahmoud.ktorscope.persistence

import io.github.mahmoud.ktorscope.core.KtorScopeConfig
import io.github.mahmoud.ktorscope.core.KtorScopeHistoryPersistence
import io.github.mahmoud.ktorscope.core.NetworkTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomKtorScopeHistoryPersistence(
    private val database: NetworkInspectorDatabase,
    maxBodyPreviewSize: Long = KtorScopeConfig.DEFAULT_MAX_BODY_PREVIEW_SIZE,
    largeBodyFileThreshold: Long = KtorScopeConfig.DEFAULT_LARGE_BODY_FILE_THRESHOLD,
    private val bodyFileStore: NetworkBodyFileStore = NoOpNetworkBodyFileStore,
) : KtorScopeHistoryPersistence {
    private val dao = database.networkTransactionDao()
    private val mapper = NetworkTransactionEntityMapper(
        maxBodyPreviewSize = maxBodyPreviewSize,
        largeBodyFileThreshold = largeBodyFileThreshold,
        bodyFileStore = bodyFileStore,
    )

    override suspend fun saveTransaction(transaction: NetworkTransaction) {
        dao.insertTransaction(mapper.toEntity(transaction))
    }

    override suspend fun observeTransactions(): Flow<List<NetworkTransaction>> {
        return dao.observeTransactions().map { entities -> entities.map(mapper::toDomain) }
    }

    override suspend fun getTransactionById(id: String): NetworkTransaction? {
        return dao.getTransactionById(id)?.let(mapper::toDomain)
    }

    override suspend fun deleteTransaction(id: String) {
        dao.getTransactionById(id)?.let { entity ->
            entity.requestBodyFilePath?.let { bodyFileStore.deleteBody(it) }
            entity.responseBodyFilePath?.let { bodyFileStore.deleteBody(it) }
        }
        dao.deleteTransaction(id)
    }

    override suspend fun clear() {
        dao.clear()
        bodyFileStore.clear()
    }

    override suspend fun trimToMaxRecords(maxRecords: Int) {
        if (maxRecords <= 0) {
            clear()
        } else {
            dao.getOldestOverLimit(maxRecords).forEach { entity ->
                entity.requestBodyFilePath?.let { bodyFileStore.deleteBody(it) }
                entity.responseBodyFilePath?.let { bodyFileStore.deleteBody(it) }
            }
            dao.deleteOldestOverLimit(maxRecords)
        }
    }
}
