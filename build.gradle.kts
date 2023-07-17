/*
 * Copyright (c) Tricked-dev 2023.
 */

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java")
    id("xyz.jpenilla.run-paper") version "2.1.0"
    id("org.jetbrains.kotlin.jvm") version "1.9.0"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("wrapper")
}

object Versions {
    const val kotlin = "1.9.0"
    const val coroutines = "1.6.2"
    const val serialization = "1.3.3"
    const val atomicfu = "0.17.3"
    const val datetime = "0.3.2"
    const val exposed = "88315512d3"
    const val hikari = "5.0.1"
}

//val output = ByteArrayOutputStream()
//project.exec {
//    commandLine("git", "describe", "--tags", "--abbrev=0")
//    standardOutput = output
//}

group = "dev.tricked"
//version = output.toString().trim()
version = "0.1.0"

repositories {
    mavenCentral()
    maven("papermc-repo") {
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven("sonatype") {
        url = uri("https://oss.sonatype.org/content/groups/public/")
    }
    maven("jitpack") {
        url = uri("https://jitpack.io")
    }
}


tasks {
    withType<JavaCompile> {
        if (JavaVersion.current() < JavaVersion.VERSION_17) {
            options.compilerArgs = listOf("-Xlint:-processing")
        }
    }

    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    create<Jar>("sourcesJar") {
        archiveClassifier.set("sources")
        from(sourceSets["main"].allSource)
    }


    withType<Copy>().named("processResources") {
        val props = mapOf("version" to version)
        inputs.properties(props)
        filteringCharset = "UTF-8"
        filesMatching("paper-plugin.yml") {
            expand(props)
        }
    }
}

tasks.wrapper {
    gradleVersion = "8.2.1"
}

tasks.shadowJar {
//    from(".") {
//        include("LICENSE")
//        into("")
//    }
    manifest {
        attributes(
            "Implementation-Title" to project.name,
            "Implementation-Version" to project.version,
//            "Main-Class" to "dev.tricked.hardermc.HarderMCKt"
        )
    }
}

artifacts {
    archives(tasks.getByName("sourcesJar"))
    archives(tasks.shadowJar)
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.1-R0.1-SNAPSHOT")

    api("org.jetbrains.kotlin:kotlin-stdlib:${Versions.kotlin}")
    api("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Versions.kotlin}")
    api("org.jetbrains.kotlin:kotlin-stdlib-jdk7:${Versions.kotlin}")
    api("org.jetbrains.kotlin:kotlin-reflect:${Versions.kotlin}")

    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:${Versions.coroutines}")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:${Versions.coroutines}")
    api("org.jetbrains.kotlinx:kotlinx-serialization-core-jvm:${Versions.serialization}")
    api("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:${Versions.serialization}")
    api("org.jetbrains.kotlinx:kotlinx-serialization-cbor-jvm:${Versions.serialization}")
    api("org.jetbrains.kotlinx:atomicfu-jvm:${Versions.atomicfu}")
    api("org.jetbrains.kotlinx:kotlinx-datetime-jvm:${Versions.datetime}")

    api("com.github.JetBrains.Exposed:exposed-core:${Versions.exposed}")
    api("com.github.JetBrains.Exposed:exposed-dao:${Versions.exposed}")
    api("com.github.JetBrains.Exposed:exposed-jdbc:${Versions.exposed}")
    api("com.github.JetBrains.Exposed:exposed-java-time:${Versions.exposed}")

    api("com.zaxxer:HikariCP:${Versions.hikari}")
    api("org.postgresql:postgresql:42.2.2")
}


val targetJavaVersion = 17

java {
    val javaVersion = JavaVersion.toVersion(targetJavaVersion)
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
    if (JavaVersion.current() < javaVersion) {
        toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
    }
}

sourceSets {
    getByName("main") {
        kotlin.srcDirs("src/main/kotlin")
    }
}

tasks.runServer {
    minecraftVersion("1.20.1")
}