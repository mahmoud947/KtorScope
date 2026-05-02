# Development

## Project Layout

```text
ktorscope-core/       Shared models, store, redaction, formatting, export helpers
ktorscope-ktor/       Ktor Client plugin
ktorscope-compose/    Compose Multiplatform inspector UI
sample-compose-app/   Android/iOS sample app using the library modules
composeApp/           Generated starter app module
iosApp/               iOS app shell
```

## Common Commands

Build the library modules:

```shell
./gradlew :ktorscope-core:build :ktorscope-ktor:build :ktorscope-compose:build
```

Build the sample Android app:

```shell
./gradlew :sample-compose-app:assembleDebug
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

The repository does not currently include Maven publishing configuration. Before publishing:

1. Add group and version metadata.
2. Configure `maven-publish` for each library module.
3. Add POM metadata, license, developers, SCM, and signing as needed.
4. Publish `ktorscope-core`, `ktorscope-ktor`, and `ktorscope-compose` together.
5. Update README dependency snippets with the released version.

## Documentation Checklist

When changing public APIs, update:

- `README.md`
- `docs/integration.md`
- `docs/configuration.md`
- `docs/compose-ui.md`
- `docs/core-apis.md`
