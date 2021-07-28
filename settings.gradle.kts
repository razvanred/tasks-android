pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
    plugins {
        id("com.android.application") version "7.1.0-alpha05"
        id("com.android.library") version "7.1.0-alpha05"
        id("org.jetbrains.kotlin.android") version "1.5.10"
        id("org.jetbrains.kotlin.jvm") version "1.5.10"
        id("org.jetbrains.kotlin.kapt") version "1.5.10"
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

enableFeaturePreview("VERSION_CATALOGS")
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "SediciTasks-Android"
include(":android-app")
include(":model")
include(":data:local:common")
include(":data:local:android:common")
