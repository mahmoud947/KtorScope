/**
 * Created by Mahmoud kamal El-Din on 02/05/2026
 */
package io.github.mahmoud.ktorscope.core

/**
 * Builds a shell-safe cURL command for this captured transaction request.
 */
fun NetworkTransaction.toCurlCommand(): String = request.toCurlCommand()

/**
 * Builds a shell-safe cURL command for this captured request.
 */
fun NetworkRequest.toCurlCommand(): String {
    val parts = mutableListOf("curl")
    parts += "-X"
    parts += method.uppercase().shellQuote()
    parts += url.shellQuote()

    headers.forEach { (name, values) ->
        values.forEach { value ->
            parts += "-H"
            parts += "$name: $value".shellQuote()
        }
    }

    body?.takeIf { it.isNotEmpty() }?.let { capturedBody ->
        parts += "--data-raw"
        parts += capturedBody.shellQuote()
    }

    return parts.joinToString(" ")
}

private fun String.shellQuote(): String {
    if (isEmpty()) return "''"
    return "'${replace("'", "'\"'\"'")}'"
}
