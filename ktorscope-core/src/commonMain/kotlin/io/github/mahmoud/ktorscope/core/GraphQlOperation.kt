/**
 * Created by Mahmoud kamal El-Din on 02/05/2026
 */
package io.github.mahmoud.ktorscope.core

/**
 * Parsed metadata for a GraphQL HTTP request body.
 */
data class GraphQlOperation(
    val operationType: String?,
    val operationName: String?,
    val query: String,
    val variables: String? = null,
)

/**
 * Parses common GraphQL JSON request bodies without adding a JSON dependency.
 */
fun NetworkTransaction.graphQlOperation(): GraphQlOperation? = request.graphQlOperation()

/**
 * Parses common GraphQL JSON request bodies without adding a JSON dependency.
 */
fun NetworkRequest.graphQlOperation(): GraphQlOperation? {
    val bodyText = body?.trim().orEmpty()
    if (bodyText.isBlank()) return null

    val query = bodyText.extractJsonString("query")?.trim().orEmpty()
    if (query.isBlank()) return null

    val operationType = query.firstGraphQlKeyword()
    val nameFromQuery = query.operationNameAfter(operationType)
    val operationName = bodyText.extractJsonString("operationName")
        ?.takeIf { it.isNotBlank() }
        ?: nameFromQuery

    return GraphQlOperation(
        operationType = operationType,
        operationName = operationName,
        query = query,
        variables = bodyText.extractJsonValue("variables"),
    )
}

private fun String.extractJsonString(key: String): String? {
    val keyIndex = indexOfJsonKey(key)
    if (keyIndex < 0) return null
    val colonIndex = indexOf(':', keyIndex)
    if (colonIndex < 0) return null
    var index = colonIndex + 1
    while (index < length && this[index].isWhitespace()) index++
    if (index >= length || this[index] != '"') return null
    index++

    val builder = StringBuilder()
    var escaping = false
    while (index < length) {
        val char = this[index]
        when {
            escaping -> {
                builder.append(
                    when (char) {
                        'n' -> '\n'
                        'r' -> '\r'
                        't' -> '\t'
                        '"' -> '"'
                        '\\' -> '\\'
                        '/' -> '/'
                        else -> char
                    },
                )
                escaping = false
            }
            char == '\\' -> escaping = true
            char == '"' -> return builder.toString()
            else -> builder.append(char)
        }
        index++
    }
    return null
}

private fun String.extractJsonValue(key: String): String? {
    val keyIndex = indexOfJsonKey(key)
    if (keyIndex < 0) return null
    val colonIndex = indexOf(':', keyIndex)
    if (colonIndex < 0) return null
    var index = colonIndex + 1
    while (index < length && this[index].isWhitespace()) index++
    if (index >= length) return null

    val start = index
    var depth = 0
    var inString = false
    var escaping = false
    while (index < length) {
        val char = this[index]
        when {
            escaping -> escaping = false
            char == '\\' && inString -> escaping = true
            char == '"' -> inString = !inString
            !inString && (char == '{' || char == '[') -> depth++
            !inString && (char == '}' || char == ']') -> {
                if (depth == 0) return substring(start, index).trim().trimEnd(',')
                depth--
            }
            !inString && char == ',' && depth == 0 -> return substring(start, index).trim()
        }
        index++
    }
    return substring(start).trim().trimEnd('}')
}

private fun String.indexOfJsonKey(key: String): Int {
    return indexOf("\"$key\"")
}

private fun String.firstGraphQlKeyword(): String? {
    val cleaned = removeGraphQlComments().trimStart()
    return listOf("query", "mutation", "subscription").firstOrNull { keyword ->
        cleaned.startsWith(keyword)
    }
}

private fun String.operationNameAfter(operationType: String?): String? {
    if (operationType == null) return null
    val cleaned = removeGraphQlComments().trimStart()
    val afterType = cleaned.removePrefix(operationType).trimStart()
    if (afterType.isBlank() || afterType.first() == '(' || afterType.first() == '{') return null
    return afterType.takeWhile { it.isLetterOrDigit() || it == '_' }.takeIf { it.isNotBlank() }
}

private fun String.removeGraphQlComments(): String {
    return lineSequence()
        .map { line -> line.substringBefore("#") }
        .joinToString("\n")
}
