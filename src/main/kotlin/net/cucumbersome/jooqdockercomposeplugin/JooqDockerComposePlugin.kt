package net.cucumbersome.jooqdockercomposeplugin

import org.gradle.api.Plugin
import org.gradle.api.Project

class JooqDockerComposePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.configurations.create("jdbc")
        project.tasks.create("generateJooqClasses", GenerateDatabaseClasses::class.java) {
            group = "jooq"
        }
    }
}