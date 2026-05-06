package io.github.mahmoud.ktorscope

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.mahmoud.ktorscope.compose.KtorScopeScreen
import io.github.mahmoud.ktorscope.compose.KtorScopeThemeMode
import io.github.mahmoud.ktorscope.core.KtorScopeHistoryPersistence
import io.github.mahmoud.ktorscope.ktor.KtorScope
import io.github.mahmoud.ktorscope.persistence.KtorScopePersistence
import io.github.mahmoud947.composeapp.generated.resources.Res
import io.github.mahmoud947.composeapp.generated.resources.ktorscope_logo
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
import org.jetbrains.compose.resources.painterResource

@Composable
fun App(
    ktorScopePersistence: KtorScopePersistence
) {
    MaterialTheme {
        var showInspector by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()
        val networkPersistence = remember { ktorScopePersistence }
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
            Surface(Modifier.fillMaxSize(), color = Color(0xFF07111F)) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                0f to Color(0xFF07111F),
                                0.55f to Color(0xFF101827),
                                1f to Color(0xFF070A12),
                            ),
                        )
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    SampleHero(
                        status = status,
                        onOpenInspector = { showInspector = true },
                    )
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
                                        setBody(
                                            """{
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
}"""
                                        )
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
                    CapturePreview(status = status)
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
    Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(16.dp)) {
        SectionHeader(
            title = "Request Lab",
            subtitle = "Generate traffic, failures, payloads, and GraphQL traces.",
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            RequestAction(
                method = "GET",
                title = "Success",
                subtitle = "200 response",
                accent = Color(0xFF5EEAD4),
                onClick = onSuccess,
                modifier = Modifier.weight(1f),
            )
            RequestAction(
                method = "ERR",
                title = "Failure",
                subtitle = "DNS error",
                accent = Color(0xFFFF6B6B),
                onClick = onFailure,
                modifier = Modifier.weight(1f),
            )
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            RequestAction(
                method = "POST",
                title = "Body",
                subtitle = "JSON + secrets",
                accent = Color(0xFFFFD166),
                onClick = onPost,
                modifier = Modifier.weight(1f),
            )
            RequestAction(
                method = "GET",
                title = "Delayed",
                subtitle = "2s timing",
                accent = Color(0xFF90CAF9),
                onClick = onDelayed,
                modifier = Modifier.weight(1f),
            )
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            RequestAction(
                method = "PUT",
                title = "PUT",
                subtitle = "Update",
                accent = Color(0xFFB8F7A3),
                compact = true,
                onClick = onPut,
                modifier = Modifier.weight(1f),
            )
            RequestAction(
                method = "PATCH",
                title = "PATCH",
                subtitle = "Partial",
                accent = Color(0xFFD8B4FE),
                compact = true,
                onClick = onPatch,
                modifier = Modifier.weight(1f),
            )
            RequestAction(
                method = "DEL",
                title = "DELETE",
                subtitle = "Remove",
                accent = Color(0xFFFFA3A3),
                compact = true,
                onClick = onDelete,
                modifier = Modifier.weight(1f),
            )
        }
        RequestAction(
            method = "GQL",
            title = "GraphQL ViewerProfile",
            subtitle = "Operation name, query, and variables preview",
            accent = Color(0xFFFF8BD1),
            onClick = onGraphQl,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun SampleHero(
    status: String,
    onOpenInspector: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF15233B),
                        Color(0xFF0B5C75),
                        Color(0xFF15131F),
                    ),
                ),
            )
            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
            .padding(18.dp),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(top = 35.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AssistChip(
                    onClick = {},
                    label = {
                        Text(
                            "Ktor Client Inspector",
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    },
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.22f)),
                )
                StatusDot(active = status.startsWith("Ready").not())
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "KtorScope",
                    color = Color.White,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Black,
                )
                Image(
                    painter = painterResource(Res.drawable.ktorscope_logo),
                    contentDescription = null,
                    modifier = Modifier.size(150.dp).align(Alignment.CenterHorizontally),
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp)
            ) {
                Button(
                    onClick = onOpenInspector,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF5EEAD4),
                        contentColor = Color(0xFF05201D),
                    ),
                ) {
                    Text("Open Inspector", fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = onOpenInspector,
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.32f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                ) {
                    Text("View Logs")
                }
            }
        }
    }
}

@Composable
private fun RequestAction(
    method: String,
    title: String,
    subtitle: String,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(if (compact) 104.dp else 118.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = method,
                color = accent,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = subtitle,
                    color = Color(0xFF9CA3AF),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun CapturePreview(status: String) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeader(
            title = "Live Capture",
            subtitle = "A video-friendly console for the latest sample action.",
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0B1220)),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusDot(active = status.startsWith("Ready").not())
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = status,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricTile("Headers", "Redacted", Color(0xFF5EEAD4), Modifier.weight(1f))
                    MetricTile("Bodies", "Captured", Color(0xFFFFD166), Modifier.weight(1f))
                    MetricTile("History", "Persisted", Color(0xFF90CAF9), Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun MetricTile(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(8.dp))
            .aspectRatio(1.1f)
            .padding(10.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(accent),
        )
        Column {
            Text(
                text = value,
                color = Color.White,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = label,
                color = Color(0xFF9CA3AF),
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = title,
            color = Color.White,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = subtitle,
            color = Color(0xFFA8B3C7),
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun StatusDot(active: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(9.dp)
                .clip(CircleShape)
                .background(if (active) Color(0xFF5EEAD4) else Color(0xFF6B7280)),
        )
        Text(
            text = if (active) "Captured" else "Ready",
            color = Color.White.copy(alpha = 0.82f),
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.End,
        )
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
