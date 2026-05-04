/**
 * Created by Mahmoud kamal El-Din on 04/05/2026
 */
package io.github.mahmoud.ktorscope.persistence

import java.io.File

actual class PlatformNetworkBodyFileStore actual constructor(
    private val rootDirectoryPath: String,
) : NetworkBodyFileStore {
    actual override suspend fun saveBody(transactionId: String, type: BodyType, content: String): String {
        val directory = File(rootDirectoryPath).apply { mkdirs() }
        val file = File(directory, "${transactionId.sanitize()}-${type.name.lowercase()}.txt")
        file.writeText(content)
        return file.absolutePath
    }

    actual override suspend fun readBody(path: String): String? {
        return runCatching { File(path).takeIf { it.exists() }?.readText() }.getOrNull()
    }

    actual override suspend fun deleteBody(path: String) {
        runCatching { File(path).delete() }
    }

    actual override suspend fun clear() {
        runCatching { File(rootDirectoryPath).deleteRecursively() }
    }
}

private fun String.sanitize(): String {
    return map { char -> if (char.isLetterOrDigit() || char == '-' || char == '_') char else '_' }.joinToString("")
}
