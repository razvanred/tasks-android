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
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = freeCompilerArgs + "-Xopt-in=kotlinx.coroutines.ExperimentalCoroutinesApi"
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    coreLibraryDesugaring(libs.android.tools.desugar)

    implementation(projects.data.repository)
    api(projects.base.android)

    implementation(libs.dagger.hilt.android)
    kapt(libs.dagger.hilt.compiler)

    testImplementation(libs.junit4)
    testImplementation(libs.robolectric)
    testImplementation(libs.truth)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)

    testImplementation(libs.dagger.hilt.android.testing)
    kaptTest(libs.dagger.hilt.compiler)

    testImplementation(projects.data.local.android.testing)
    testImplementation(projects.base.common.test)

    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.test.ext.junit)
    testImplementation(libs.androidx.test.rules)
    testImplementation(libs.androidx.test.runner)

    testImplementation(libs.kotlinx.coroutines.test)

    testImplementation(libs.androidx.arch.core.testing)
}
