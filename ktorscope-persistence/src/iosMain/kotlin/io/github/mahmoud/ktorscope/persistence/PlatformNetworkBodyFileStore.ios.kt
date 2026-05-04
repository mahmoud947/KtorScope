/**
 * Created by Mahmoud kamal El-Din on 04/05/2026
 */
@file:OptIn(kotlinx.cinterop.BetaInteropApi::class, kotlinx.cinterop.ExperimentalForeignApi::class)

package io.github.mahmoud.ktorscope.persistence

import platform.Foundation.NSFileManager
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.stringWithContentsOfFile
import platform.Foundation.writeToFile

actual class PlatformNetworkBodyFileStore actual constructor(
    private val rootDirectoryPath: String,
) : NetworkBodyFileStore {
    actual override suspend fun saveBody(transactionId: String, type: BodyType, content: String): String {
        NSFileManager.defaultManager.createDirectoryAtPath(
            path = rootDirectoryPath,
            withIntermediateDirectories = true,
            attributes = null,
            error = null,
        )
        val path = "$rootDirectoryPath/${transactionId.sanitize()}-${type.name.lowercase()}.txt"
        NSString.create(string = content).writeToFile(
            path = path,
            atomically = true,
            encoding = NSUTF8StringEncoding,
            error = null,
        )
        return path
    }

    actual override suspend fun readBody(path: String): String? {
        return runCatching {
            NSString.stringWithContentsOfFile(
                path = path,
                encoding = NSUTF8StringEncoding,
                error = null,
            )
        }.getOrNull()
    }

    actual override suspend fun deleteBody(path: String) {
        runCatching {
            NSFileManager.defaultManager.removeItemAtPath(path, error = null)
        }
    }

    actual override suspend fun clear() {
        runCatching {
            NSFileManager.defaultManager.removeItemAtPath(rootDirectoryPath, error = null)
        }
    }
}

private fun String.sanitize(): String {
    return map { char -> if (char.isLetterOrDigit() || char == '-' || char == '_') char else '_' }.joinToString("")
}
