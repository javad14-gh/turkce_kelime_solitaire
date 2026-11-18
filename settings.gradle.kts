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
        maven { url = uri("https://maven.tapsell.ir") }
        google()
        mavenCentral()
    }
}

rootProject.name = "TurkceKelimeSolitaire"
include(":app")
