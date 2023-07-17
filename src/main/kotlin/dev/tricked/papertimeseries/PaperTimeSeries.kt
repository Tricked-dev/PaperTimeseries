package dev.tricked.papertimeseries

import dev.tricked.papertimeseries.collectors.*
import dev.tricked.papertimeseries.common.Players
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*


class PaperTimeSeries : JavaPlugin(), Listener {
    init {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    }
    //    private val tpsCollector = TpsCollector()
    private val collectors = listOf(
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

    override fun onEnable() {

        Database.connect(
            "jdbc:postgresql://localhost:5432/paper", driver = "org.postgresql.Driver",
            user = "paper", password = "time",
        )

        transaction {
            SchemaUtils.create(Players)
        }
//        transaction {
//            exec("SELECT create_hypertable('players', 'time')")
//        }

        for (collector in collectors) {
            transaction {
                SchemaUtils.create(collector.table)
            }
            try {
                transaction {
                    exec("SELECT create_hypertable('${collector.table.tableName}', 'time')" )
                }
            } catch(e: Throwable) {
                //it's fine
            }

        }

        val defaultDelay = config.getInt("time", 250)
        if(defaultDelay == 250) {
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

//        server.pluginManager.registerEvents(this, this)
//        transaction {
//            SchemaUtils.create(Tps, Players)
//        }
//        val userJoinCollector = UserJoinCollector()
//        transaction {
//            SchemaUtils.create(userJoinCollector.table)
//        }
//        userJoinCollector.enable(this, 0);

//        tpsCollector.run()
//
//
//       server
//            .scheduler
//            .scheduleSyncRepeatingTask(this, tpsCollector, 0L, TpsCollector.POLL_INTERVAL.toLong());

    }

    override fun onDisable() {
        // Plugin shutdown logic
    }
}
