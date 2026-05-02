# Core APIs

`ktorscope-core` contains the shared data model and utilities used by both the Ktor plugin and Compose UI.

## Store

```kotlin
import io.github.mahmoud.ktorscope.core.KtorScopeStore

val store = KtorScopeStore()
val transactions = store.transactions

store.clear()
```

`transactions` is a `StateFlow<List<NetworkTransaction>>`. New transactions are inserted at the beginning of the list.

## Models

```kotlin
data class NetworkTransaction(
    val id: String,
    val request: NetworkRequest,
    val response: NetworkResponse? = null,
    val error: NetworkError? = null,
    val durationMillis: Long? = null,
    val createdAtMillis: Long,
)
```

`NetworkTransaction.isFailed` returns `true` when an error was captured.

`NetworkRequest` includes method, URL, headers, optional body preview, and `bodyTruncated`.

`NetworkResponse` includes status, headers, optional body preview, and `bodyTruncated`.

`NetworkError` includes type, message, and optional stack trace.

## cURL

```kotlin
import io.github.mahmoud.ktorscope.core.toCurlCommand

val curl = transaction.toCurlCommand()
```

The cURL helper shell-quotes method, URL, headers, and body.

## Pretty Printing

```kotlin
import io.github.mahmoud.ktorscope.core.KtorScopePrettyPrintConfig
import io.github.mahmoud.ktorscope.core.prettyPrint

val text = transaction.prettyPrint(
    KtorScopePrettyPrintConfig(
        includeHeaders = true,
        includeBodies = true,
        includeCurl = true,
        includeGraphQl = true,
        prettyJson = true,
    ),
)
```

Pretty printing can be used directly or enabled through the Ktor plugin.

## Export

```kotlin
import io.github.mahmoud.ktorscope.core.exportKtorScopeLogs
import io.github.mahmoud.ktorscope.core.exportLogs

val reportFromStore = store.exportLogs()
val reportFromList = transactions.exportKtorScopeLogs()
```

The default export config includes headers, bodies, cURL commands, GraphQL details, and pretty JSON formatting.

## GraphQL

```kotlin
import io.github.mahmoud.ktorscope.core.graphQlOperation

val operation = transaction.graphQlOperation()
```

KtorScope parses common JSON GraphQL request bodies containing `query`, `operationName`, and `variables`. The parser intentionally avoids adding a JSON dependency to the core module.

## Body Preview

```kotlin
import io.github.mahmoud.ktorscope.core.toBodyPreview

val preview = body.toBodyPreview(maxBodySize = 10_000)
```

`BodyPreview.truncated` is `true` when the source body was larger than the configured limit.
