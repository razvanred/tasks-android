plugins {
    id("com.diffplug.spotless") version "5.14.2"
    id("com.github.ben-manes.versions") version "0.39.0"
}

allprojects {
    apply(plugin = "com.diffplug.spotless")

    spotless {
        kotlin {
            target("**/*.kt")
            ktlint(libs.versions.ktlint.get())
        }

        kotlinGradle {
            // same as kotlin, but for .gradle.kts files (defaults to '*.gradle.kts')
            target("**/*.gradle.kts")
            ktlint(libs.versions.ktlint.get())
        }
    }
}
