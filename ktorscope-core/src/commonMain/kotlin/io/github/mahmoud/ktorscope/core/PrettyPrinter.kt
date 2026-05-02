/**
 * Created by Mahmoud kamal El-Din on 02/05/2026
 */
package io.github.mahmoud.ktorscope.core

/**
 * Options for log-friendly pretty printing.
 */
data class KtorScopePrettyPrintConfig(
    val includeHeaders: Boolean = true,
    val includeBodies: Boolean = true,
    val includeCurl: Boolean = false,
    val includeGraphQl: Boolean = true,
    val prettyJson: Boolean = true,
)

/**
 * Pretty prints a captured transaction for logs.
 */
fun NetworkTransaction.prettyPrint(
    config: KtorScopePrettyPrintConfig = KtorScopePrettyPrintConfig(),
): String = buildString {
    appendLine("KtorScope")
    appendLine("=========")
    appendLine("${request.method.uppercase()} ${request.url}")
    appendLine("id: $id")
    appendLine("createdAtMillis: $createdAtMillis")
    durationMillis?.let { appendLine("duration: ${it}ms") }
    appendLine("result: ${resultLabel()}")

    if (config.includeGraphQl) {
        graphQlOperation()?.let { operation ->
            appendLine()
            appendLine("GraphQL")
            appendLine("-------")
            appendLine("type: ${operation.operationType ?: "unknown"}")
            appendLine("name: ${operation.operationName ?: "anonymous"}")
            appendBlock("query", operation.query)
            operation.variables?.takeIf { it.isNotBlank() && it != "null" }?.let { variables ->
                appendBlock("variables", variables.prettyMaybeJson(config.prettyJson))
            }
        }
    }

    appendLine()
    appendLine("Request")
    appendLine("-------")
    appendRequest(request, config)

    response?.let { capturedResponse ->
        appendLine()
        appendLine("Response")
        appendLine("--------")
        appendResponse(capturedResponse, config)
    }

    error?.let { capturedError ->
        appendLine()
        appendLine("Error")
        appendLine("-----")
        appendLine("${capturedError.type}: ${capturedError.message.orEmpty()}")
        capturedError.stackTrace?.takeIf { it.isNotBlank() }?.let { appendBlock("stackTrace", it) }
    }

    if (config.includeCurl) {
        appendLine()
        appendLine("cURL")
        appendLine("----")
        appendLine(toCurlCommand())
    }
}.trimEnd()

/**
 * Pretty prints a captured request for logs.
 */
fun NetworkRequest.prettyPrint(
    config: KtorScopePrettyPrintConfig = KtorScopePrettyPrintConfig(),
): String = buildString {
    appendLine("Request")
    appendLine("=======")
    appendRequest(this@prettyPrint, config)
}.trimEnd()

/**
 * Pretty prints a captured response for logs.
 */
fun NetworkResponse.prettyPrint(
    config: KtorScopePrettyPrintConfig = KtorScopePrettyPrintConfig(),
): String = buildString {
    appendLine("Response")
    appendLine("========")
    appendResponse(this@prettyPrint, config)
}.trimEnd()

/**
 * Pretty prints JSON objects/arrays when possible, otherwise returns the original string.
 */
fun String.prettyPrintJsonOrSelf(): String = prettyMaybeJson(prettyJson = true)

private fun NetworkTransaction.resultLabel(): String {
    val status = response?.statusCode
    return when {
        error != null -> "failed"
        status != null -> "$status ${response.statusDescription}"
        else -> "pending"
    }
}

private fun StringBuilder.appendPrettyHeaders(headers: Map<String, List<String>>) {
    if (headers.isEmpty()) {
        appendLine("headers: none")
        return
    }
    appendLine("headers:")
    headers.forEach { (name, values) ->
        appendLine("  $name: ${values.joinToString()}")
    }
}

private fun StringBuilder.appendPrettyBody(
    body: String?,
    truncated: Boolean,
    prettyJson: Boolean,
) {
    if (body.isNullOrBlank()) {
        appendLine("body: none")
        return
    }
    appendBlock(
        label = if (truncated) "body (truncated)" else "body",
        value = body.prettyMaybeJson(prettyJson),
    )
}

private fun StringBuilder.appendBlock(label: String, value: String) {
    appendLine("$label:")
    value.lineSequence().forEach { line ->
        appendLine("  $line")
    }
}

private fun StringBuilder.appendRequest(
    request: NetworkRequest,
    config: KtorScopePrettyPrintConfig,
) {
    appendLine("method: ${request.method.uppercase()}")
    appendLine("url: ${request.url}")
    if (config.includeHeaders) appendPrettyHeaders(request.headers)
    if (config.includeBodies) appendPrettyBody(request.body, request.bodyTruncated, config.prettyJson)
}

private fun StringBuilder.appendResponse(
    response: NetworkResponse,
    config: KtorScopePrettyPrintConfig,
) {
    appendLine("status: ${response.statusCode} ${response.statusDescription}")
    if (config.includeHeaders) appendPrettyHeaders(response.headers)
    if (config.includeBodies) appendPrettyBody(response.body, response.bodyTruncated, config.prettyJson)
}

private fun String.prettyMaybeJson(prettyJson: Boolean): String {
    if (!prettyJson) return this
    val trimmed = trim()
    if (!(trimmed.startsWith("{") && trimmed.endsWith("}")) && !(trimmed.startsWith("[") && trimmed.endsWith("]"))) {
        return this
    }
    val builder = StringBuilder()
    var indent = 0
    var inString = false
    var escaping = false
    trimmed.forEach { char ->
        when {
            escaping -> {
                builder.append(char)
                escaping = false
            }
            char == '\\' && inString -> {
                builder.append(char)
                escaping = true
            }
            char == '"' -> {
                builder.append(char)
                inString = !inString
            }
            inString -> builder.append(char)
            char == '{' || char == '[' -> {
                builder.append(char).append('\n')
                indent++
                builder.appendIndent(indent)
            }
            char == '}' || char == ']' -> {
                builder.append('\n')
                indent = (indent - 1).coerceAtLeast(0)
                builder.appendIndent(indent)
                builder.append(char)
            }
            char == ',' -> {
                builder.append(char).append('\n')
                builder.appendIndent(indent)
            }
            char == ':' -> builder.append(": ")
            !char.isWhitespace() -> builder.append(char)
        }
    }
    return builder.toString()
}

private fun StringBuilder.appendIndent(level: Int) {
    repeat(level) { append("  ") }
}
