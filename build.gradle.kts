import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.21"
        `kotlin-dsl`
        groovy
        `maven-publish`
        id("java-gradle-plugin")
        id("com.gradle.plugin-publish") version "1.1.0"
        id("com.github.ben-manes.versions").version("0.46.0")
//    id("com.avast.gradle.docker-compose") version "0.16.3"

}

group = "net.cucumbersome"
version = "0.1-SNAPSHOT"

gradlePlugin {
    plugins.create("jooqDockerPlugin") {
        id = "net.cucumbersome.jooq-docker-compose"
        implementationClass = "net.cucumbersome.jooqdockercomposeplugin.JooqDockerComposePlugin"
        version = project.version
    }
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))

    implementation("org.jooq:jooq-codegen:3.14.15")
    implementation("org.flywaydb:flyway-core:6.4.3")
    implementation("com.avast.gradle:gradle-docker-compose-plugin:0.16.11")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}