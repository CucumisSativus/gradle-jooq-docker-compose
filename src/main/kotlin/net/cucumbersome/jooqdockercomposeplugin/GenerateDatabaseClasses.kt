package net.cucumbersome.jooqdockercomposeplugin

import com.avast.gradle.dockercompose.DockerComposePlugin
import org.flywaydb.core.api.Location.FILESYSTEM_PREFIX
import org.gradle.api.DefaultTask
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.*
import org.gradle.api.tasks.SourceSet.MAIN_SOURCE_SET_NAME
import org.jooq.meta.jaxb.Database
import org.jooq.meta.jaxb.Generate
import org.jooq.meta.jaxb.Generator
import org.jooq.meta.jaxb.Jdbc
import org.jooq.meta.jaxb.Target
abstract class GenerateDatabaseClasses : DefaultTask() {
    @get:Input
    var databaseDockerComposeSericeName = "db"

    @get:Input
    var dbUser: String = "postgres"

    @get:Input
    var dbPassword: String = "postgres"

    @InputFiles
    @PathSensitive(PathSensitivity.RELATIVE)
    val inputDirectory = project.objects.fileCollection().from("src/main/resources/db/migration")

    @OutputDirectory
    val outputDirectory =
        project.objects.directoryProperty().convention(project.layout.buildDirectory.dir("generated-jooq"))


    init {
        project.plugins.withType(JavaPlugin::class.java) {
            project.convention.getPlugin(JavaPluginConvention::class.java).sourceSets.named(MAIN_SOURCE_SET_NAME) {
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

    private fun generateDbUrl(): String {
        project.plugins.withType(DockerComposePlugin::class.java) {
            val serviceInfo = this.dockerCompose
            val host = serviceInfo.host
            val port = serviceInfo.port

            return "jdbc:postgresql://$host:$port/postgres"
        }

    }

    private fun generate() {
        project.delete(outputDirectory)
        org.jooq.codegen.GenerationTool.generate(org.jooq.meta.jaxb.Configuration().apply {
            logging = org.jooq.meta.jaxb.Logging.INFO
            this.withJdbc(Jdbc().apply {
                driver = "org.postgresql.Driver"
                url = generateDbUrl()
                user = dbUser
                password = dbPassword
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
                    directory = outputDirectory.asFile.get().toString()  // default (can be omitted)
                })
            })
        })
    }

    private fun migrate() {
        val inputDirectory = inputDirectory
            .map { "$FILESYSTEM_PREFIX${it.absolutePath}" }.toTypedArray()
        org.flywaydb.core.Flyway.configure()
            .dataSource(generateDbUrl(), dbUser, dbPassword)
            .locations(*inputDirectory)
            .load()
            .migrate()
    }
}

//tasks.register<GenerateDatabaseClasses>("generateDatabaseClasses")