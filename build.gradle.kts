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

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}
