/**
 * Created by Mahmoud kamal El-Din on 04/05/2026
 */
package io.github.mahmoud.ktorscope.persistence

interface NetworkBodyFileStore {
    suspend fun saveBody(transactionId: String, type: BodyType, content: String): String
    suspend fun readBody(path: String): String?
    suspend fun deleteBody(path: String)
    suspend fun clear()
}

enum class BodyType {
    REQUEST,
    RESPONSE,
}

object NoOpNetworkBodyFileStore : NetworkBodyFileStore {
    override suspend fun saveBody(transactionId: String, type: BodyType, content: String): String = ""

    override suspend fun readBody(path: String): String? = null

    override suspend fun deleteBody(path: String) = Unit

    override suspend fun clear() = Unit
}

expect class PlatformNetworkBodyFileStore(rootDirectoryPath: String) : NetworkBodyFileStore {
    override suspend fun saveBody(transactionId: String, type: BodyType, content: String): String
    override suspend fun readBody(path: String): String?
    override suspend fun deleteBody(path: String)
    override suspend fun clear()
}
