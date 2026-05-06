plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
}

tasks.register("bootRun") {
    group = "application"
    description = "Builds and installs the debug Android app on a connected device or emulator."
    dependsOn(":app:installDebug")
}
