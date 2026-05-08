
@file:OptIn(kotlin.io.encoding.ExperimentalEncodingApi::class)

package io.github.mahmoud.ktorscope.persistence

import io.github.mahmoud.ktorscope.core.WebSocketFrameDirection
import io.github.mahmoud.ktorscope.core.WebSocketFrameInspection
import io.github.mahmoud.ktorscope.core.WebSocketFrameType
import kotlin.io.encoding.Base64

/**
 * Created by Mahmoud kamal El-Din on 08/05/2026
 */
internal object WebSocketFramesCodec {
    fun encode(frames: List<WebSocketFrameInspection>): String? {
        if (frames.isEmpty()) return null
        return frames.joinToString("\n") { frame ->
            listOf(
                frame.index.toString(),
                frame.direction.name,
                frame.type.name,
                frame.timestampMillis.toString(),
                frame.sizeBytes.toString(),
                frame.payload.orEmpty().token(),
                frame.payloadTruncated.toString(),
                frame.fin.toString(),
                frame.closeCode?.toString().orEmpty(),
                frame.closeReason.orEmpty().token(),
            ).joinToString("|")
        }
    }

    fun decode(value: String?): List<WebSocketFrameInspection> {
        if (value.isNullOrBlank()) return emptyList()
        return value.lineSequence().mapNotNull { line ->
            val parts = line.split("|")
            if (parts.size < 10) return@mapNotNull null
            WebSocketFrameInspection(
                index = parts[0].toIntOrNull() ?: return@mapNotNull null,
                direction = runCatching { WebSocketFrameDirection.valueOf(parts[1]) }.getOrNull()
                    ?: return@mapNotNull null,
                type = runCatching { WebSocketFrameType.valueOf(parts[2]) }.getOrNull() ?: return@mapNotNull null,
                timestampMillis = parts[3].toLongOrNull() ?: return@mapNotNull null,
                sizeBytes = parts[4].toLongOrNull() ?: return@mapNotNull null,
                payload = parts[5].untoken().takeIf { it.isNotEmpty() },
                payloadTruncated = parts[6].toBoolean(),
                fin = parts[7].toBoolean(),
                closeCode = parts[8].toLongOrNull(),
                closeReason = parts[9].untoken().takeIf { it.isNotEmpty() },
            )
        }.toList()
    }

    private fun String.token(): String = Base64.encode(encodeToByteArray())

    private fun String.untoken(): String {
        return runCatching { Base64.decode(this).decodeToString() }.getOrDefault("")
    }
}
