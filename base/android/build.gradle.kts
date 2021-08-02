import app.sedici.tasks.buildsrc.Android

plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
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
}

dependencies {
    api(projects.base.common)
    api(projects.model)

    implementation(libs.dagger.hilt.android)
    kapt(libs.dagger.hilt.compiler)
}
