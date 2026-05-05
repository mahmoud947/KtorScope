# Development

## Project Layout

```text
ktorscope-core/       Shared models, store, redaction, formatting, export helpers
ktorscope-ktor/       Ktor Client plugin
ktorscope-compose/    Compose Multiplatform inspector UI
ktorscope-persistence/ Optional Room KMP history persistence and body file storage
composeApp/           Android/iOS sample app using the library modules
iosApp/               iOS app shell
```

## Common Commands

Build the library modules:

```shell
./gradlew :ktorscope-core:build :ktorscope-ktor:build :ktorscope-compose:build :ktorscope-persistence:build
```

Build the sample Android app:

```shell
./gradlew :composeApp:assembleDebug
```

Run checks for a single module:

```shell
./gradlew :ktorscope-core:check
```

## API Boundaries

- Keep platform-independent models and utilities in `ktorscope-core`.
- Keep Ktor-specific interception logic in `ktorscope-ktor`.
- Keep UI, clipboard, and share behavior in `ktorscope-compose`.
- Use `KtorScopeStore` as the handoff between capture and presentation.

## Publishing Notes

The repository publishes the four library modules with the same Maven Central flow used by KPDF:

1. Set `VERSION_NAME` in `gradle.properties`.
2. Configure Maven Central and signing credentials for the Vanniktech Maven Publish plugin.
3. Publish `ktorscope-core`, `ktorscope-ktor`, `ktorscope-compose`, and `ktorscope-persistence` together.
4. Update README dependency snippets with the released version.

## Documentation Checklist

When changing public APIs, update:

- `README.md`
- `docs/integration.md`
- `docs/configuration.md`
- `docs/compose-ui.md`
- `docs/core-apis.md`
