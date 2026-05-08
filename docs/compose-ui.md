# Compose UI

`ktorscope-compose` provides `KtorScopeScreen`, a Compose Multiplatform inspector for captured network transactions.

## Basic Screen

```kotlin
import androidx.compose.runtime.Composable
import io.github.mahmoud.ktorscope.compose.KtorScopeScreen

@Composable
fun InspectorScreen(onClose: () -> Unit) {
    KtorScopeScreen(onBackClicked = onClose)
}
```

The screen reads from `KtorScopeStore.shared` by default.

## Custom Store

```kotlin
import androidx.compose.runtime.Composable
import io.github.mahmoud.ktorscope.compose.KtorScopeScreen
import io.github.mahmoud.ktorscope.core.KtorScopeStore

@Composable
fun InspectorScreen(
    store: KtorScopeStore,
    onClose: () -> Unit,
) {
    KtorScopeScreen(
        store = store,
        onBackClicked = onClose,
    )
}
```

Use the same store in your Ktor plugin configuration:

```kotlin
val store = KtorScopeStore()

val client = HttpClient {
    install(KtorScope) {
        store = store
    }
}
```

## Theme Mode

```kotlin
import io.github.mahmoud.ktorscope.compose.KtorScopeThemeMode

KtorScopeScreen(
    themeMode = KtorScopeThemeMode.System,
    onThemeModeChange = { selectedMode ->
        // Persist the selected mode if your app wants to remember it.
    },
)
```

Supported modes are `System`, `Light`, and `Dark`.

## Copy and Share

`KtorScopeScreen` provides platform copy/share handlers by default. You can override them for tests, analytics, or custom app behavior:

```kotlin
KtorScopeScreen(
    onCopy = { text -> clipboardManager.setText(text) },
    onShare = { text -> shareNetworkReport(text) },
)
```

The UI can share all filtered logs from the list screen or a single transaction from the details screen. WebSocket transaction exports include captured frame summaries and payload previews.

## WebSocket Frames

When a transaction was captured from a Ktor WebSocket session, the details screen adds a `Frames` tab. The tab shows frame order, direction, type, payload size, close metadata, and a text or hex payload preview. The summary card also shows the current frame count so live sessions are easy to spot.

## Layout

The inspector adapts to available width:

- Wide layouts show a transaction list and details panel side by side.
- Narrow layouts show the list first, then navigate into details.

The list supports query filtering, status filtering, theme switching, clearing the store, and sharing the current filtered report.
