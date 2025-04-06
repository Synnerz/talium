import gg.essential.gradle.util.noRunConfigs

plugins {
    id("java")
    kotlin("jvm")
    id("gg.essential.multi-version")
    id("gg.essential.defaults")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

loom {
    noRunConfigs()
}

java {
    withSourcesJar()
}

repositories {
    mavenCentral()
}

val shadowImpl: Configuration by configurations.creating {
    configurations.implementation.get().extendsFrom(this)
}

dependencies {
    shadowImpl(kotlin("stdlib-jdk8"))
}

tasks {
    jar {
        archiveBaseName.set("talium")
        dependsOn(shadowJar)
    }
    shadowJar {
        archiveClassifier.set("all")
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        configurations = listOf(shadowImpl)
        mergeServiceFiles()
    }
}
