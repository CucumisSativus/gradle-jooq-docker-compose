package net.cucumbersome.jooqdockercomposeplugin

import org.gradle.api.provider.Property

interface JooqDockerComposePluginExtension {
    val databaseDockerComposeSericeName: Property<String>
    val dbUser: Property<String>
    val dbPassword: Property<String>

}