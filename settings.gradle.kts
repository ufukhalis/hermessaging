plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "hermessaging"
include("core")
include("kafka")
include("rabbitmq")
