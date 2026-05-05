plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidx.room)
    alias(libs.plugins.composeCompiler)
    id("maven-publish")
    id("signing")
}

description = "Room-backed multiplatform history persistence for KtorScope."

kotlin {
    androidTarget()

    val xcfName = "ktorscope-persistenceKit"

    iosX64 {
        binaries.framework {
            baseName = xcfName
        }
    }

    iosArm64 {
        binaries.framework {
            baseName = xcfName
        }
    }

    iosSimulatorArm64 {
        binaries.framework {
            baseName = xcfName
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                api(projects.ktorscopeCore)
                implementation(libs.kotlin.stdlib)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.compose.runtime)
                implementation(libs.androidx.room.runtime)
                implementation(libs.androidx.sqlite.bundled)
            }
        }

        androidMain {
            dependencies {
                implementation(libs.compose.ui)
            }
        }
    }
}

android {
    namespace = "io.github.mahmoud.ktorscope_persistence"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
    }
}

dependencies {
    add("kspAndroid", libs.androidx.room.compiler)
    add("kspIosArm64", libs.androidx.room.compiler)
    add("kspIosSimulatorArm64", libs.androidx.room.compiler)
    add("kspIosX64", libs.androidx.room.compiler)
}

room {
    schemaDirectory("$projectDir/schemas")
}
