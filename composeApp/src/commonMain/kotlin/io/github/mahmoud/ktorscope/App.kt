package io.github.mahmoud.ktorscope

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
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.mahmoud.ktorscope.compose.KtorScopeScreen
import io.github.mahmoud.ktorscope.compose.KtorScopeThemeMode
import io.github.mahmoud.ktorscope.core.KtorScopeHistoryPersistence
import io.github.mahmoud.ktorscope.ktor.KtorScope
import io.github.mahmoud.ktorscope.persistence.KtorScopePersistence
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.launch

@Composable
fun App(
    ktorScopePersistence: KtorScopePersistence
) {
    MaterialTheme {
        var showInspector by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()
        val networkPersistence = remember {  ktorScopePersistence}
        val client = rememberSampleClient(networkPersistence.historyPersistence)
        var status by remember { mutableStateOf("Ready") }

        if (showInspector) {
            KtorScopeScreen(
                onBackClicked = { showInspector = false },
                themeMode = KtorScopeThemeMode.Dark,
                persistHistory = true,
                onLoadFullBody = networkPersistence.bodyFileStore::readBody,
            )
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
                                status = runSampleCall {
                                    client.get("https://httpbin.org/get").body<String>()
                                }
                            }
                        },
                        onFailure = {
                            scope.launch {
                                status = runSampleCall {
                                    client.get("https://invalid.ktorscope.local/failure")
                                        .body<String>()
                                }
                            }
                        },
                        onPost = {
                            scope.launch {
                                status = runSampleCall {
                                    client.post("https://httpbin.org/post") {
                                        contentType(ContentType.Application.Json)
                                        header("Authorization", "Bearer super-secret-token")
                                        header("X-Api-Key", "sample-key")
                                        setBody("""{
  "id":"6ce4a00a-677d-4265-8144-4873d3d0075d",
  "url":"https://api.filepreviews.io/v2/previews/6ce4a00a-677d-4265-8144-4873d3d0075d/",
  "status":"success",
  "preview":{
    "original_size":{
      "width":"1280",
      "height":"1024"
    },
    "page":1,
    "size":{
      "width":"1280",
      "height":"1024"
    },
    "url":"https://s3.amazonaws.com/demo.filepreviews.io/bb3d2c82dac99a0509b43f9ee3dd655f93368bb21855c75ec326f92547f3ed06/2ce8aa80407b1ca9b80e6a51ff24c1ab_original-1.png",
    "resized":false,
    "requested_size":"original"
  },
  "thumbnails":[
    {
      "original_size":{
        "width":"1280",
        "height":"1024"
      },
      "page":1,
      "size":{
        "width":"1280",
        "height":"1024"
      },
      "url":"https://s3.amazonaws.com/demo.filepreviews.io/bb3d2c82dac99a0509b43f9ee3dd655f93368bb21855c75ec326f92547f3ed06/2ce8aa80407b1ca9b80e6a51ff24c1ab_original-1.png",
      "resized":false,
      "requested_size":"original"
    }
  ],
  "original_file":{
    "name":"i07VJLqZ3bd",
    "size":21258,
    "encoding":"utf-8",
    "metadata":{
      "webpage":{
        "oembed":{
          "author_name":"Bubblebeccapugs",
          "version":1,
          "title":"Sunday Funday #pugsofVine #dogsofvine #pug #dog #cute #happy #StellaArtois &#x1f37a;☀️&#x1f60e;",
          "author_url":"https://vine.co/u/1148651418689925120",
          "thumbnail_width":480,
          "cache_age":3153600000,
          "provider_url":"https://vine.co/",
          "thumbnail_url":"https://v.cdn.vine.co/r/thumbs/97026346B01343637394330906624_5580a4e3be3.17.0.14983072149648355644.mp4.jpg?versionId=LvMeaYzdPviCoUZIPD2kf8TSa9ReKjmU",
          "width":600,
          "thumbnail_height":480,
          "type":"video",
          "provider_name":"Vine",
          "height":600,
          "html":"<iframe class="          \&quot;vine-embed\&quot;" src="\&quot;https://vine.co/v/i07VJLqZ3bd/embed/simple\&quot;" width="\&quot;600\&quot;" height="\&quot;600\&quot;" frameborder="\&quot;0\&quot;"></iframe><script src="\&quot;//platform.vine.co/static/scripts/embed.js\&quot;" async=""></script>"
        },
        "videos":[
          {
            "width":535,
            "height":535,
            "type":"text/html",
            "src":"http://vine.co/v/i07VJLqZ3bd/fb-card?audio=1",
            "secure_src":"https://vine.co/v/i07VJLqZ3bd/fb-card?audio=1"
          }
        ],
        "title":"Sunday Funday #pugsofVine #dogsofvine #pug #dog #cute #happy #StellaArtois &#x1f37a;☀️&#x1f60e;",
        "images":[
          {
            "type":"og:image",
            "src":"https://v.cdn.vine.co/r/thumbs/97026346B01343637394330906624_5580a4e3be3.17.0.14983072149648355644.mp4.jpg?versionId=LvMeaYzdPviCoUZIPD2kf8TSa9ReKjmU"
          },
          {
            "type":"favicon",
            "src":"https://v.cdn.vine.co/w/c034b859-assets/images/favicon.ico"
          }
        ],
        "url":"https://vine.co/v/i07VJLqZ3bd",
        "locale":"en_US",
        "description":"Vine by Bubblebeccapugs"
      }
    },
    "mimetype":"text/html",
    "type":"text",
    "total_pages":1,
    "extension":""
  },
  "user_data":null
}""")
                                    }.body<String>()
                                }
                            }
                        },
                        onDelayed = {
                            scope.launch {
                                status = runSampleCall {
                                    client.get("https://httpbin.org/delay/2").body<String>()
                                }
                            }
                        },
                        onPut = {
                            scope.launch {
                                status = runSampleCall {
                                    client.put("https://httpbin.org/put") {
                                        contentType(ContentType.Application.Json)
                                        setBody("""{"method":"PUT","message":"Updated from KtorScope"}""")
                                    }.body<String>()
                                }
                            }
                        },
                        onPatch = {
                            scope.launch {
                                status = runSampleCall {
                                    client.patch("https://httpbin.org/patch") {
                                        contentType(ContentType.Application.Json)
                                        setBody("""{"method":"PATCH","message":"Partially updated from KtorScope"}""")
                                    }.body<String>()
                                }
                            }
                        },
                        onDelete = {
                            scope.launch {
                                status = runSampleCall {
                                    client.delete("https://httpbin.org/delete").body<String>()
                                }
                            }
                        },
                        onGraphQl = {
                            scope.launch {
                                status = runSampleCall {
                                    client.post("https://httpbin.org/post") {
                                        contentType(ContentType.Application.Json)
                                        setBody(
                                            """
                                            {
                                              "operationName": "ViewerProfile",
                                              "query": "query ViewerProfile(${"$"}login: String!) { user(login: ${"$"}login) { id name login } }",
                                              "variables": { "login": "octocat" }
                                            }
                                            """.trimIndent(),
                                        )
                                    }.body<String>()
                                }
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
private fun rememberSampleClient(
    persistence: KtorScopeHistoryPersistence,
): HttpClient {
    return remember(persistence) {
        HttpClient {
            install(ContentNegotiation)
            install(KtorScope) {
                enabled = true
                captureBodies = true
                maxBodySize = 250_000
                historyPersistence {
                    enabled = true
                    maxRecords = 300
                    this.persistence = persistence
                }
                redactHeaders = setOf("Authorization", "Cookie", "Set-Cookie", "X-Api-Key")
                prettyPrint = true
                prettyPrintConfig {
                    includeHeaders = true
                    includeBodies = true
                    includeCurl = true
                    includeGraphQl = true
                    prettyJson = true
                }
                logger = { message -> println(message) }
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
    onPut: () -> Unit,
    onPatch: () -> Unit,
    onDelete: () -> Unit,
    onGraphQl: () -> Unit,
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
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = onPut, modifier = Modifier.weight(1f)) { Text("PUT") }
            Button(onClick = onPatch, modifier = Modifier.weight(1f)) { Text("PATCH") }
            Button(onClick = onDelete, modifier = Modifier.weight(1f)) { Text("DELETE") }
        }
        Button(onClick = onGraphQl, modifier = Modifier.fillMaxWidth()) {
            Text("GraphQL")
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
