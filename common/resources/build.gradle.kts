import app.sedici.tasks.buildsrc.Android

plugins {
    id("com.android.library")
}

android {
    compileSdk = Android.compileSdk

    defaultConfig {
        minSdk = Android.minSdk
    }
}
