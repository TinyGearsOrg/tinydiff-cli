import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.10"
    application
}

group = "org.tinygears"
version = "1.0-SNAPSHOT"

application {
    mainClass.set("org.tinygears.tinydiff.cli.Test")
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
//   implementation("net.java.dev.jna:jna:5.12.1")
    implementation("org.fusesource.jansi:jansi:2.4.0")
    implementation("org.jline:jline-terminal-jansi:3.21.0")
    implementation("info.picocli:picocli:4.6.3")
    implementation("com.github.TinyGearsOrg:tinydiff:main-SNAPSHOT")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}