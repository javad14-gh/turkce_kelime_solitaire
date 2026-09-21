pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = java.net.URI.create("https://maven.tapsell.ir") }
        maven { url = java.net.URI.create("https://repo.tapsell.ir/content/repositories/releases/") }
    }
}

rootProject.name = "TurkceKelimeSolitaire"
include(":app")
