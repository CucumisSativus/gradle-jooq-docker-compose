import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.31"
    `kotlin-dsl`
    groovy
    `maven-publish`
    `java-gradle-plugin`
    id("com.gradle.plugin-publish") version "1.1.0"
    id("com.github.ben-manes.versions").version("0.46.0")
    checkstyle

}

checkstyle {
    maxWarnings = 0
}

group = "net.cucumbersome"
version = "0.1"

gradlePlugin {

    plugins {
        create("jooqDockerComposePlugin") {
            id = "net.cucumbersome.jooq-docker-compose"
            implementationClass = "net.cucumbersome.jooqdockercomposeplugin.JooqDockerComposePlugin"
            version = project.version
            displayName = "Generate Jooq classes from docker-compose"
        }
    }
}
pluginBundle {

    website = "https://github.com/CucumisSativus/gradle-jooq-docker-compose"
    vcsUrl = "https://github.com/CucumisSativus/gradle-jooq-docker-compose.git"
    description =
        "Plugin useses docker-compose to start database, migrate it with flyway and generate jooq classes"
    tags = listOf("jooq", "docker-compose", "database", "flyway")
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))

    implementation("org.jooq:jooq-codegen:3.14.15")
    implementation("org.flywaydb:flyway-core:6.4.3")
    implementation("com.avast.gradle:gradle-docker-compose-plugin:0.16.11")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}