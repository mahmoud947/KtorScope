/**
 * Created by Mahmoud kamal El-Din on 02/05/2026
 */
package io.github.mahmoud.ktorscope.core

data class BodyPreview(
    val value: String,
    val truncated: Boolean,
)

fun String.toBodyPreview(maxBodySize: Int): BodyPreview {
    if (maxBodySize <= 0) return BodyPreview(value = "", truncated = isNotEmpty())
    return if (length > maxBodySize) {
        BodyPreview(value = take(maxBodySize), truncated = true)
    } else {
        BodyPreview(value = this, truncated = false)
    }
}
