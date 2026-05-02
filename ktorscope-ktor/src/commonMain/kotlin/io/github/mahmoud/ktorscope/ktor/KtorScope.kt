/**
 * Created by Mahmoud kamal El-Din on 02/05/2026
 */
package io.github.mahmoud.ktorscope.ktor

import io.github.mahmoud.ktorscope.core.BodyPreview
import io.github.mahmoud.ktorscope.core.KtorScopeConfig
import io.github.mahmoud.ktorscope.core.KtorScopePrettyPrintConfig
import io.github.mahmoud.ktorscope.core.KtorScopeStore
import io.github.mahmoud.ktorscope.core.NetworkError
import io.github.mahmoud.ktorscope.core.NetworkRequest
import io.github.mahmoud.ktorscope.core.NetworkResponse
import io.github.mahmoud.ktorscope.core.NetworkTransaction
import io.github.mahmoud.ktorscope.core.Redactor
import io.github.mahmoud.ktorscope.core.prettyPrint
import io.github.mahmoud.ktorscope.core.toBodyPreview
import io.ktor.client.plugins.api.ClientPlugin
import io.ktor.client.plugins.api.Send
import io.ktor.client.plugins.api.SendingRequest
import io.ktor.client.plugins.api.SetupRequest
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.request.HttpRequest
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Headers
import io.ktor.http.content.ByteArrayContent
import io.ktor.http.content.OutgoingContent
import io.ktor.http.content.TextContent
import io.ktor.util.AttributeKey
import io.ktor.util.date.getTimeMillis
import kotlin.random.Random

/**
 * Ktor Client plugin that records requests, responses, duration, and failures into KtorScopeStore.
 */
val KtorScope: ClientPlugin<KtorScopePluginConfig> = createClientPlugin(
    name = "KtorScope",
    createConfiguration = ::KtorScopePluginConfig,
) {
    val config = pluginConfig.toCoreConfig()
    val prettyPrintEnabled = pluginConfig.prettyPrint
    val prettyPrintConfig = pluginConfig.prettyPrintConfig
    val logger = pluginConfig.logger
    val startedAtKey = AttributeKey<Long>("KtorScopeStartedAt")
    val requestKey = AttributeKey<NetworkRequest>("KtorScopeRequest")

    on(SetupRequest) { request ->
        if (config.enabled) {
            val startedAt = getTimeMillis()
            request.attributes.put(startedAtKey, startedAt)
            request.attributes.put(
                requestKey,
                request.toNetworkRequest(
                    content = null,
                    config = config,
                ),
            )
        }
    }

    on(SendingRequest) { request, content ->
        if (config.enabled) {
            request.attributes.put(
                requestKey,
                request.toNetworkRequest(
                    content = content,
                    config = config,
                ),
            )
        }
    }

    onResponse { response ->
        if (!config.enabled) return@onResponse
        val call = response.call
        val startedAt = call.request.attributes.getOrNull(startedAtKey) ?: getTimeMillis()
        val request = call.request.attributes.getOrNull(requestKey)
            ?: call.request.toNetworkRequest(config = config)
        val responseBody = if (config.captureBodies) {
            runCatching { response.bodyAsText().toBodyPreview(config.maxBodySize) }.getOrNull()
        } else {
            null
        }

        val transaction = NetworkTransaction(
            id = newTransactionId(),
            request = request,
            response = NetworkResponse(
                statusCode = response.status.value,
                statusDescription = response.status.description,
                headers = Redactor.redactHeaders(
                    headers = response.headers.toMap(),
                    sensitiveHeaderNames = config.redactHeaders,
                ),
                body = responseBody?.value,
                bodyTruncated = responseBody?.truncated ?: false,
            ),
            durationMillis = getTimeMillis() - startedAt,
            createdAtMillis = startedAt,
        )
        config.store.add(transaction)
        if (prettyPrintEnabled) {
            logger(transaction.prettyPrint(prettyPrintConfig))
        }
    }

    on(Send) { request ->
        try {
            proceed(request)
        } catch (cause: Throwable) {
            if (config.enabled) {
                val startedAt = request.attributes.getOrNull(startedAtKey) ?: getTimeMillis()
                val networkRequest = request.attributes.getOrNull(requestKey)
                    ?: request.toNetworkRequest(content = null, config = config)
                val transaction = NetworkTransaction(
                    id = newTransactionId(),
                    request = networkRequest,
                    error = NetworkError(
                        type = cause::class.simpleName ?: "Throwable",
                        message = cause.message,
                    ),
                    durationMillis = getTimeMillis() - startedAt,
                    createdAtMillis = startedAt,
                )
                config.store.add(transaction)
                if (prettyPrintEnabled) {
                    logger(transaction.prettyPrint(prettyPrintConfig))
                }
            }
            throw cause
        }
    }
}

/**
 * Configuration exposed in HttpClient { install(KtorScope) { ... } }.
 */
class KtorScopePluginConfig {
    var enabled: Boolean = true
    var captureBodies: Boolean = true
    var maxBodySize: Int = KtorScopeConfig.DEFAULT_MAX_BODY_SIZE
    var redactHeaders: Set<String> = KtorScopeConfig.DEFAULT_REDACT_HEADERS
    var store: KtorScopeStore = KtorScopeStore.shared
    var prettyPrint: Boolean = false
    var prettyPrintConfig: KtorScopePrettyPrintConfig = KtorScopePrettyPrintConfig()
    var logger: (String) -> Unit = { message -> println(message) }

    fun toCoreConfig(): KtorScopeConfig = KtorScopeConfig(
        enabled = enabled,
        captureBodies = captureBodies,
        maxBodySize = maxBodySize,
        redactHeaders = redactHeaders,
        store = store,
    )
}

private fun HttpRequestBuilder.toNetworkRequest(
    content: OutgoingContent?,
    config: KtorScopeConfig,
): NetworkRequest {
    val body = if (config.captureBodies) {
        runCatching { content?.previewBody(config.maxBodySize) }.getOrNull()
    } else {
        null
    }
    return NetworkRequest(
        method = method.value,
        url = url.buildString(),
        headers = Redactor.redactHeaders(
            headers = headers.build().toMap(),
            sensitiveHeaderNames = config.redactHeaders,
        ),
        body = body?.value,
        bodyTruncated = body?.truncated ?: false,
    )
}

private fun HttpRequest.toNetworkRequest(config: KtorScopeConfig): NetworkRequest {
    return NetworkRequest(
        method = method.value,
        url = url.toString(),
        headers = Redactor.redactHeaders(
            headers = headers.toMap(),
            sensitiveHeaderNames = config.redactHeaders,
        ),
    )
}

private fun OutgoingContent.previewBody(maxBodySize: Int): BodyPreview? {
    return when (this) {
        is TextContent -> text.toBodyPreview(maxBodySize)
        is ByteArrayContent -> bytes().decodeToString().toBodyPreview(maxBodySize)
        else -> null
    }
}

private fun Headers.toMap(): Map<String, List<String>> {
    return names().associateWith { name -> getAll(name).orEmpty() }
}

private fun newTransactionId(): String {
    return "ktorscope-${getTimeMillis()}-${Random.nextLong()}"
}
