package net.cucumbersome.jooqdockercomposeplugin

import org.gradle.api.Plugin
import org.gradle.api.Project

class JooqDockerComposePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.configurations.create("jdbc")
        project.extensions.create("jooqDockerComposePlugin", JooqDockerComposePluginExtension::class.java)
        project.tasks.create("generateJooqClasses", GenerateDatabaseClassesTask::class.java) {
            group = "jooq"
        }
    }
}