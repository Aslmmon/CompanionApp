import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm")
    alias(libs.plugins.kotlinSerialization)
    application
}

kotlin {
    jvmToolchain(11)
}

application {
    mainClass.set("com.aslmmovic.qurancompanion.generator.MainKt")
}

tasks.named<JavaExec>("run") {
    workingDir = rootProject.projectDir
}

dependencies {
    implementation(libs.apache.pdfbox)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlinx.coroutines.test)
}
