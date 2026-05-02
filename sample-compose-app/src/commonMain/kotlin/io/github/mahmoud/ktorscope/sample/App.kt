/**
 * Created by Mahmoud kamal El-Din on 02/05/2026
 */
package io.github.mahmoud.ktorscope.sample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.mahmoud.ktorscope.compose.KtorScopeScreen
import io.github.mahmoud.ktorscope.ktor.KtorScope
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.launch

@Composable
fun App() {
    MaterialTheme {
        var showInspector by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()
        val client = rememberSampleClient()
        var status by remember { mutableStateOf("Ready") }

        if (showInspector) {
            KtorScopeScreen()
        } else {
            Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text("KtorScope Sample", style = MaterialTheme.typography.headlineSmall)
                    Text(status, style = MaterialTheme.typography.bodyMedium)
                    RequestButtons(
                        onSuccess = {
                            scope.launch {
                                status = runSampleCall { client.get("https://httpbin.org/get").body<String>() }
                            }
                        },
                        onFailure = {
                            scope.launch {
                                status = runSampleCall { client.get("https://invalid.ktorscope.local/failure").body<String>() }
                            }
                        },
                        onPost = {
                            scope.launch {
                                status = runSampleCall {
                                    client.post("https://httpbin.org/post") {
                                        contentType(ContentType.Application.Json)
                                        header("Authorization", "Bearer super-secret-token")
                                        header("X-Api-Key", "sample-key")
                                        setBody("""{"message":"Hello from KtorScope"}""")
                                    }.body<String>()
                                }
                            }
                        },
                        onDelayed = {
                            scope.launch {
                                status = runSampleCall { client.get("https://httpbin.org/delay/2").body<String>() }
                            }
                        },
                    )
                    Button(onClick = { showInspector = true }) {
                        Text("Open KtorScope")
                    }
                }
            }
        }
    }
}

@Composable
private fun rememberSampleClient(): HttpClient {
    return remember {
        HttpClient {
            install(ContentNegotiation)
            install(KtorScope) {
                enabled = true
                captureBodies = true
                maxBodySize = 250_000
                redactHeaders = setOf("Authorization", "Cookie", "Set-Cookie", "X-Api-Key")
            }
        }
    }
}

@Composable
private fun RequestButtons(
    onSuccess: () -> Unit,
    onFailure: () -> Unit,
    onPost: () -> Unit,
    onDelayed: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = onSuccess, modifier = Modifier.weight(1f)) { Text("Success") }
            Button(onClick = onFailure, modifier = Modifier.weight(1f)) { Text("Failure") }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = onPost, modifier = Modifier.weight(1f)) { Text("POST body") }
            Button(onClick = onDelayed, modifier = Modifier.weight(1f)) { Text("Delayed") }
        }
    }
}

private suspend fun runSampleCall(block: suspend () -> String): String {
    return runCatching {
        block()
        "Request finished. Open KtorScope to inspect it."
    }.getOrElse { cause ->
        "Request failed: ${cause.message.orEmpty()}"
    }
}
