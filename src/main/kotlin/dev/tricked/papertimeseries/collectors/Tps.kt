package dev.tricked.papertimeseries.collectors

import dev.tricked.papertimeseries.PaperTimeSeries
import dev.tricked.papertimeseries.TpsMonitor
import dev.tricked.papertimeseries.common.TimedCollector
import org.bukkit.Bukkit
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp
import org.jetbrains.exposed.sql.transactions.transaction

object Tps : Table() {
    val time = timestamp("time").defaultExpression(CurrentTimestamp())
    val value = decimal("value", 5,3)
    override val primaryKey = PrimaryKey(time)
}

class TpsCollector : TimedCollector<Tps>() {
    override val table = Tps

    private var tpsMonitorTask: Int? = null

    private val tpsCollector = TpsMonitor()

   override fun enable(plugin: PaperTimeSeries, collectionTime: Int) {
        super.enable(plugin, collectionTime)
        this.tpsMonitorTask = startTask(plugin)
    }

   override fun disable(plugin: PaperTimeSeries) {
        super.disable(plugin)
        Bukkit.getScheduler().cancelTask(tpsMonitorTask!!)
    }

    private fun startTask(plugin: PaperTimeSeries): Int {
        return Bukkit.getServer()
            .scheduler
            .scheduleSyncRepeatingTask(plugin, tpsCollector, 0L, TpsMonitor.POLL_INTERVAL.toLong())
    }

   override fun run() {
       transaction {
           table.insert {
               it[value] = tpsCollector.averageTPS
           }
       }
    }
}