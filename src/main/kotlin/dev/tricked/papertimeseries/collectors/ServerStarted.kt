package dev.tricked.papertimeseries.collectors

import dev.tricked.papertimeseries.PaperTimeSeries
import dev.tricked.papertimeseries.common.BaseCollector
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.lang.management.ManagementFactory
import java.time.Clock
import java.time.ZoneId

object Startup : Table() {
    val time = timestamp("time").defaultExpression(CurrentTimestamp())
    val javaVersion = varchar("java_version", 50)
    val cpuCount = integer("cpu_count")
    val totalRam = long("total_ram")
    val osName = varchar("os_name", 50)
    val kernelVersion = varchar("kernel_version", 50)
    val jvmImplementation = varchar("jvm_implementation", 50)
    val systemArch = varchar("system_arch", 50)
}

object StartupTime {
    val startup = Clock.system(ZoneId.of("UTC")).instant()
}

class ServerStartedCollector : BaseCollector<Startup>() {
    override val table = Startup

    override fun enable(plugin: PaperTimeSeries, collectionTime: Int) {
        super.enable(plugin, collectionTime)
        transaction {
            val res = table.select { table.time eq StartupTime.startup }.singleOrNull()
            // plugin was reloaded
            if(res != null) return@transaction

            val javaVersion = System.getProperty("java.version")
            val cpuCount = ManagementFactory.getOperatingSystemMXBean().availableProcessors

            val totalRam = Runtime.getRuntime().totalMemory()

            val osName = System.getProperty("os.name")
            val kernelVersion = getKernelVersion()

            val jvmImplementation = System.getProperty("java.vm.name")
            val systemArch = System.getProperty("os.arch")

            table.insert {
                it[time] =  StartupTime.startup
                it[this.javaVersion] = javaVersion
                it[this.cpuCount] = cpuCount
                it[this.totalRam] = totalRam
                it[this.osName] = osName
                it[this.kernelVersion] = kernelVersion
                it[this.jvmImplementation] = jvmImplementation
                it[this.systemArch] = systemArch
            }
        }
    }

    private fun getKernelVersion(): String {
        val osName = System.getProperty("os.name").lowercase()

        return when {
            osName.contains("nix") || osName.contains("nux") || osName.contains("mac") -> {
                executeCommand("uname -r")
            }
            osName.contains("win") -> {
                System.getProperty("os.version")
            }
            osName.contains("sunos") -> {
                executeCommand("uname -v")
            }
            else -> {
                "Unknown"
            }
        }
    }

    private fun executeCommand(command: String): String {
        val process = Runtime.getRuntime().exec(command)
        process.waitFor()
        return process.inputStream.bufferedReader().use { it.readText().trim() }
    }

}