import app.sedici.tasks.buildsrc.Android

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    compileSdk = Android.compileSdk

    defaultConfig {
        minSdk = Android.minSdk
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    packagingOptions {
        resources.excludes.addAll(
            listOf(
                "META-INF/AL2.0",
                "META-INF/LGPL2.1",
            )
        )
    }
}

dependencies {
    coreLibraryDesugaring(libs.android.tools.desugar)

    api(projects.base.android)
    api(projects.base.common.test)

    api(libs.dagger.hilt.android.testing)

    api(libs.kotlinx.coroutines.test)

    implementation(libs.androidx.test.runner)
}
