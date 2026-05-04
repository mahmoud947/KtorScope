/**
 * Created by Mahmoud kamal El-Din on 04/05/2026
 */
@file:OptIn(kotlin.io.encoding.ExperimentalEncodingApi::class)

package io.github.mahmoud.ktorscope.persistence

import kotlin.io.encoding.Base64

internal object HeadersCodec {
    fun encode(headers: Map<String, List<String>>): String? {
        if (headers.isEmpty()) return null
        return headers.entries.joinToString("\n") { (name, values) ->
            "${name.token()}:${values.joinToString(",") { it.token() }}"
        }
    }

    fun decode(value: String?): Map<String, List<String>> {
        if (value.isNullOrBlank()) return emptyMap()
        return value.lineSequence().mapNotNull { line ->
            val name = line.substringBefore(":", missingDelimiterValue = "")
            if (name.isBlank()) return@mapNotNull null
            val values = line.substringAfter(":", missingDelimiterValue = "")
                .takeIf { it.isNotBlank() }
                ?.split(",")
                ?.map { it.untoken() }
                .orEmpty()
            name.untoken() to values
        }.toMap()
    }

    private fun String.token(): String = Base64.encode(encodeToByteArray())

    private fun String.untoken(): String {
        return runCatching { Base64.decode(this).decodeToString() }.getOrDefault("")
    }
}
