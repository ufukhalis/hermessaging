plugins {
    kotlin("jvm")
}

group = "io.github.ufukhalis.hermessaging"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":core"))

    implementation("com.rabbitmq:amqp-client:5.21.0")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}
