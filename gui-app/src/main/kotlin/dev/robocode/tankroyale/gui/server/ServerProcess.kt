package dev.robocode.tankroyale.gui.server

import dev.robocode.tankroyale.gui.settings.ConfigSettings
import dev.robocode.tankroyale.gui.settings.ServerSettings
import dev.robocode.tankroyale.gui.ui.server.ServerActions
import dev.robocode.tankroyale.gui.ui.server.ServerEvents
import dev.robocode.tankroyale.gui.ui.server.ServerLogFrame
import dev.robocode.tankroyale.gui.util.ResourceUtil
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.InputStreamReader
import java.io.PrintStream
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.atomic.AtomicBoolean

object ServerProcess {

    private const val JAR_FILE_NAME = "robocode-tankroyale-server"

    private val isRunning = AtomicBoolean(false)
    private var process: Process? = null
    private var logThread: Thread? = null
    private val logThreadRunning = AtomicBoolean(false)

    var port: Int = ServerSettings.serverPort
        private set

    init {
        ServerActions
    }

    fun isRunning(): Boolean = isRunning.get()

    fun start(port: Int = ServerSettings.serverPort) {
        if (isRunning.get()) return

        this.port = port

        var command: MutableList<String>
        ServerSettings.apply {
            command = mutableListOf(
                "java",
                "-jar",
                getServerJar(),
                "--port=$port",
                "--games=classic,melee,1v1",
                "--tps=${ConfigSettings.tps}",
                "--controllerSecrets=${controllerSecrets.joinToString(",")}",
                "--botSecrets=${botSecrets.joinToString(",")}"
            )
            if (initialPositionsEnabled) {
                command += "--enable-initial-position"
            }
        }
        ProcessBuilder(command).apply {
            redirectErrorStream(true)
            process = start()
        }
        isRunning.set(true)

        startLogThread()

        ServerEvents.onStarted.fire(Unit)
    }

    fun stop() {
        if (!isRunning.get()) return

        stopLogThread()

        process?.apply {
            if (isAlive) {
                PrintStream(outputStream).apply {
                    println("q")
                    flush()
                }
            }
            waitFor()
            isRunning.set(false)
        }
        process = null
        logThread = null

        ServerEvents.onStopped.fire(Unit)
    }

    fun reboot() {
        stop()
        start(port)
    }

    private fun getServerJar(): String {
        System.getProperty("serverJar")?.let {
            Paths.get(it).apply {
                if (Files.exists(this)) {
                    throw FileNotFoundException(toString())
                }
                return toString()
            }
        }
        Paths.get("").apply {
            Files.list(this).filter { it.startsWith(JAR_FILE_NAME) && it.endsWith(".jar") }.findFirst().apply {
                if (isPresent) {
                    return get().toString()
                }
            }
        }
        return try {
            ResourceUtil.getResourceFile("${JAR_FILE_NAME}.jar")?.absolutePath ?: ""
        } catch (ex: Exception) {
            System.err.println(ex.message)
            ""
        }
    }

    private fun startLogThread() {
        logThread = Thread {
            logThreadRunning.set(true)

            BufferedReader(InputStreamReader(process?.inputStream!!)).use {
                while (logThreadRunning.get()) {
                    try {
                        it.lines().forEach() { line ->
                            ServerLogFrame.append(line + "\n")
                        }
                    } catch (e: InterruptedException) {
                        logThreadRunning.set(false)
                    }
                }
            }
        }.apply { start() }
    }

    private fun stopLogThread() {
        logThreadRunning.set(false)
        logThread?.interrupt()
    }
}

fun main() {
    ServerProcess.start()
    println("Server started")
    System.`in`.read()
    ServerProcess.stop()
    println("Server stopped ")
}