# Integration Guide

This guide shows the smallest useful KtorScope setup for a Kotlin Multiplatform app.

Current version: `1.1.0`

## 1. Add Dependencies

For external apps, consume the published artifacts:

```kotlin
commonMain.dependencies {
    implementation("io.github.mahmoud947:ktorscope-core:1.1.0")
    implementation("io.github.mahmoud947:ktorscope-ktor:1.1.0")
    implementation("io.github.mahmoud947:ktorscope-compose:1.1.0")
    implementation("io.github.mahmoud947:ktorscope-persistence:1.1.0")
}
```

If you use a Gradle version catalog, add the modules to `gradle/libs.versions.toml`:

```toml
[versions]
ktorscope = "1.1.0"

[libraries]
ktorscope-core = { module = "io.github.mahmoud947:ktorscope-core", version.ref = "ktorscope" }
ktorscope-ktor = { module = "io.github.mahmoud947:ktorscope-ktor", version.ref = "ktorscope" }
ktorscope-compose = { module = "io.github.mahmoud947:ktorscope-compose", version.ref = "ktorscope" }
ktorscope-persistence = { module = "io.github.mahmoud947:ktorscope-persistence", version.ref = "ktorscope" }
```

Then use the aliases in your dependencies:

```kotlin
commonMain.dependencies {
    implementation(libs.ktorscope.core)
    implementation(libs.ktorscope.ktor)
    implementation(libs.ktorscope.compose)
    implementation(libs.ktorscope.persistence)
}
```

Inside this repository:

```kotlin
commonMain.dependencies {
    implementation(projects.ktorscopeCore)
    implementation(projects.ktorscopeKtor)
}
```

Add the Compose UI module only when you want the built-in inspector screen:

```kotlin
commonMain.dependencies {
    implementation(projects.ktorscopeCompose)
}
```

Add the persistence module only when you want Room-backed history and platform file storage for large bodies:

```kotlin
commonMain.dependencies {
    implementation(projects.ktorscopePersistence)
}
```

## 2. Install the Ktor Plugin

```kotlin
import io.github.mahmoud.ktorscope.ktor.KtorScope
import io.ktor.client.HttpClient

val client = HttpClient {
    install(KtorScope)
}
```

By default, KtorScope writes to `KtorScopeStore.shared`, captures body previews and WebSocket frames, redacts common sensitive headers, and keeps the data in memory.

## 3. Configure Capture Behavior

```kotlin
import io.github.mahmoud.ktorscope.core.KtorScopeStore
import io.github.mahmoud.ktorscope.ktor.KtorScope
import io.ktor.client.HttpClient

val inspectorStore = KtorScopeStore()

val client = HttpClient {
    install(KtorScope) {
        enabled = true
        captureBodies = true
        captureWebSocketFrames = true
        maxBodySize = 128_000
        redactHeaders = setOf(
            "Authorization",
            "Cookie",
            "Set-Cookie",
            "X-Api-Key",
            "X-Session-Token",
        )
        store = inspectorStore
    }
}
```

Use a custom store when you want isolated clients, tests, or multiple inspector sessions.

## 4. Optional WebSocket Frame Inspection

If your app uses Ktor WebSockets, install Ktor's `WebSockets` plugin alongside KtorScope:

```kotlin
import io.github.mahmoud.ktorscope.ktor.KtorScope
import io.github.mahmoud.ktorscope.core.NetworkProtocol
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.wss
import io.ktor.websocket.Frame

val client = HttpClient {
    install(WebSockets)
    install(KtorScope) {
        captureWebSocketFrames = true
        maxWebSocketFramePreviewSize = 64_000
    }
}

suspend fun sendSampleFrame() {
    client.wss("wss://ws.postman-echo.com/raw") {
        send(Frame.Text("""{"type":"hello"}"""))
    }
}
```

Each WebSocket handshake is captured as a transaction with `protocol = NetworkProtocol.WEBSOCKET`. The details screen shows a `Frames` tab with sent and received text, binary, ping, pong, and close frames.

## 5. Optional Historical Session Persistence

By default, KtorScope is in-memory only. If you want historical transactions across launches, add `ktorscope-persistence`, create `KtorScopePersistence` on each platform, and pass the history adapter into the plugin.

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

Room dependencies stay out of `ktorscope-core`, `ktorscope-ktor`, and `ktorscope-compose`. `ScopPersistenceFactory` returns both the history adapter and the body file store needed by the UI.

## 6. Render the Inspector

```kotlin
import androidx.compose.runtime.Composable
import io.github.mahmoud.ktorscope.compose.KtorScopeScreen
import io.github.mahmoud.ktorscope.core.KtorScopeStore
import io.github.mahmoud.ktorscope.persistence.KtorScopePersistence

@Composable
fun DebugNetworkScreen(
    store: KtorScopeStore,
    ktorScopePersistence: KtorScopePersistence,
    onClose: () -> Unit,
) {
    KtorScopeScreen(
        store = store,
        persistHistory = true,
        onLoadFullBody = ktorScopePersistence.bodyFileStore::readBody,
        onBackClicked = onClose,
    )
}
```

If you use the default `KtorScopeStore.shared`, you can omit the `store` parameter. Pass `persistHistory = true` to show the current-session, persisted-history, and all-history filters.

## 7. Inspect Data Without UI

```kotlin
import io.github.mahmoud.ktorscope.core.KtorScopeStore

val currentTransactions = KtorScopeStore.shared.transactions.value
val persistedTransactions = KtorScopeStore.shared.persistedTransactions.value
```

`transactions` is a `StateFlow<List<NetworkTransaction>>`, so non-Compose code can collect it directly.

## 8. Export Logs

```kotlin
import io.github.mahmoud.ktorscope.core.exportLogs

val report = KtorScopeStore.shared.exportLogs()
```

The exported report includes pretty printed requests, responses, failures, WebSocket frames, cURL commands, and GraphQL details using the default export config.
