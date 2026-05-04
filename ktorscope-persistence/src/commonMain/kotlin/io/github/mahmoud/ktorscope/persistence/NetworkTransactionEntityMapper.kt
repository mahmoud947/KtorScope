/**
 * Created by Mahmoud kamal El-Din on 04/05/2026
 */
package io.github.mahmoud.ktorscope.persistence

import io.github.mahmoud.ktorscope.core.NetworkError
import io.github.mahmoud.ktorscope.core.NetworkRequest
import io.github.mahmoud.ktorscope.core.NetworkResponse
import io.github.mahmoud.ktorscope.core.NetworkTransaction

internal class NetworkTransactionEntityMapper(
    private val maxBodyPreviewSize: Long,
    private val largeBodyFileThreshold: Long,
    private val bodyFileStore: NetworkBodyFileStore,
) {
    suspend fun toEntity(transaction: NetworkTransaction): NetworkTransactionEntity {
        val requestBody = persistBodyIfNeeded(transaction.id, BodyType.REQUEST, transaction.request.body)
        val responseBody = persistBodyIfNeeded(transaction.id, BodyType.RESPONSE, transaction.response?.body)

        return NetworkTransactionEntity(
            id = transaction.id,
            method = transaction.request.method,
            url = transaction.request.url,
            host = transaction.request.url.hostPart(),
            path = transaction.request.url.pathPart(),
            statusCode = transaction.response?.statusCode,
            durationMs = transaction.durationMillis,
            timestampMs = transaction.createdAtMillis,
            requestHeadersJson = HeadersCodec.encode(transaction.request.headers),
            responseHeadersJson = HeadersCodec.encode(transaction.response?.headers.orEmpty()),
            requestBodyPreview = requestBody.preview,
            responseBodyPreview = responseBody.preview,
            requestBodyFilePath = transaction.request.bodyFilePath ?: requestBody.filePath,
            responseBodyFilePath = transaction.response?.bodyFilePath ?: responseBody.filePath,
            requestBodySizeBytes = transaction.request.bodySizeBytes ?: requestBody.sizeBytes,
            responseBodySizeBytes = transaction.response?.bodySizeBytes ?: responseBody.sizeBytes,
            errorMessage = transaction.error?.message,
            errorType = transaction.error?.type,
            isFromCache = transaction.isFromCache,
            createdAtMs = transaction.createdAtMillis,
        )
    }

    fun toDomain(entity: NetworkTransactionEntity): NetworkTransaction {
        return NetworkTransaction(
            id = entity.id,
            request = NetworkRequest(
                method = entity.method,
                url = entity.url,
                headers = HeadersCodec.decode(entity.requestHeadersJson),
                body = entity.requestBodyPreview,
                bodyTruncated = entity.requestBodyFilePath != null,
                bodyFilePath = entity.requestBodyFilePath,
                bodySizeBytes = entity.requestBodySizeBytes,
            ),
            response = entity.statusCode?.let {
                NetworkResponse(
                    statusCode = it,
                    statusDescription = "",
                    headers = HeadersCodec.decode(entity.responseHeadersJson),
                    body = entity.responseBodyPreview,
                    bodyTruncated = entity.responseBodyFilePath != null,
                    bodyFilePath = entity.responseBodyFilePath,
                    bodySizeBytes = entity.responseBodySizeBytes,
                )
            },
            error = entity.errorType?.let { type ->
                NetworkError(
                    type = type,
                    message = entity.errorMessage,
                )
            },
            durationMillis = entity.durationMs,
            createdAtMillis = entity.timestampMs,
            isFromCache = entity.isFromCache,
        )
    }

    private suspend fun persistBodyIfNeeded(
        transactionId: String,
        type: BodyType,
        body: String?,
    ): PersistedBody {
        if (body == null) return PersistedBody()
        val sizeBytes = body.encodeToByteArray().size.toLong()
        if (sizeBytes <= largeBodyFileThreshold || bodyFileStore === NoOpNetworkBodyFileStore) {
            return PersistedBody(preview = body.takePreview(), sizeBytes = sizeBytes)
        }

        val filePath = runCatching {
            bodyFileStore.saveBody(transactionId, type, body)
        }.getOrNull().orEmpty().ifBlank { null }

        return PersistedBody(
            preview = body.takePreview(),
            filePath = filePath,
            sizeBytes = sizeBytes,
        )
    }

    private fun String.takePreview(): String {
        return take(maxBodyPreviewSize.coerceAtMost(Int.MAX_VALUE.toLong()).toInt())
    }

    private data class PersistedBody(
        val preview: String? = null,
        val filePath: String? = null,
        val sizeBytes: Long? = null,
    )
}

private fun String.hostPart(): String? {
    return substringAfter("://", this).substringBefore("/").takeIf { it.isNotBlank() }
}

private fun String.pathPart(): String? {
    val withoutScheme = substringAfter("://", this)
    val path = withoutScheme.substringAfter("/", missingDelimiterValue = "")
    return if (path.isBlank()) "/" else "/$path"
}
