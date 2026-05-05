# KtorScope

<p align="center">
  <img src="docs/assets/ktorscope-logo.svg" alt="KtorScope logo" width="160" />
</p>

KtorScope is a Kotlin Multiplatform network inspector for Ktor Client. It captures requests, responses, failures, timings, headers, body previews, GraphQL metadata, cURL commands, and exportable logs, then exposes everything through a shared in-memory store and an optional Compose Multiplatform UI.

The project currently targets Android and iOS.

## Modules

| Module | Purpose |
| --- | --- |
| `ktorscope-core` | Shared models, store, redaction, body previews, cURL generation, GraphQL parsing, pretty printing, and log export helpers. |
| `ktorscope-ktor` | Ktor Client plugin that records network transactions into `KtorScopeStore`. |
| `ktorscope-compose` | Compose Multiplatform inspector UI, clipboard/share hooks, and transaction details screens. |
| `ktorscope-persistence` | Optional Room KMP history persistence plus platform file storage for large bodies. |

## Quick Start

Add the modules you need. Inside this repository, use project dependencies:

```kotlin
commonMain.dependencies {
    implementation(projects.ktorscopeCore)
    implementation(projects.ktorscopeKtor)
    implementation(projects.ktorscopeCompose)
    implementation(projects.ktorscopePersistence) // optional Room history
}
```

For a published Maven setup, use the same module split:

```kotlin
commonMain.dependencies {
    implementation("io.github.mahmoud947:ktorscope-core:<version>")
    implementation("io.github.mahmoud947:ktorscope-ktor:<version>")
    implementation("io.github.mahmoud947:ktorscope-compose:<version>")
    implementation("io.github.mahmoud947:ktorscope-persistence:<version>")
}
```

Install the plugin in your Ktor client:

```kotlin
import io.github.mahmoud.ktorscope.ktor.KtorScope
import io.ktor.client.HttpClient

val client = HttpClient {
    install(KtorScope) {
        enabled = true
        captureBodies = true
        maxBodySize = 250_000
        redactHeaders = setOf("Authorization", "Cookie", "Set-Cookie", "X-Api-Key")
        prettyPrint = true
        prettyPrintConfig {
            includeCurl = true
        }
        logger = { message -> println(message) }
    }
}
```

Show the inspector UI from Compose:

```kotlin
import androidx.compose.runtime.Composable
import io.github.mahmoud.ktorscope.compose.KtorScopeScreen

@Composable
fun NetworkInspectorRoute(onClose: () -> Unit) {
    KtorScopeScreen(onBackClicked = onClose)
}
```

To keep historical sessions across launches, add `ktorscope-persistence`, create a `KtorScopePersistence` on each platform with `ScopPersistenceFactory`, and pass its `historyPersistence` into the Ktor plugin:

```kotlin
install(KtorScope) {
    historyPersistence {
        enabled = true
        maxRecords = 500
        this.persistence = ktorScopePersistence.historyPersistence
    }
}
```

Then pass `persistHistory = true` and `onLoadFullBody = ktorScopePersistence.bodyFileStore::readBody` to `KtorScopeScreen`.

## What It Captures

- Request method, URL, headers, and supported text/byte-array request bodies.
- Response status, headers, supported text response bodies, and request duration.
- Failures thrown by Ktor before a response is available.
- Redacted sensitive headers before transactions are stored.
- Truncated body previews using `maxBodySize`.
- GraphQL operation type, operation name, query, and variables for common JSON GraphQL request bodies.
- cURL commands and pretty printed log reports.

## Documentation

- [Integration Guide](docs/integration.md)
- [Configuration](docs/configuration.md)
- [Compose UI](docs/compose-ui.md)
- [Core APIs](docs/core-apis.md)
- [Development](docs/development.md)
- [Brand Assets](docs/brand-assets.md)

## Build

Build all library modules:

```shell
./gradlew :ktorscope-core:build :ktorscope-ktor:build :ktorscope-compose:build :ktorscope-persistence:build
```

## Publishing

KtorScope uses the same Maven Central publishing setup as KPDF. Set `VERSION_NAME` in `gradle.properties`, configure the Maven Central and signing credentials expected by the Vanniktech Maven Publish plugin, then publish all library modules together:

```shell
./gradlew publishToMavenCentral
```

## License

KtorScope is released under the [Apache License 2.0](LICENSE).
