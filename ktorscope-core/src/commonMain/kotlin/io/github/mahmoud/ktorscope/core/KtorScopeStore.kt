/**
 * Created by Mahmoud kamal El-Din on 02/05/2026
 */
package io.github.mahmoud.ktorscope.core

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Transaction store exposed as StateFlow for Compose and other observers.
 * Persistence is opt-in through KtorScopeHistoryPersistence.
 */
class KtorScopeStore(
    initialTransactions: List<NetworkTransaction> = emptyList(),
    persistHistory: Boolean = false,
    persistence: KtorScopeHistoryPersistence = NoOpKtorScopeHistoryPersistence,
    maxHistoryRecords: Int = DEFAULT_MAX_HISTORY_RECORDS,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) {
    private var persistHistory: Boolean = persistHistory
    private var persistence: KtorScopeHistoryPersistence = persistence
    private var maxHistoryRecords: Int = maxHistoryRecords
    private var observingPersistence: Boolean = false
    private val _transactions = MutableStateFlow(initialTransactions)
    private val _persistedTransactions = MutableStateFlow<List<NetworkTransaction>>(emptyList())

    val transactions: StateFlow<List<NetworkTransaction>> = _transactions.asStateFlow()
    val persistedTransactions: StateFlow<List<NetworkTransaction>> = _persistedTransactions.asStateFlow()

    init {
        startPersistenceObserverIfNeeded()
    }

    fun enablePersistence(
        persistence: KtorScopeHistoryPersistence,
        maxHistoryRecords: Int = DEFAULT_MAX_HISTORY_RECORDS,
    ) {
        this.persistHistory = true
        this.persistence = persistence
        this.maxHistoryRecords = maxHistoryRecords
        startPersistenceObserverIfNeeded()
    }

    private fun startPersistenceObserverIfNeeded() {
        if (!persistHistory || observingPersistence) return
        observingPersistence = true
        scope.launch {
            runCatching {
                persistence.observeTransactions().collect { history ->
                    _persistedTransactions.value = history
                }
            }
        }
    }

    fun add(transaction: NetworkTransaction) {
        _transactions.update { current -> listOf(transaction) + current }
        if (persistHistory) {
            scope.launch {
                runCatching {
                    persistence.saveTransaction(transaction)
                    persistence.trimToMaxRecords(maxHistoryRecords)
                }
            }
        }
    }

    fun clear() {
        _transactions.value = emptyList()
        if (persistHistory) {
            scope.launch {
                runCatching { persistence.clear() }
            }
        }
    }

    fun clearMemory() {
        _transactions.value = emptyList()
    }

    fun clearPersistedHistory() {
        if (persistHistory) {
            scope.launch {
                runCatching { persistence.clear() }
            }
        }
    }

    companion object {
        const val DEFAULT_MAX_HISTORY_RECORDS: Int = 500

        val shared: KtorScopeStore = KtorScopeStore()
    }
}
