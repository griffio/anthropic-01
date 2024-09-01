plugins {
    kotlin("jvm") version "2.0.20"
    kotlin("plugin.serialization") version "2.0.20"
}

group = "griffio"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val ktorClientVer = "2.3.12"

dependencies {
    implementation("io.ktor:ktor-client-core:$ktorClientVer")
    implementation("io.ktor:ktor-client-java:$ktorClientVer")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorClientVer")
    implementation("io.ktor:ktor-client-logging:$ktorClientVer")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorClientVer")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1")
    implementation("ch.qos.logback:logback-classic:1.5.7")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}
