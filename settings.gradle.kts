pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
// SỬA DÒNG NÀY: Dùng dấu nháy kép và hàm uri()
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "MyApp"
include(":app")
 