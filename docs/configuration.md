# Configuration

KtorScope is configured from the Ktor Client plugin block:

```kotlin
install(KtorScope) {
    enabled = true
    captureBodies = true
    captureWebSocketFrames = true
    maxWebSocketFramePreviewSize = 64_000
    maxBodySize = 250_000
    historyPersistence {
        enabled = false
        maxRecords = 500
        maxBodyPreviewSize = 250_000
        largeBodyFileThreshold = 250_000
    }
    redactHeaders = setOf("Authorization", "Cookie", "Set-Cookie", "X-Api-Key")
    store = KtorScopeStore.shared
    prettyPrint = false
    logger = { message -> println(message) }
}
```

## Plugin Options

| Option | Default | Description |
| --- | --- | --- |
| `enabled` | `true` | Turns capture on or off without removing the plugin. |
| `captureBodies` | `true` | Captures supported request and response body previews. |
| `captureWebSocketFrames` | `true` | Captures WebSocket session metadata and incoming/outgoing frames when Ktor's `WebSockets` plugin is installed. |
| `maxBodySize` | `250_000` | Legacy body preview size shortcut. Prefer `historyPersistence { maxBodyPreviewSize = ... }` for persisted history. |
| `maxBodyPreviewSize` | `250_000` | Maximum captured body preview length. Longer bodies are truncated unless persistence keeps full bodies for file storage. |
| `maxWebSocketFramePreviewSize` | `64_000` | Maximum text payload or hex-encoded binary/control frame preview length. Larger frames are marked truncated. |
| `largeBodyFileThreshold` | `250_000` | Size threshold used by persistence when deciding whether full body content should move to platform file storage. |
| `historyPersistenceConfig` | `KtorScopeHistoryPersistenceConfig()` | Optional persistence config. Room lives in `ktorscope-persistence`, not core. |
| `historyPersistence { ... }` | disabled | Builder for enabling persistence, max records, body preview size, large-body threshold, and adapter. |
| `redactHeaders` | `Authorization`, `Cookie`, `Set-Cookie`, `X-Api-Key`, `Api-Key`, `access_token`, `refresh_token` | Case-insensitive header names to redact before storage. |
| `store` | `KtorScopeStore.shared` | Destination store for captured transactions. |
| `prettyPrint` | `false` | Logs each captured transaction through `logger`. |
| `prettyPrintConfig` | `KtorScopePrettyPrintConfig()` | Controls printed headers, bodies, cURL, GraphQL, and JSON formatting. |
| `logger` | `println` | Receives pretty printed transaction text when `prettyPrint` is enabled. |

## Body Capture

KtorScope currently previews:

- `TextContent`
- `ByteArrayContent`
- response bodies readable through `bodyAsText()`

Other streaming or custom body types may be captured without a body preview. Set `captureBodies = false` when you only need metadata.

## WebSocket Frame Capture

KtorScope can inspect Ktor Client WebSocket sessions when your client also installs Ktor's `WebSockets` plugin:

```kotlin
import io.github.mahmoud.ktorscope.ktor.KtorScope
import io.github.mahmoud.ktorscope.core.NetworkProtocol
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.WebSockets

val client = HttpClient {
    install(WebSockets)
    install(KtorScope) {
        captureWebSocketFrames = true
        maxWebSocketFramePreviewSize = 64_000
    }
}
```

Captured WebSocket sessions are stored as `NetworkTransaction` records with `protocol = NetworkProtocol.WEBSOCKET`. KtorScope records the upgrade request/response and appends frame entries as they are sent or received. Text frames are shown as UTF-8 previews; binary, ping, and pong frames are shown as hex previews; close frames include close code and reason when available.

Set `captureWebSocketFrames = false` when you want HTTP inspection only.

## Redaction

Sensitive headers are redacted before transactions are added to the store:

```kotlin
install(KtorScope) {
    redactHeaders = setOf(
        "Authorization",
        "Cookie",
        "Set-Cookie",
        "X-Api-Key",
        "X-Device-Secret",
    )
}
```

Header matching is case-insensitive. Redacted values are replaced with a block marker.

## Optional Persistence

KtorScope is in-memory by default:

```kotlin
val client = HttpClient {
    install(KtorScope) {
        enabled = true
        captureBodies = true
    }
}
```

Room persistence is opt-in from the separate persistence module. Create a `KtorScopePersistence` at the platform edge, then pass it into your common code.

Android:

```kotlin
import io.github.mahmoud.ktorscope.persistence.ScopPersistenceFactory

val ktorScopePersistence = ScopPersistenceFactory(applicationContext).create(
    databaseName = "network_inspector.db",
    bodyDirectoryName = "network_inspector_bodies",
)
```

iOS:

```kotlin
import io.github.mahmoud.ktorscope.persistence.ScopPersistenceFactory

val ktorScopePersistence = ScopPersistenceFactory().create(
    databaseName = "network_inspector.db",
    bodyDirectoryName = "network_inspector_bodies",
)
```

Common Ktor setup:

```kotlin
val client = HttpClient {
    install(KtorScope) {
        enabled = true
        captureBodies = true
        historyPersistence {
            enabled = true
            maxRecords = 500
            maxBodyPreviewSize = 250_000
            largeBodyFileThreshold = 250_000
            this.persistence = ktorScopePersistence.historyPersistence
        }
    }
}
```

Room stores searchable metadata, body previews, and WebSocket frame history. The persistence module can store larger body content in platform files and expose the file path on the domain model for the details screen.

Common Compose setup:

```kotlin
KtorScopeScreen(
    persistHistory = true,
    onLoadFullBody = ktorScopePersistence.bodyFileStore::readBody,
)
```

## Pretty Printing

```kotlin
install(KtorScope) {
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
```

Pretty printing is useful during local debugging, CI diagnostics, and bug reports. Avoid logging captured bodies in production unless you have reviewed your privacy and security requirements.

## Production Usage

The library does not automatically restrict itself to debug builds. Gate it from your app code when needed:

```kotlin
install(KtorScope) {
    enabled = isDebugBuild
}
```

Use the debug flag or build configuration system that exists in your app.
