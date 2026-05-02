/**
 * Created by Mahmoud kamal El-Din on 02/05/2026
 */
package io.github.mahmoud.ktorscope.core

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * In-memory transaction store exposed as StateFlow for Compose and other observers.
 */
class KtorScopeStore(
    initialTransactions: List<NetworkTransaction> = emptyList(),
) {
    private val _transactions = MutableStateFlow(initialTransactions)

    val transactions: StateFlow<List<NetworkTransaction>> = _transactions.asStateFlow()

    fun add(transaction: NetworkTransaction) {
        _transactions.update { current -> listOf(transaction) + current }
    }

    fun clear() {
        _transactions.value = emptyList()
    }

    companion object {
        val shared: KtorScopeStore = KtorScopeStore()
    }
}
