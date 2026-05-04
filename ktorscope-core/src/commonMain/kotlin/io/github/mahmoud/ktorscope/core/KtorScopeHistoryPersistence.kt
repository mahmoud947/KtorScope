/**
 * Created by Mahmoud kamal El-Din on 02/05/2026
 */
package io.github.mahmoud.ktorscope.core

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

interface KtorScopeHistoryPersistence {
    suspend fun saveTransaction(transaction: NetworkTransaction)
    suspend fun observeTransactions(): Flow<List<NetworkTransaction>>
    suspend fun getTransactionById(id: String): NetworkTransaction?
    suspend fun deleteTransaction(id: String)
    suspend fun clear()
    suspend fun trimToMaxRecords(maxRecords: Int)
}

object NoOpKtorScopeHistoryPersistence : KtorScopeHistoryPersistence {
    override suspend fun saveTransaction(transaction: NetworkTransaction) = Unit

    override suspend fun observeTransactions(): Flow<List<NetworkTransaction>> = flowOf(emptyList())

    override suspend fun getTransactionById(id: String): NetworkTransaction? = null

    override suspend fun deleteTransaction(id: String) = Unit

    override suspend fun clear() = Unit

    override suspend fun trimToMaxRecords(maxRecords: Int) = Unit
}
