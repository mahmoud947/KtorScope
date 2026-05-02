# Configuration

KtorScope is configured from the Ktor Client plugin block:

```kotlin
install(KtorScope) {
    enabled = true
    captureBodies = true
    maxBodySize = 250_000
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
| `maxBodySize` | `250_000` | Maximum captured body preview length. Longer bodies are truncated. |
| `redactHeaders` | `Authorization`, `Cookie`, `Set-Cookie`, `X-Api-Key` | Case-insensitive header names to redact before storage. |
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

## Pretty Printing

```kotlin
import io.github.mahmoud.ktorscope.core.KtorScopePrettyPrintConfig

install(KtorScope) {
    prettyPrint = true
    prettyPrintConfig = KtorScopePrettyPrintConfig(
        includeHeaders = true,
        includeBodies = true,
        includeCurl = true,
        includeGraphQl = true,
        prettyJson = true,
    )
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
