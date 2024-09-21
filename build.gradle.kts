plugins {
    kotlin("jvm") version "2.0.0"
}

group = "io.github.ufukhalis.hermessaging"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":core"))
    implementation(project(":rabbitmq"))
    implementation(project(":kafka"))

    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.0")
    implementation("org.apache.kafka:kafka-clients:3.8.0")
    implementation("com.rabbitmq:amqp-client:5.21.0")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}
