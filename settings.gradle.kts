rootProject.name = "SediciTasks-Android"

include(":model")
include(":data:local:common")
include(":data:local:android:common")
include(":android-app")

enableFeaturePreview("VERSION_CATALOGS")
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
include(":common:compose")
include(":ui:tasks")
include(":common:resources")
include(":ui:create-task")
include(":data:local:android:ondevice")
include(":data:local:android:inmemory")
include(":base:common")
include(":base:android")
include(":domain")
