/**
 * Created by Mahmoud kamal El-Din on 02/05/2026
 */
package io.github.mahmoud.ktorscope.ktor

import io.github.mahmoud.ktorscope.core.BodyPreview
import io.github.mahmoud.ktorscope.core.KtorScopeConfig
import io.github.mahmoud.ktorscope.core.KtorScopeHistoryPersistenceConfig
import io.github.mahmoud.ktorscope.core.KtorScopePrettyPrintConfig
import io.github.mahmoud.ktorscope.core.KtorScopeStore
import io.github.mahmoud.ktorscope.core.NetworkError
import io.github.mahmoud.ktorscope.core.KtorScopeHistoryPersistence
import io.github.mahmoud.ktorscope.core.NetworkRequest
import io.github.mahmoud.ktorscope.core.NetworkResponse
import io.github.mahmoud.ktorscope.core.NetworkTransaction
import io.github.mahmoud.ktorscope.core.NoOpKtorScopeHistoryPersistence
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
    if (config.persistHistory) {
        config.store.enablePersistence(
            persistence = config.persistence,
            maxHistoryRecords = config.maxHistoryRecords,
        )
    }
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
            runCatching {
                response.bodyAsText().toBodyPreview(
                    maxBodySize = config.maxBodyPreviewSize.toPreviewSize(),
                    keepFullBody = config.persistHistory,
                )
            }.getOrNull()
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
                bodySizeBytes = responseBody?.sourceSizeBytes,
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
    var maxBodyPreviewSize: Long = KtorScopeConfig.DEFAULT_MAX_BODY_PREVIEW_SIZE
    var largeBodyFileThreshold: Long = KtorScopeConfig.DEFAULT_LARGE_BODY_FILE_THRESHOLD
    var persistHistory: Boolean = false
    var maxHistoryRecords: Int = KtorScopeStore.DEFAULT_MAX_HISTORY_RECORDS
    var redactHeaders: Set<String> = KtorScopeConfig.DEFAULT_REDACT_HEADERS
    var store: KtorScopeStore = KtorScopeStore.shared
    var persistence: KtorScopeHistoryPersistence = NoOpKtorScopeHistoryPersistence
    var historyPersistenceConfig: KtorScopeHistoryPersistenceConfig = KtorScopeHistoryPersistenceConfig()
    var prettyPrint: Boolean = false
    var prettyPrintConfig: KtorScopePrettyPrintConfig = KtorScopePrettyPrintConfig()
    var logger: (String) -> Unit = { message -> println(message) }

    fun historyPersistence(
        block: KtorScopeHistoryPersistenceConfigBuilder.() -> Unit,
    ) {
        historyPersistenceConfig = KtorScopeHistoryPersistenceConfigBuilder(historyPersistenceConfig)
            .apply(block)
            .build()
    }

    fun prettyPrintConfig(
        block: KtorScopePrettyPrintConfigBuilder.() -> Unit,
    ) {
        prettyPrintConfig = KtorScopePrettyPrintConfigBuilder(prettyPrintConfig)
            .apply(block)
            .build()
    }

    fun toCoreConfig(): KtorScopeConfig = KtorScopeConfig(
        enabled = enabled,
        captureBodies = captureBodies,
        persistHistory = resolvedHistoryConfig.enabled,
        maxHistoryRecords = resolvedHistoryConfig.maxRecords,
        maxBodyPreviewSize = resolvedHistoryConfig.maxBodyPreviewSize,
        largeBodyFileThreshold = resolvedHistoryConfig.largeBodyFileThreshold,
        redactHeaders = redactHeaders,
        store = store,
        persistence = resolvedHistoryConfig.persistence,
    )

    private val resolvedHistoryConfig: KtorScopeHistoryPersistenceConfig
        get() {
            val legacyPreviewSize = maxBodyPreviewSize.takeIf { it != KtorScopeConfig.DEFAULT_MAX_BODY_PREVIEW_SIZE }
                ?: maxBodySize.toLong()
            val legacyConfig = KtorScopeHistoryPersistenceConfig(
                enabled = persistHistory,
                maxRecords = maxHistoryRecords,
                maxBodyPreviewSize = legacyPreviewSize,
                largeBodyFileThreshold = largeBodyFileThreshold,
                persistence = persistence,
            )
            return if (historyPersistenceConfig != KtorScopeHistoryPersistenceConfig()) {
                historyPersistenceConfig
            } else {
                legacyConfig
            }
        }
}

class KtorScopeHistoryPersistenceConfigBuilder internal constructor(
    config: KtorScopeHistoryPersistenceConfig,
) {
    var enabled: Boolean = config.enabled
    var maxRecords: Int = config.maxRecords
    var maxBodyPreviewSize: Long = config.maxBodyPreviewSize
    var largeBodyFileThreshold: Long = config.largeBodyFileThreshold
    var persistence: KtorScopeHistoryPersistence = config.persistence

    fun build(): KtorScopeHistoryPersistenceConfig = KtorScopeHistoryPersistenceConfig(
        enabled = enabled,
        maxRecords = maxRecords,
        maxBodyPreviewSize = maxBodyPreviewSize,
        largeBodyFileThreshold = largeBodyFileThreshold,
        persistence = persistence,
    )
}

class KtorScopePrettyPrintConfigBuilder internal constructor(
    config: KtorScopePrettyPrintConfig,
) {
    var includeHeaders: Boolean = config.includeHeaders
    var includeBodies: Boolean = config.includeBodies
    var includeCurl: Boolean = config.includeCurl
    var includeGraphQl: Boolean = config.includeGraphQl
    var prettyJson: Boolean = config.prettyJson

    fun build(): KtorScopePrettyPrintConfig = KtorScopePrettyPrintConfig(
        includeHeaders = includeHeaders,
        includeBodies = includeBodies,
        includeCurl = includeCurl,
        includeGraphQl = includeGraphQl,
        prettyJson = prettyJson,
    )
}

private fun HttpRequestBuilder.toNetworkRequest(
    content: OutgoingContent?,
    config: KtorScopeConfig,
): NetworkRequest {
    val body = if (config.captureBodies) {
        runCatching {
            content?.previewBody(
                maxBodySize = config.maxBodyPreviewSize.toPreviewSize(),
                keepFullBody = config.persistHistory,
            )
        }.getOrNull()
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
        bodySizeBytes = body?.sourceSizeBytes,
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

private fun OutgoingContent.previewBody(
    maxBodySize: Int,
    keepFullBody: Boolean,
): BodyPreview? {
    return when (this) {
        is TextContent -> text.toBodyPreview(maxBodySize, keepFullBody)
        is ByteArrayContent -> bytes().decodeToString().toBodyPreview(maxBodySize, keepFullBody)
        else -> null
    }
}

private fun String.toBodyPreview(
    maxBodySize: Int,
    keepFullBody: Boolean,
): BodyPreview {
    val preview = toBodyPreview(maxBodySize)
    return if (keepFullBody && preview.truncated) {
        preview.copy(value = this)
    } else {
        preview
    }
}

private fun Headers.toMap(): Map<String, List<String>> {
    return names().associateWith { name -> getAll(name).orEmpty() }
}

private fun newTransactionId(): String {
    return "ktorscope-${getTimeMillis()}-${Random.nextLong()}"
}

private fun Long.toPreviewSize(): Int = coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
