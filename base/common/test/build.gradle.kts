plugins {
    id("kotlin")
}

dependencies {
    api(projects.base.common)

    api(libs.kotlinx.coroutines.test)

    implementation(libs.junit4)
}
