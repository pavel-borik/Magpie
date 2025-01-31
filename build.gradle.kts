import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version "2.0.21"
    java
    idea
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.pb"
version = "1.0"

repositories {
    mavenCentral()
}

val kotlin = "2.0.21"

val kord = "0.15.0"
val ktor = "3.0.0"
val exposed = "0.58.0"
val h2 = "2.1.212"
val hikari = "6.2.1"

val log4j = "2.24.3"
val slf4j = "2.0.16"
val kotlinLogging = "7.0.3"

val caffeine = "3.2.0"

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlin")
    implementation("dev.kord:kord-core:$kord")
    implementation("org.slf4j:slf4j-api:$slf4j")
    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:$log4j")
    implementation("org.apache.logging.log4j:log4j-api:$log4j")
    implementation("org.apache.logging.log4j:log4j-core:$log4j")

    implementation("io.github.oshai:kotlin-logging-jvm:$kotlinLogging")
    implementation("com.h2database:h2:$h2")
    implementation("com.zaxxer:HikariCP:$hikari")
    implementation("org.jetbrains.exposed:exposed-core:$exposed")
    implementation("org.jetbrains.exposed:exposed-dao:$exposed")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposed")
    implementation("com.github.ben-manes.caffeine:caffeine:$caffeine")
    implementation("io.ktor:ktor-client-core:$ktor")
    implementation("io.ktor:ktor-client-cio:$ktor")
    implementation("io.ktor:ktor-client-jackson:$ktor")
}

kotlin {
    compilerOptions {
        compilerOptions.freeCompilerArgs.add("-Xjsr305=strict")
        optIn.add("kotlin.RequiresOptIn")
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "com.pb.MagpieKt"
    }
    configurations["compileClasspath"].forEach { file: File ->
        from(zipTree(file.absoluteFile))
    }
}