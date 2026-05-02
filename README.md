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
| `sample-compose-app` | Android/iOS sample app that wires the library together. |

## Quick Start

Add the modules you need. Inside this repository, the sample uses project dependencies:

```kotlin
commonMain.dependencies {
    implementation(projects.ktorscopeCore)
    implementation(projects.ktorscopeKtor)
    implementation(projects.ktorscopeCompose)
}
```

For a published Maven setup, use the same module split once publishing metadata is added:

```kotlin
commonMain.dependencies {
    implementation("io.github.mahmoud.ktorscope:ktorscope-core:<version>")
    implementation("io.github.mahmoud.ktorscope:ktorscope-ktor:<version>")
    implementation("io.github.mahmoud.ktorscope:ktorscope-compose:<version>")
}
```

Install the plugin in your Ktor client:

```kotlin
import io.github.mahmoud.ktorscope.core.KtorScopePrettyPrintConfig
import io.github.mahmoud.ktorscope.ktor.KtorScope
import io.ktor.client.HttpClient

val client = HttpClient {
    install(KtorScope) {
        enabled = true
        captureBodies = true
        maxBodySize = 250_000
        redactHeaders = setOf("Authorization", "Cookie", "Set-Cookie", "X-Api-Key")
        prettyPrint = true
        prettyPrintConfig = KtorScopePrettyPrintConfig(includeCurl = true)
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

## Run the Sample

Build the Android sample:

```shell
./gradlew :sample-compose-app:assembleDebug
```

Build all library modules:

```shell
./gradlew :ktorscope-core:build :ktorscope-ktor:build :ktorscope-compose:build
```

Open `iosApp` in Xcode to run the iOS shell app, or use the `sample-compose-app` iOS target from your Kotlin Multiplatform IDE workflow.

## Current Status

KtorScope is source-ready in this repository. Maven publishing configuration is not present yet, so external consumers need either local project/module dependencies or a future published artifact coordinate.

## License

Add the project license before distributing the library publicly.
