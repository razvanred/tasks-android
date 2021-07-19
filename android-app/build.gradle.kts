plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

val appVersionCode = (rootProject.properties["sedici.tasks.versioncode"] as? String)?.toInt() ?: 1

android {
    compileSdk = 30

    defaultConfig {
        applicationId = "app.sedici.largebutton.showcase"
        minSdk = 21
        targetSdk = 30
        versionCode = appVersionCode
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
    implementation(libs.androidx.coreKtx)
    implementation(libs.androidx.appcompat)
    implementation(libs.google.android.material.material)
    implementation(libs.androidx.activity.compose)

    implementation(libs.compose.material.material)
    implementation(libs.compose.ui.ui)
    implementation(libs.compose.ui.tooling)
}
