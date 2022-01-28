plugins {
    kotlin("jvm") version "1.6.0"
    java
    idea
    id("com.github.johnrengelman.shadow") version "7.1.0"
}

group = "com.pb"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.6.0")
    implementation("dev.kord:kord-core:0.8.0-M8")
    implementation("org.slf4j:slf4j-api:1.7.35")
    implementation("org.slf4j:slf4j-log4j12:1.7.35")
    implementation("io.github.microutils:kotlin-logging:2.1.21")
    implementation("com.h2database:h2:2.1.210")
    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation("org.jetbrains.exposed:exposed-core:0.37.3")
    implementation("org.jetbrains.exposed:exposed-dao:0.37.3")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.37.3")
    implementation("io.ktor:ktor-client-core:1.6.7")
    implementation("io.ktor:ktor-client-cio:1.6.7")
    implementation("io.ktor:ktor-client-jackson:1.6.7")
    implementation("io.ktor:ktor-jackson:1.6.7")
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