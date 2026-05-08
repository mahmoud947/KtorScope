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
    val isFromCache: Boolean = false,
    val protocol: NetworkProtocol = NetworkProtocol.HTTP,
    val webSocketFrames: List<WebSocketFrameInspection> = emptyList(),
) {
    val isFailed: Boolean
        get() = error != null

    val isWebSocket: Boolean
        get() = protocol == NetworkProtocol.WEBSOCKET
}

/**
 * Captured network protocol family.
 */
enum class NetworkProtocol {
    HTTP,
    WEBSOCKET,
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
    val bodyFilePath: String? = null,
    val bodySizeBytes: Long? = null,
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
    val bodyFilePath: String? = null,
    val bodySizeBytes: Long? = null,
)

/**
 * Captured failure information for requests that throw before a response is available.
 */
data class NetworkError(
    val type: String,
    val message: String?,
    val stackTrace: String? = null,
)

/**
 * Direction of an inspected WebSocket frame relative to the client.
 */
enum class WebSocketFrameDirection {
    INCOMING,
    OUTGOING,
}

/**
 * Captured WebSocket frame kind.
 */
enum class WebSocketFrameType {
    TEXT,
    BINARY,
    CLOSE,
    PING,
    PONG,
}

/**
 * A single WebSocket frame observed on an upgraded connection.
 */
data class WebSocketFrameInspection(
    val index: Int,
    val direction: WebSocketFrameDirection,
    val type: WebSocketFrameType,
    val timestampMillis: Long,
    val sizeBytes: Long,
    val payload: String? = null,
    val payloadTruncated: Boolean = false,
    val fin: Boolean = true,
    val closeCode: Long? = null,
    val closeReason: String? = null,
)
