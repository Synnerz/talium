pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://repo.essential.gg/repository/maven-public")
        maven("https://maven.architectury.dev")
        maven("https://maven.fabricmc.net")
        maven("https://maven.minecraftforge.net")
    }

    plugins {
        kotlin("jvm") version "2.0.0"
        val egtVersion = "0.6.7"
        id("gg.essential.multi-version.root") version egtVersion
    }
}

listOf(
    "1.8.9-forge-1",
    "1.8.9-forge-2"
).forEach { version ->
    include(":$version")
    project(":$version").apply {
        projectDir = file("versions/$version")
        buildFileName = "../../build.gradle.kts"
    }
}

rootProject.buildFileName = "root.gradle.kts"