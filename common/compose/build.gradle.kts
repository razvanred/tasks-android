import app.sedici.tasks.buildsrc.Android

plugins {
    id("com.android.library")
    kotlin("android")
}

android {
    compileSdk = Android.compileSdk

    defaultConfig {
        minSdk = Android.minSdk
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.get()
    }
}

dependencies {
    implementation(libs.compose.ui.ui)
    implementation(libs.compose.foundation.foundation)
    implementation(libs.compose.material.material)
    implementation(libs.compose.foundation.layout)
    implementation(libs.compose.animation.animation)
    implementation(libs.compose.ui.tooling)

    api(libs.androidx.lifecycle.runtime.ktx)
}
