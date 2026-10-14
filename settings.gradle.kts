rootProject.name = "DocReader"

pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

include(":core:domain")
include(":core:ports")
include(":core:application")
include(":core:ooxml")
include(":core:markdown")
include(":core:pdf")
include(":core:data")
include(":app")
