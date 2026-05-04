package io.github.mahmoud.ktorscope

import androidx.compose.ui.window.ComposeUIViewController
import io.github.mahmoud.ktorscope.persistence.ScopPersistenceFactory

fun MainViewController() = ComposeUIViewController {
    val persistence = ScopPersistenceFactory().create(
        databaseName = "network_inspector.db",
        bodyDirectoryName = "network_inspector_bodies",
    )

    App(persistence)
}