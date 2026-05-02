/**
 * Created by Mahmoud kamal El-Din on 02/05/2026
 */
package io.github.mahmoud.ktorscope.compose.components

import io.github.mahmoud.ktorscope.core.NetworkError
import io.github.mahmoud.ktorscope.core.NetworkRequest
import io.github.mahmoud.ktorscope.core.NetworkResponse
import io.github.mahmoud.ktorscope.core.NetworkTransaction

internal object KtorScopePreviewData {
    val transactions: List<NetworkTransaction> = listOf(
        NetworkTransaction(
            id = "preview-1",
            request = NetworkRequest(
                method = "GET",
                url = "https://api.github.com/users/octocat",
                headers = mapOf("Accept" to listOf("application/json")),
            ),
            response = NetworkResponse(
                statusCode = 200,
                statusDescription = "OK",
                headers = mapOf("Content-Type" to listOf("application/json")),
                body = """{"login":"octocat","name":"The Octocat"}""",
            ),
            durationMillis = 128,
            createdAtMillis = 1_714_633_200_000,
        ),
        NetworkTransaction(
            id = "preview-2",
            request = NetworkRequest(
                method = "POST",
                url = "https://api.example.com/graphql",
                headers = mapOf("Content-Type" to listOf("application/json")),
                body = """
                    {
                      "operationName": "ViewerProfile",
                      "query": "query ViewerProfile(${"$"}login: String!) { user(login: ${"$"}login) { id name login } }",
                      "variables": { "login": "octocat" }
                    }
                """.trimIndent(),
            ),
            response = NetworkResponse(
                statusCode = 201,
                statusDescription = "Created",
                headers = mapOf("Content-Type" to listOf("application/json")),
                body = """{"data":{"user":{"id":"1","name":"The Octocat","login":"octocat"}}}""",
            ),
            durationMillis = 264,
            createdAtMillis = 1_714_633_260_000,
        ),
        NetworkTransaction(
            id = "preview-3",
            request = NetworkRequest(
                method = "DELETE",
                url = "https://api.example.com/items/42",
                headers = mapOf("Authorization" to listOf("██")),
            ),
            error = NetworkError(
                type = "ConnectTimeoutException",
                message = "Connection timed out",
            ),
            durationMillis = 1_504,
            createdAtMillis = 1_714_633_320_000,
        ),
    )
}
