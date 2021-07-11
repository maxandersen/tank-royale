import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.Files

defaultTasks("clean", "build")

val clean = tasks.register<Delete>("clean") {
    delete(project.buildDir)
}

tasks.register("build") {
    dependsOn(clean)
    dependsOn(zipSampleBots)
}

abstract class BaseTask : DefaultTask() {

    @Internal
    protected val archiveDir: Path = project.buildDir.toPath().resolve("archive")

    @Internal
    protected val libsDir: Path = archiveDir.resolve("libs")

    protected fun createDir(path: Path) {
        if (!Files.exists(path)) {
            Files.createDirectory(path)
        }
    }

    protected fun deleteDir(path: Path) {
        if (Files.exists(path)) {
            Files.walk(path)
                .sorted(Comparator.reverseOrder())
                .map(({ obj: Path -> obj.toFile() }))
                .forEach(({ obj: File -> obj.delete() }))
        }
    }
}

abstract class CreateDirs : BaseTask() {
    @TaskAction
    fun build() {
        createDir(project.buildDir.toPath())
        createDir(archiveDir)
        createDir(libsDir)
    }
}

val createDirs = task<CreateDirs>("createDirs") {}

val copyBotApiJar = task<Copy>("copyBotApiJar") {
    dependsOn(":bot-api:java:jar")
    dependsOn(createDirs)

    from(project(":bot-api:java").file("build/libs"))
    into(project.buildDir.resolve("archive/libs"))
    include("java-*.jar")
    exclude("*javadoc*")
    rename("^.*(\\d\\.\\d\\.\\d)\\.jar", "robocode-tankroyale-bot-api-$1.jar")
}

abstract class FindBotApiJarFilename : BaseTask() {
    @TaskAction
    fun build() {
        project.extra["botApiJarFilename"] =
            Files.list(libsDir).filter { path ->
                path.fileName.toString().startsWith("robocode-tankroyale-bot-api")
            }.findFirst().get().fileName.toString()
    }
}

val findBotApiJarFilename = task<FindBotApiJarFilename>("findBotApiJarFilename") {
    dependsOn(copyBotApiJar)
}

abstract class CopyBotFiles : BaseTask() {
    @TaskAction
    fun prepareBotFiles() {
        Files.list(project.projectDir.toPath()).forEach { projectDir ->
            run {
                if (Files.isDirectory(projectDir) && isBotProjectDir(projectDir)) {
                    copyBotJar(projectDir)
                    copyBotJsonFile(projectDir)
                    createCmdFile(projectDir)
                    createShFile(projectDir)
                }
            }
        }
    }

    private fun isBotProjectDir(dir: Path): Boolean {
        val filename = dir.fileName.toString()
        return !filename.startsWith(".") && filename != "build"
    }

    private fun copyBotJar(projectDir: Path) {
        val jarFilename = getBotJarPath(projectDir)
        Files.copy(jarFilename, libsDir.resolve(jarFilename.fileName))
    }

    private fun getBotJarPath(projectDir: Path): Path {
        val archiveDir: Path = projectDir.resolve("build/libs")
        for (dir in Files.list(archiveDir)) {
            if (dir.startsWith(projectDir)) {
                return archiveDir.resolve(dir)
            }
        }
        throw IllegalStateException("Could not find jar archive in dir: $projectDir")
    }

    private fun copyBotJsonFile(projectDir: Path) {
        val filename = projectDir.fileName.toString() + ".json"
        val jsonFilePath = projectDir.resolve("src/main/resources/$filename")
        Files.copy(jsonFilePath, archiveDir.resolve(filename))
    }

    private fun createCmdFile(projectDir: Path) {
        val filename = projectDir.fileName.toString() + ".cmd"
        val printWriter = object : java.io.PrintWriter(archiveDir.resolve(filename).toFile()) {
            override fun println() {
                write("\r\n") // Windows Carriage Return + New-line
            }
        }

        printWriter.use {
            val jarFilename = getBotJarPath(projectDir).fileName
            val className = "dev.robocode.tankroyale.sample.bots." + projectDir.fileName.toString()
            it.println("java -cp libs/$jarFilename;libs/${project.extra["botApiJarFilename"]} $className")
            it.close()
        }
    }

    private fun createShFile(projectDir: Path) {
        val filename = projectDir.fileName.toString() + ".sh"
        val printWriter = object : java.io.PrintWriter(archiveDir.resolve(filename).toFile()) {
            override fun println() {
                write("\n") // Unix New-line
            }
        }
        printWriter.use {
            it.println("#!/bin/sh")
            val jarFilename = getBotJarPath(projectDir).fileName
            val className = "dev.robocode.tankroyale.sample.bots." + projectDir.fileName.toString()
            it.println("java -cp libs/$jarFilename:libs/${project.extra["botApiJarFilename"]} $className")
            it.close()
        }
    }
}

val copyBotFiles = task<CopyBotFiles>("copyBotFiles") {
    dependsOn(findBotApiJarFilename)

    dependsOn(":sample-bots:java:Corners:build")
    dependsOn(":sample-bots:java:Crazy:build")
    dependsOn(":sample-bots:java:Fire:build")
    dependsOn(":sample-bots:java:MyFirstBot:build")
    dependsOn(":sample-bots:java:RamFire:build")
    dependsOn(":sample-bots:java:SpinBot:build")
    dependsOn(":sample-bots:java:Target:build")
    dependsOn(":sample-bots:java:TrackFire:build")
    dependsOn(":sample-bots:java:Walls:build")
}

abstract class CopyRobocodeIcon : BaseTask() {
    @TaskAction
    fun copyIcon() {
        Files.copy(project.projectDir.toPath().resolve("../../gfx/Tank/Tank.ico"), archiveDir.resolve("robocode.ico"))
    }
}

val copyRobocodeIcon = tasks.register<CopyRobocodeIcon>("copyRobocodeIcon") {}

val zipSampleBots = task<Zip>("zipSampleBots") {
    dependsOn(copyBotFiles)
    dependsOn(copyRobocodeIcon)

    archiveFileName.set("robocode-tankoyale-sample-bots.zip")
    destinationDirectory.set(Paths.get(System.getProperty("user.dir")).resolve("build").toFile())

    from(Paths.get(System.getProperty("user.dir")).resolve("build/archive").toFile())
}
