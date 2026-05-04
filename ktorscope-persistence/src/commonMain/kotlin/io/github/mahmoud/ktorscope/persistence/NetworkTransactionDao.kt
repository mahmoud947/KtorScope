/**
 * Created by Mahmoud kamal El-Din on 04/05/2026
 */
package io.github.mahmoud.ktorscope.persistence

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NetworkTransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(entity: NetworkTransactionEntity)

    @Query("SELECT * FROM network_transactions ORDER BY timestampMs DESC")
    fun observeTransactions(): Flow<List<NetworkTransactionEntity>>

    @Query("SELECT * FROM network_transactions WHERE id = :id LIMIT 1")
    suspend fun getTransactionById(id: String): NetworkTransactionEntity?

    @Query("DELETE FROM network_transactions WHERE id = :id")
    suspend fun deleteTransaction(id: String)

    @Query("DELETE FROM network_transactions")
    suspend fun clear()

    @Query("DELETE FROM network_transactions WHERE id NOT IN (SELECT id FROM network_transactions ORDER BY timestampMs DESC LIMIT :maxRecords)")
    suspend fun deleteOldestOverLimit(maxRecords: Int)

    @Query("SELECT * FROM network_transactions WHERE id NOT IN (SELECT id FROM network_transactions ORDER BY timestampMs DESC LIMIT :maxRecords)")
    suspend fun getOldestOverLimit(maxRecords: Int): List<NetworkTransactionEntity>
}
