package net.cucumbersome.jooqdockercomposeplugin

import org.gradle.api.file.Directory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider

interface JooqDockerComposePluginExtension {
    val databaseDockerComposeSericeName: Property<String>
    val dbUser: Property<String>
    val dbPassword: Property<String>

    val dbMigrationLocation: Property<String>
    val generatedClassesPath: Property<Provider<Directory>>
}