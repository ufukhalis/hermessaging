plugins {
    id("java")
    kotlin("jvm")
}

group = "io.github.ufukhalis.hermessaging"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":core"))
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.apache.kafka:kafka-clients:3.8.0")

    testImplementation("io.kotest:kotest-runner-junit5:5.9.1")
    testImplementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.0")
    testImplementation("org.testcontainers:kafka:1.20.1")
}

tasks.test {
    useJUnitPlatform()
}
