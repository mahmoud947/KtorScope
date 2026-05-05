import com.vanniktech.maven.publish.MavenPublishBaseExtension
import org.gradle.kotlin.dsl.configure

val publishedModules = setOf(
    ":ktorscope-core",
    ":ktorscope-ktor",
    ":ktorscope-compose",
    ":ktorscope-persistence",
)

plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.androidx.room) apply false
    alias(libs.plugins.androidKotlinMultiplatformLibrary) apply false
    alias(libs.plugins.androidLint) apply false
    id("com.vanniktech.maven.publish") version "0.35.0" apply false
}

group = providers.gradleProperty("GROUP").orNull ?: "io.github.mahmoud947"
version = providers.gradleProperty("VERSION_NAME").orNull ?: "0.1.0-SNAPSHOT"

subprojects {
    group = rootProject.group
    version = rootProject.version

    if (path in publishedModules) {
        pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
            apply(plugin = "com.vanniktech.maven.publish")
        }
    }

    plugins.withId("com.vanniktech.maven.publish") {
        extensions.configure<MavenPublishBaseExtension> {
            publishToMavenCentral()
            signAllPublications()

            configureBasedOnAppliedPlugins(
                javadocJar = true,
                sourcesJar = true,
            )

            pom {
                name.set(
                    providers.gradleProperty("POM_NAME")
                        .orElse(project.name)
                        .map { rootName ->
                            when (project.path) {
                                ":ktorscope-core" -> "$rootName Core"
                                ":ktorscope-ktor" -> "$rootName Ktor"
                                ":ktorscope-compose" -> "$rootName Compose"
                                ":ktorscope-persistence" -> "$rootName Persistence"
                                else -> rootName
                            }
                        }
                )
                description.set(
                    providers.gradleProperty("POM_DESCRIPTION")
                        .orElse(project.description ?: project.name)
                )
                url.set(
                    providers.gradleProperty("POM_URL")
                        .orElse("https://github.com/mahmoud947/KtorScope")
                )

                scm {
                    connection.set(
                        providers.gradleProperty("POM_SCM_CONNECTION")
                            .orElse("scm:git:git://github.com/mahmoud947/KtorScope.git")
                    )
                    developerConnection.set(
                        providers.gradleProperty("POM_SCM_DEV_CONNECTION")
                            .orElse("scm:git:ssh://git@github.com/mahmoud947/KtorScope.git")
                    )
                    url.set(
                        providers.gradleProperty("POM_SCM_URL")
                            .orElse("https://github.com/mahmoud947/KtorScope")
                    )
                }
            }
        }
    }
}
