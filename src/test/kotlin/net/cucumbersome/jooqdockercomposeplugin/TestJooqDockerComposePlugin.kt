package net.cucumbersome.jooqdockercomposeplugin

import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class TestJooqDockerComposePlugin {
    @Test
    fun `apply plugin`() {
        val project: Project = ProjectBuilder.builder().build()
        project.getPluginManager().apply("net.cucumbersome.jooq-docker-compose")

        assertTrue(project.getTasks().getByName("generateJooqClasses") is GenerateDatabaseClassesTask)
    }

    @Test
    fun `apply plugin with configuration`() {
        val project: Project = ProjectBuilder.builder().build()
        project.getPluginManager().apply("net.cucumbersome.jooq-docker-compose")
        project.extensions.configure<JooqDockerComposePluginExtension> {
            this.databaseDockerComposeSericeName.set("db")
            this.dbUser.set("user")
            this.dbPassword.set("password")
            this.dbMigrationLocation.set("src/test/resources/db/migration")
            this.generatedClassesPath.set(project.layout.buildDirectory.dir("generated-jooq"))
        }

        assertTrue(project.getTasks().getByName("generateJooqClasses") is GenerateDatabaseClassesTask)
    }

}