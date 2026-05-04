# Integration Guide

This guide shows the smallest useful KtorScope setup for a Kotlin Multiplatform app.

## 1. Add Dependencies

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

For external apps, publish the modules first, then consume the artifacts:

```kotlin
commonMain.dependencies {
    implementation("io.github.mahmoud.ktorscope:ktorscope-core:<version>")
    implementation("io.github.mahmoud.ktorscope:ktorscope-ktor:<version>")
    implementation("io.github.mahmoud.ktorscope:ktorscope-compose:<version>")
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

By default, KtorScope writes to `KtorScopeStore.shared`, captures body previews, redacts common sensitive headers, and keeps the data in memory.

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

## 4. Optional Historical Session Persistence

By default, KtorScope is in-memory only. If you want historical transactions across launches, add `ktorscope-persistence` and pass an opt-in Room adapter:

```kotlin
import io.github.mahmoud.ktorscope.persistence.createRoomKtorScopeHistoryPersistence
import io.github.mahmoud.ktorscope.persistence.rememberKtorScopeHistoryPersistence

val ktorScopePersistence = rememberKtorScopeHistoryPersistence()

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

Room dependencies stay out of `ktorscope-core`, `ktorscope-ktor`, and `ktorscope-compose`.

## 5. Render the Inspector

```kotlin
import androidx.compose.runtime.Composable
import io.github.mahmoud.ktorscope.compose.KtorScopeScreen
import io.github.mahmoud.ktorscope.core.KtorScopeStore

@Composable
fun DebugNetworkScreen(
    store: KtorScopeStore,
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

## 6. Inspect Data Without UI

```kotlin
import io.github.mahmoud.ktorscope.core.KtorScopeStore

val currentTransactions = KtorScopeStore.shared.transactions.value
val persistedTransactions = KtorScopeStore.shared.persistedTransactions.value
```

`transactions` is a `StateFlow<List<NetworkTransaction>>`, so non-Compose code can collect it directly.

## 7. Export Logs

```kotlin
import io.github.mahmoud.ktorscope.core.exportLogs

val report = KtorScopeStore.shared.exportLogs()
```

The exported report includes pretty printed requests, responses, failures, cURL commands, and GraphQL details using the default export config.
