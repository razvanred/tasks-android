import app.sedici.tasks.buildsrc.Android

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("dagger.hilt.android.plugin")
}

val appVersionCode = (rootProject.properties["sedici.tasks.versioncode"] as? String)?.toInt() ?: 1
println("APK version code: $appVersionCode")

android {
    compileSdk = Android.compileSdk

    defaultConfig {
        applicationId = "app.sedici.tasks"
        minSdk = Android.minSdk
        targetSdk = Android.targetSdk
        versionCode = appVersionCode
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
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
    implementation(projects.base.android)
    implementation(projects.data.local.android.common) // TODO remove

    implementation(projects.common.compose)
    implementation(projects.common.resources)

    implementation(projects.ui.tasks)
    implementation(projects.ui.stats)

    implementation(libs.androidx.coreKtx)
    implementation(libs.androidx.appcompat)
    implementation(libs.google.android.material.material)
    implementation(libs.androidx.activity.compose)

    implementation(libs.compose.material.material)
    implementation(libs.compose.ui.ui)
    implementation(libs.compose.ui.tooling)
    implementation(libs.compose.material.iconsext)

    implementation(libs.androidx.navigation.compose)

    implementation(libs.dagger.hilt.android)
    implementation(libs.androidx.hilt.navigationCompose)
    kapt(libs.dagger.hilt.compiler)

    debugImplementation(projects.data.local.android.inmemory) // TODO remove
    releaseImplementation(projects.data.local.android.ondevice) // TODO remove
}
