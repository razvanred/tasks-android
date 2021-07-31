rootProject.name = "SediciTasks-Android"

include(":model")
include(":data:local:common")
include(":data:local:android:common")
include(":android-app")

enableFeaturePreview("VERSION_CATALOGS")
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
include(":common:compose")
