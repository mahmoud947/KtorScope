/**
 * Created by Mahmoud kamal El-Din on 02/05/2026
 */
package io.github.mahmoud.ktorscope.compose.components

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive

internal data class JsonPreviewState(
    val isLoading: Boolean = false,
    val lines: List<JsonLine> = emptyList(),
    val error: String? = null,
)

internal data class JsonLine(
    val index: Int,
    val tokens: List<JsonToken>,
)

internal sealed interface JsonToken {
    data class Key(val text: String) : JsonToken
    data class StringValue(val text: String) : JsonToken
    data class NumberValue(val text: String) : JsonToken
    data class BooleanValue(val text: String) : JsonToken
    data object NullValue : JsonToken
    data class Symbol(val text: String) : JsonToken
    data class Plain(val text: String) : JsonToken
}

internal data class JsonPreviewChunk(
    val lines: List<JsonLine>,
    val error: String? = null,
    val isComplete: Boolean = false,
    val isLimited: Boolean = false,
)

internal class FastJsonPreviewFormatter(
    private val batchSize: Int = 300,
    private val maxLines: Int? = null,
) {
    fun format(rawJson: String): Flow<JsonPreviewChunk> = flow {
        val firstJsonChar = rawJson.indexOfFirstNonWhitespace()
        if (firstJsonChar == -1) {
            emit(JsonPreviewChunk(lines = listOf(JsonLine(0, listOf(JsonToken.Plain("Empty body")))), isComplete = true))
            return@flow
        }

        val first = rawJson[firstJsonChar]
        val formatter: LineEmitter = if (first == '{' || first == '[') {
            JsonLineEmitter()
        } else {
            PlainLineEmitter()
        }

        var chunk = mutableListOf<JsonLine>()
        var error: String? = null
        var limited = false
        try {
            formatter.scan(rawJson, firstJsonChar) { line ->
                if (!currentCoroutineContext().isActive) return@scan false
                chunk.add(line)
                if (chunk.size >= batchSize) {
                    emit(JsonPreviewChunk(lines = chunk))
                    chunk = mutableListOf()
                }
                if (maxLines != null && line.index + 1 >= maxLines) {
                    limited = true
                    return@scan false
                }
                true
            }
        } catch (throwable: Throwable) {
            error = throwable.message ?: "Unable to format body preview"
        }

        if (chunk.isNotEmpty() || error != null) {
            emit(JsonPreviewChunk(lines = chunk, error = error, isComplete = true, isLimited = limited))
        } else {
            emit(JsonPreviewChunk(lines = emptyList(), isComplete = true, isLimited = limited))
        }
    }.flowOn(Dispatchers.Default)
}

private interface LineEmitter {
    suspend fun scan(
        source: String,
        startIndex: Int,
        emitLine: suspend (JsonLine) -> Boolean,
    )
}

private class JsonLineEmitter : LineEmitter {
    private val maxLineChars = 4_000
    private var lineIndex = 0
    private var indent = 0
    private var expectingKey = false
    private var currentLength = 0
    private val currentTokens = mutableListOf<JsonToken>()

    override suspend fun scan(
        source: String,
        startIndex: Int,
        emitLine: suspend (JsonLine) -> Boolean,
    ) {
        var index = startIndex
        while (index < source.length) {
            val char = source[index]
            when {
                char.isWhitespace() -> index++
                char == '{' -> {
                    if (!addSymbol("{", emitLine)) return
                    expectingKey = true
                    index++
                    if (source.nextNonWhitespace(index) != '}') {
                        if (!emitCurrentLine(emitLine)) return
                        indent++
                    }
                }
                char == '[' -> {
                    if (!addSymbol("[", emitLine)) return
                    expectingKey = false
                    index++
                    if (source.nextNonWhitespace(index) != ']') {
                        if (!emitCurrentLine(emitLine)) return
                        indent++
                    }
                }
                char == '}' || char == ']' -> {
                    if (currentTokens.isNotEmpty()) {
                        if (!emitCurrentLine(emitLine)) return
                    }
                    indent = (indent - 1).coerceAtLeast(0)
                    if (!addSymbol(char.toString(), emitLine)) return
                    expectingKey = false
                    index++
                }
                char == ',' -> {
                    if (!addSymbol(",", emitLine)) return
                    index++
                    if (!emitCurrentLine(emitLine)) return
                    expectingKey = source.nextContainerToken(index) != ']'
                }
                char == ':' -> {
                    if (!addSymbol(": ", emitLine)) return
                    expectingKey = false
                    index++
                }
                char == '"' -> {
                    val endIndex = source.stringEnd(index)
                    val isKey = expectingKey && source.nextNonWhitespace(endIndex + 1) == ':'
                    if (isKey) {
                        if (!addStringChunks(source, index, endIndex, emitLine, JsonToken::Key)) return
                    } else {
                        if (!addStringChunks(source, index, endIndex, emitLine, JsonToken::StringValue)) return
                    }
                    index = endIndex + 1
                }
                char == 't' && source.startsWithJsonLiteral(index, "true") -> {
                    if (!addToken(JsonToken.BooleanValue("true"), "true", emitLine)) return
                    index += 4
                }
                char == 'f' && source.startsWithJsonLiteral(index, "false") -> {
                    if (!addToken(JsonToken.BooleanValue("false"), "false", emitLine)) return
                    index += 5
                }
                char == 'n' && source.startsWithJsonLiteral(index, "null") -> {
                    if (!addToken(JsonToken.NullValue, "null", emitLine)) return
                    index += 4
                }
                char.isJsonNumberStart() -> {
                    val endIndex = source.numberEnd(index)
                    val number = source.substring(index, endIndex)
                    if (!addToken(JsonToken.NumberValue(number), number, emitLine)) return
                    index = endIndex
                }
                else -> {
                    if (!addToken(JsonToken.Plain(char.toString()), char.toString(), emitLine)) return
                    index++
                }
            }
        }
        if (currentTokens.isNotEmpty()) {
            emitCurrentLine(emitLine)
        }
    }

    private suspend fun addSymbol(value: String, emitLine: suspend (JsonLine) -> Boolean): Boolean {
        return addToken(JsonToken.Symbol(value), value, emitLine)
    }

    private suspend fun addToken(
        token: JsonToken,
        text: String,
        emitLine: suspend (JsonLine) -> Boolean,
    ): Boolean {
        if (currentLength > 0 && currentLength + text.length > maxLineChars) {
            if (!emitCurrentLine(emitLine)) return false
        }
        currentTokens.add(token)
        currentLength += text.length
        return true
    }

    private suspend fun addStringChunks(
        source: String,
        startIndex: Int,
        endIndex: Int,
        emitLine: suspend (JsonLine) -> Boolean,
        createToken: (String) -> JsonToken,
    ): Boolean {
        var chunkStart = startIndex
        while (chunkStart <= endIndex) {
            val chunkEnd = minOf(endIndex + 1, chunkStart + maxLineChars)
            val chunk = source.substring(chunkStart, chunkEnd)
            if (!addToken(createToken(chunk), chunk, emitLine)) return false
            chunkStart = chunkEnd
            if (chunkStart <= endIndex && !emitCurrentLine(emitLine)) return false
        }
        return true
    }

    private suspend fun emitCurrentLine(emitLine: suspend (JsonLine) -> Boolean): Boolean {
        val tokens = buildList {
            if (indent > 0) add(JsonToken.Plain("  ".repeat(indent)))
            addAll(currentTokens)
        }
        currentTokens.clear()
        currentLength = 0
        return emitLine(JsonLine(lineIndex++, tokens))
    }
}

private class PlainLineEmitter : LineEmitter {
    private val maxLineChars = 4_000

    override suspend fun scan(
        source: String,
        startIndex: Int,
        emitLine: suspend (JsonLine) -> Boolean,
    ) {
        var lineIndex = 0
        var lineStart = startIndex
        var index = startIndex
        while (index < source.length) {
            if (source[index] == '\n') {
                if (!emitLine(JsonLine(lineIndex++, listOf(JsonToken.Plain(source.substring(lineStart, index)))))) return
                lineStart = index + 1
            } else if (index - lineStart >= maxLineChars) {
                if (!emitLine(JsonLine(lineIndex++, listOf(JsonToken.Plain(source.substring(lineStart, index)))))) return
                lineStart = index
            }
            index++
        }
        if (lineStart <= source.length) {
            emitLine(JsonLine(lineIndex, listOf(JsonToken.Plain(source.substring(lineStart)))))
        }
    }
}

private fun String.indexOfFirstNonWhitespace(): Int {
    for (index in indices) {
        if (!this[index].isWhitespace()) return index
    }
    return -1
}

private fun String.nextNonWhitespace(startIndex: Int): Char? {
    for (index in startIndex until length) {
        val char = this[index]
        if (!char.isWhitespace()) return char
    }
    return null
}

private fun String.nextContainerToken(startIndex: Int): Char? {
    for (index in startIndex until length) {
        val char = this[index]
        if (!char.isWhitespace()) return char
    }
    return null
}

private fun String.stringEnd(startIndex: Int): Int {
    var index = startIndex + 1
    var escaping = false
    while (index < length) {
        val char = this[index]
        when {
            escaping -> escaping = false
            char == '\\' -> escaping = true
            char == '"' -> return index
        }
        index++
    }
    return lastIndex
}

private fun Char.isJsonNumberStart(): Boolean = isDigit() || this == '-'

private fun String.numberEnd(startIndex: Int): Int {
    var index = startIndex
    while (index < length && this[index] in "-0123456789.eE+") {
        index++
    }
    return index
}

private fun String.startsWithJsonLiteral(startIndex: Int, literal: String): Boolean {
    if (!startsWith(literal, startIndex)) return false
    val endIndex = startIndex + literal.length
    val before = getOrNull(startIndex - 1)
    val after = getOrNull(endIndex)
    return before.isJsonBoundary() && after.isJsonBoundary()
}

private fun Char?.isJsonBoundary(): Boolean {
    return this == null || isWhitespace() || this in "{}[]:,"
}
