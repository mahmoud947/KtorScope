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
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.wss
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.websocket.Frame
import io.ktor.websocket.readBytes
import io.ktor.websocket.readText
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

@Composable
fun App(
    ktorScopePersistence: KtorScopePersistence
) {
    MaterialTheme {
        var showInspector by remember { mutableStateOf(false) }
        val lightMode = SampleLightMode
        val sampleColors = remember(lightMode) { sampleColors(lightMode) }
        val inspectorThemeMode = if (lightMode) KtorScopeThemeMode.Light else KtorScopeThemeMode.Dark
        val scope = rememberCoroutineScope()
        val networkPersistence = remember { ktorScopePersistence }
        val client = rememberSampleClient(networkPersistence.historyPersistence)
        var status by remember { mutableStateOf("Ready") }

        if (showInspector) {
            KtorScopeScreen(
                onBackClicked = { showInspector = false },
                themeMode = inspectorThemeMode,
                persistHistory = true,
                onLoadFullBody = networkPersistence.bodyFileStore::readBody,
            )
        } else {
            Surface(Modifier.fillMaxSize(), color = sampleColors.backgroundStart) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                0f to sampleColors.backgroundStart,
                                0.55f to sampleColors.backgroundMid,
                                1f to sampleColors.backgroundEnd,
                            ),
                        )
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    SampleHero(
                        status = status,
                        colors = sampleColors,
                        onOpenInspector = { showInspector = true },
                    )
                    RequestButtons(
                        colors = sampleColors,
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
                        onWebSocket = {
                            scope.launch {
                                status = runSampleWebSocket(client)
                            }
                        },
                    )
                    CapturePreview(status = status, colors = sampleColors)
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
            install(WebSockets)
            install(KtorScope) {
                enabled = true
                captureBodies = true
                captureWebSocketFrames = true
                maxWebSocketFramePreviewSize = 64_000
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
    colors: SampleColors,
    onSuccess: () -> Unit,
    onFailure: () -> Unit,
    onPost: () -> Unit,
    onDelayed: () -> Unit,
    onPut: () -> Unit,
    onPatch: () -> Unit,
    onDelete: () -> Unit,
    onGraphQl: () -> Unit,
    onWebSocket: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(16.dp)) {
        SectionHeader(
            title = "Request Lab",
            subtitle = "Generate traffic, failures, payloads, and GraphQL traces.",
            colors = colors,
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            RequestAction(
                method = "GET",
                title = "Success",
                subtitle = "200 response",
                accent = Color(0xFF5EEAD4),
                colors = colors,
                onClick = onSuccess,
                modifier = Modifier.weight(1f),
            )
            RequestAction(
                method = "ERR",
                title = "Failure",
                subtitle = "DNS error",
                accent = Color(0xFFFF6B6B),
                colors = colors,
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
                colors = colors,
                onClick = onPost,
                modifier = Modifier.weight(1f),
            )
            RequestAction(
                method = "GET",
                title = "Delayed",
                subtitle = "2s timing",
                accent = Color(0xFF90CAF9),
                colors = colors,
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
                colors = colors,
                compact = true,
                onClick = onPut,
                modifier = Modifier.weight(1f),
            )
            RequestAction(
                method = "PATCH",
                title = "PATCH",
                subtitle = "Partial",
                accent = Color(0xFFD8B4FE),
                colors = colors,
                compact = true,
                onClick = onPatch,
                modifier = Modifier.weight(1f),
            )
            RequestAction(
                method = "DEL",
                title = "DELETE",
                subtitle = "Remove",
                accent = Color(0xFFFFA3A3),
                colors = colors,
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
            colors = colors,
            onClick = onGraphQl,
            modifier = Modifier.fillMaxWidth(),
        )
        RequestAction(
            method = "WS",
            title = "WebSocket echo",
            subtitle = "Handshake, sent frames, received echoes",
            accent = Color(0xFF38BDF8),
            colors = colors,
            onClick = onWebSocket,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun SampleHero(
    status: String,
    colors: SampleColors,
    onOpenInspector: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        colors.heroStart,
                        colors.heroMid,
                        colors.heroEnd,
                    ),
                ),
            )
            .border(1.dp, colors.border, RoundedCornerShape(8.dp))
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
                            color = colors.secondaryText
                        )
                    },
                    border = BorderStroke(1.dp, colors.border),
                )
                StatusDot(active = status.startsWith("Ready").not(), colors = colors)
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "KtorScope",
                    color = colors.primaryText,
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
                        containerColor = colors.ctaContainer,
                        contentColor = colors.ctaContent,
                    ),
                ) {
                    Text("Open Inspector", fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = onOpenInspector,
                    border = BorderStroke(1.dp, colors.borderStrong),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.primaryText),
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
    colors: SampleColors,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(if (compact) 104.dp else 118.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = colors.card),
        border = BorderStroke(1.dp, colors.border),
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
                    color = colors.primaryText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = subtitle,
                    color = colors.secondaryText,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun CapturePreview(status: String, colors: SampleColors) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeader(
            title = "Live Capture",
            subtitle = "A video-friendly console for the latest sample action.",
            colors = colors,
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = colors.console),
            border = BorderStroke(1.dp, colors.border),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusDot(active = status.startsWith("Ready").not(), colors = colors)
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = status,
                        color = colors.primaryText,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricTile("Headers", "Redacted", Color(0xFF5EEAD4), colors, Modifier.weight(1f))
                    MetricTile("Bodies", "Captured", Color(0xFFFFD166), colors, Modifier.weight(1f))
                    MetricTile("History", "Persisted", Color(0xFF90CAF9), colors, Modifier.weight(1f))
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
    colors: SampleColors,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(colors.tile)
            .border(1.dp, colors.border, RoundedCornerShape(8.dp))
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
                color = colors.primaryText,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = label,
                color = colors.secondaryText,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String, subtitle: String, colors: SampleColors) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = title,
            color = colors.primaryText,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = subtitle,
            color = colors.secondaryText,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun StatusDot(active: Boolean, colors: SampleColors) {
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
            color = colors.primaryText.copy(alpha = 0.82f),
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.End,
        )
    }
}

private data class SampleColors(
    val backgroundStart: Color,
    val backgroundMid: Color,
    val backgroundEnd: Color,
    val heroStart: Color,
    val heroMid: Color,
    val heroEnd: Color,
    val card: Color,
    val console: Color,
    val tile: Color,
    val border: Color,
    val borderStrong: Color,
    val primaryText: Color,
    val secondaryText: Color,
    val ctaContainer: Color,
    val ctaContent: Color,
)

private const val SampleLightMode = true

private fun sampleColors(lightMode: Boolean): SampleColors {
    return if (lightMode) {
        SampleColors(
            backgroundStart = Color(0xFFF8FAFC),
            backgroundMid = Color(0xFFEAF3F7),
            backgroundEnd = Color(0xFFF5F7FB),
            heroStart = Color(0xFFE9F8F5),
            heroMid = Color(0xFFD7ECFF),
            heroEnd = Color(0xFFFFFFFF),
            card = Color(0xFFFFFFFF),
            console = Color(0xFFFFFFFF),
            tile = Color(0xFFF1F5F9),
            border = Color(0x1F0F172A),
            borderStrong = Color(0x660F172A),
            primaryText = Color(0xFF0F172A),
            secondaryText = Color(0xFF516070),
            ctaContainer = Color(0xFF0F766E),
            ctaContent = Color.White,
        )
    } else {
        SampleColors(
            backgroundStart = Color(0xFF07111F),
            backgroundMid = Color(0xFF101827),
            backgroundEnd = Color(0xFF070A12),
            heroStart = Color(0xFF15233B),
            heroMid = Color(0xFF0B5C75),
            heroEnd = Color(0xFF15131F),
            card = Color(0xFF111827),
            console = Color(0xFF0B1220),
            tile = Color.White.copy(alpha = 0.05f),
            border = Color.White.copy(alpha = 0.1f),
            borderStrong = Color.White.copy(alpha = 0.32f),
            primaryText = Color.White,
            secondaryText = Color(0xFFA8B3C7),
            ctaContainer = Color(0xFF5EEAD4),
            ctaContent = Color(0xFF05201D),
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

private suspend fun runSampleWebSocket(client: HttpClient): String {
    return runCatching {
        var textEcho = ""
        var binaryEchoBytes = 0
        client.wss("wss://ws.postman-echo.com/raw") {
            val textMessage = """{"source":"KtorScope","kind":"text-frame","value":"hello websocket"}"""
            send(Frame.Text(textMessage))
            textEcho = withTimeout(5_000) {
                (incoming.receive() as? Frame.Text)?.readText().orEmpty()
            }

            val binaryMessage = "KtorScope binary frame".encodeToByteArray()
            send(Frame.Binary(fin = true, data = binaryMessage))
            binaryEchoBytes = withTimeout(5_000) {
                incoming.receive().readBytes().size
            }
        }
        "WebSocket echoed ${textEcho.length} text chars and $binaryEchoBytes binary bytes. Open KtorScope frames."
    }.getOrElse { cause ->
        "WebSocket failed: ${cause.message.orEmpty()}"
    }
}
