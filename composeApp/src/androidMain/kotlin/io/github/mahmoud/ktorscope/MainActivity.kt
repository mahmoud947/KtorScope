package io.github.mahmoud.ktorscope

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import io.github.mahmoud.ktorscope.persistence.KtorScopePersistence
import io.github.mahmoud.ktorscope.persistence.ScopPersistenceFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val persistence = ScopPersistenceFactory(application.applicationContext).create(
            databaseName = "network_inspector.db",
            bodyDirectoryName = "network_inspector_bodies",
        )
        setContent {
            App(persistence)
        }
    }
}
