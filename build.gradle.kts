plugins {
    kotlin("jvm") version "1.8.21"
    java
    idea
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "com.pb"
version = "1.0"

repositories {
    mavenCentral()
}

val kotlin = "1.8.21"

val kord = "0.10.0"
val ktor = "2.3.0"
val exposed = "0.42.0"

val log4j = "2.20.0"
val slf4j = "2.0.7"

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlin")
    implementation("dev.kord:kord-core:$kord")
    implementation("org.slf4j:slf4j-api:$slf4j")
    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:$log4j")
    implementation("org.apache.logging.log4j:log4j-api:$log4j")
    implementation("org.apache.logging.log4j:log4j-core:$log4j")

    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")
    implementation("com.h2database:h2:2.1.212")
    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation("org.jetbrains.exposed:exposed-core:$exposed")
    implementation("org.jetbrains.exposed:exposed-dao:$exposed")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposed")
    implementation("com.github.ben-manes.caffeine:caffeine:3.1.1")
    implementation("io.ktor:ktor-client-core:$ktor")
    implementation("io.ktor:ktor-client-cio:$ktor")
    implementation("io.ktor:ktor-client-jackson:$ktor")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict", "-opt-in=kotlin.RequiresOptIn")
        jvmTarget = "17"
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