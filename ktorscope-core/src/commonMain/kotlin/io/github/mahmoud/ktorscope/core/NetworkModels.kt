/**
 * Created by Mahmoud kamal El-Din on 02/05/2026
 */
package io.github.mahmoud.ktorscope.core

/**
 * A single observed network call captured by KtorScope.
 */
data class NetworkTransaction(
    val id: String,
    val request: NetworkRequest,
    val response: NetworkResponse? = null,
    val error: NetworkError? = null,
    val durationMillis: Long? = null,
    val createdAtMillis: Long,
) {
    val isFailed: Boolean
        get() = error != null
}

/**
 * Captured request metadata and optional body preview.
 */
data class NetworkRequest(
    val method: String,
    val url: String,
    val headers: Map<String, List<String>> = emptyMap(),
    val body: String? = null,
    val bodyTruncated: Boolean = false,
)

/**
 * Captured response metadata and optional body preview.
 */
data class NetworkResponse(
    val statusCode: Int,
    val statusDescription: String,
    val headers: Map<String, List<String>> = emptyMap(),
    val body: String? = null,
    val bodyTruncated: Boolean = false,
)

/**
 * Captured failure information for requests that throw before a response is available.
 */
data class NetworkError(
    val type: String,
    val message: String?,
    val stackTrace: String? = null,
)
