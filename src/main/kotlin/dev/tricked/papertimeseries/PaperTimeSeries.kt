package dev.tricked.papertimeseries

import dev.tricked.papertimeseries.collectors.*
import dev.tricked.papertimeseries.commands.TimeCommands
import dev.tricked.papertimeseries.common.Players
import dev.tricked.papertimeseries.tps.TickDurationCollector
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.util.*


class PaperTimeSeries : JavaPlugin(), Listener {
    init {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    }

    private val collectors by lazy {
        // It can happen in onEnable it just need to happen once
        listOf(
            TpsCollector(),
            UserPresenceCollector(),
            EntityCountCollector(),
            LoadedChunksCollector(),
            MemoryCollector(),
            ThreadsCollector(),
            MobCapCollector(),
            ServerStartedCollector(),
            CommandsCollector()
        )
    }

    private var database: Database? = null

    override fun onEnable() {

        val cmd = TimeCommands(this)
        getCommand("pts")!!.setExecutor(cmd)
        getCommand("pts")!!.tabCompleter = cmd

        val (url, user, password) = getSettings()

        if (database == null) {
            onDisable()
        }

        database = Database.connect(
            url, driver = "org.postgresql.Driver",
            user = user, password = password,
        )

        TickDurationCollector.init(this)

        transaction {
            Players.update {
                it[online] = false
            }
        }

        val createTables = getDefaultBool("create.tables", true)
        val createHyperTables = getDefaultBool("create.hypertables", true)
        if (createTables)
            transaction {
                SchemaUtils.create(Players)
            }

        for (collector in collectors) {
            if (createTables)
                transaction {
                    SchemaUtils.create(collector.table)
                }
            if (createHyperTables)
                transaction {
                    //checks if hypertable is enabled if not create one
                    exec("SELECT count(*) FROM timescaledb_information.hypertables WHERE hypertable_name = '${collector.table.tableName}'") { rs ->
                        {
                            if (rs.getInt("count") == 0) {
                                exec("SELECT create_hypertable('${collector.table.tableName}', 'time')")
                            }
                        }
                    }
                }
        }

        val defaultDelay = config.getInt("time", 250)
        if (defaultDelay == 250) {
            config.set("time", 250)
        }


        for (collector in collectors) {
            val name = collector.javaClass.simpleName
            var enabled = config.get("collectors.${name}.enabled")
            if (enabled == null) {
                config.set("collectors.${name}.enabled", true)
                enabled = true
            }
            var delay = config.getInt("collectors.${name}.time")
            if (delay == 0) {
                delay = defaultDelay
            }
            if (enabled == true) {
                collector.enable(this, delay)
            }
        }

        saveConfig()
    }


    override fun onDisable() {
        for (collector in collectors) {
            collector.disable(this)
        }
        if (database != null) TransactionManager.closeAndUnregister(database!!)
    }

    private fun getDefault(path: String, default: String): String {
        var result = config.getString(path)
        if (result == null) {
            result = default
            config.set(path, result)
        }
        return result
    }

    private fun getDefaultBool(path: String, default: Boolean): Boolean {
        var result = config.get(path)
        if (result == null) {
            result = default
            config.set(path, result)
        }
        return result as Boolean
    }

    private fun getSettings(): Triple<String, String, String> {
        val url = getDefault("database.url", "jdbc:postgresql://localhost:5432/paper")
        val user = getDefault("database.user", "postgres")
        val password = getDefault("database.password", "postgres")
        saveConfig()
        return Triple(url, user, password)
    }

}
