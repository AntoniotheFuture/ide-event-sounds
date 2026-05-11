pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://cache-redirector.jetbrains.com/plugins.gradle.org/m2")
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "org.jetbrains.kotlin.jvm") {
                useModule("org.jetbrains.kotlin:kotlin-gradle-plugin:${requested.version}")
            }
            if (requested.id.id == "org.jetbrains.intellij") {
                useModule("org.jetbrains.intellij.plugins:gradle-intellij-plugin:${requested.version}")
            }
        }
    }
}

rootProject.name = "ide-event-sounds"

include("packages:core")
include("packages:intellij")
