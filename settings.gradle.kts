rootProject.name = "DocReader"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

include(":core:domain")
include(":core:ports")
include(":core:application")
include(":core:ooxml")
include(":core:markdown")
include(":core:pdf")
