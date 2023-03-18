package net.cucumbersome.jooqdockercomposeplugin

import com.avast.gradle.dockercompose.ComposeExtension
import org.flywaydb.core.api.Location.FILESYSTEM_PREFIX
import org.gradle.api.DefaultTask
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.*
import org.gradle.api.tasks.SourceSet.MAIN_SOURCE_SET_NAME
import org.gradle.kotlin.dsl.get
import org.jooq.meta.jaxb.*
import org.jooq.meta.jaxb.Target

abstract class GenerateDatabaseClassesTask : DefaultTask() {
    @Internal
    fun getDbUser() = getExtension().dbUser.get()

    @Internal
    fun getDbPassword() = getExtension().dbPassword.get()

    @Internal
    fun getDbMigrationLocation() = getExtension().dbMigrationLocation.getOrElse("src/main/resources/db/migration")

    @Internal
    fun getGeneratedClassesPath() = getExtension().generatedClassesPath.getOrElse(
        project.layout.buildDirectory.dir("generated-jooq")
    )

    @InputFiles
    @PathSensitive(PathSensitivity.RELATIVE)
    val inputDirectory = project.objects.fileCollection().from(getDbMigrationLocation())

    @OutputDirectory
    val outputDirectory =
        project.objects.directoryProperty().convention(getGeneratedClassesPath())


    init {
        project.plugins.withType(JavaPlugin::class.java) {
            val javaPluginExtension = project.extensions.findByType(JavaPluginExtension::class.java)
                ?: throw IllegalStateException("Cannot find java plugin extension")
            javaPluginExtension.sourceSets.named(MAIN_SOURCE_SET_NAME) {
                java {
                    srcDir(outputDirectory)
                }
            }
        }
    }

    @TaskAction
    fun execute() {
        migrate()
        generate()
    }

    private fun getExtension(): JooqDockerComposePluginExtension {
        return project.extensions.getByName("jooqDockerComposePlugin") as JooqDockerComposePluginExtension
    }

    private fun generateDbUrl(): String {
        val extension = project.extensions["dockerCompose"]
        if (extension !is ComposeExtension) {
            throw IllegalStateException("Cannot find docker compose extension")
        }
        val databaseDockerComposeSericeName = getExtension().databaseDockerComposeSericeName.get()
        val dataBaseUrl = extension.servicesInfos[databaseDockerComposeSericeName]?.let {
            val host = it.host
            val port = it.port

            "jdbc:postgresql://$host:$port/postgres"
        }
        return dataBaseUrl ?: throw IllegalStateException("Cannot find database url")


    }

    private fun generate() {
        project.delete(outputDirectory)
        org.jooq.codegen.GenerationTool.generate(Configuration().apply {
            logging = Logging.INFO
            this.withJdbc(Jdbc().apply {
                driver = "org.postgresql.Driver"
                url = generateDbUrl()
                user = getDbUser()
                password = getDbPassword()
            })
            this.withGenerator(Generator().apply {
                name = "org.jooq.codegen.KotlinGenerator"
                this.withDatabase(Database().apply {
                    name = "org.jooq.meta.postgres.PostgresDatabase"
                    inputSchema = "public"
                    excludes = "flyway_schema_history"
                    schemaVersionProvider = "SELECT MAX(version) FROM flyway_schema_history"
                })
                this.withGenerate(Generate().apply {
                    isDeprecated = false
                    isRecords = false
                    isImmutablePojos = true
                    isFluentSetters = true
                    isDaos = true
                })
                this.withTarget(Target().apply {
                    packageName = "com.threatray.binaryauthenticator.dbmodel"
                    directory = outputDirectory.asFile.get().toString()
                })
            })
        })
    }

    private fun migrate() {
        val inputDirectory = inputDirectory
            .map { "$FILESYSTEM_PREFIX${it.absolutePath}" }.toTypedArray()
        org.flywaydb.core.Flyway.configure()
            .dataSource(generateDbUrl(), getDbUser(), getDbPassword())
            .locations(*inputDirectory)
            .load()
            .migrate()
    }
}
