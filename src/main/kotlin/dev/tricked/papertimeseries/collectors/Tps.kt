package dev.tricked.papertimeseries.collectors

import dev.tricked.papertimeseries.PaperTimeSeries
import dev.tricked.papertimeseries.common.TimedCollector
import dev.tricked.papertimeseries.tps.TickDurationCollector.tickDurations
import dev.tricked.papertimeseries.tps.TpsMonitor
import org.bukkit.Bukkit
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

object Tps : Table() {
    val time = timestamp("time").defaultExpression(CurrentTimestamp())
    val value = decimal("value", 5, 3)
    val nanoAVG = long("nano_avg")
    val nanoMAX = long("nano_max")
    val nanoMIN = long("nano_min")
    val nanoMedian = long("nano_median")
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
        if (tpsMonitorTask != null)
            Bukkit.getScheduler().cancelTask(tpsMonitorTask!!)
    }

    private fun startTask(plugin: PaperTimeSeries): Int {
        return Bukkit.getServer()
            .scheduler
            .scheduleSyncRepeatingTask(plugin, tpsCollector, 0L, TpsMonitor.POLL_INTERVAL.toLong())
    }

    private fun getTickDurationMedian(): Long {
        /* Copy the original array - don't want to sort it! */
        val tickTimes: LongArray = tickDurations!!.clone()
        Arrays.sort(tickTimes)
        return tickTimes[tickTimes.size / 2]
    }

    private fun getTickDurationMax(): Long {
        var max = Long.MIN_VALUE
        for (c in tickDurations!!) {
            if (c > max) {
                max = c
            }
        }
        return max
    }

    private fun getTickDurationMin(): Long {
        var min = Long.MAX_VALUE
        for (c in tickDurations!!) {
            if (c < min) {
                min = c
            }
        }
        return min
    }

    private fun getTickDurationAverage(): Long {
        var sum: Long = 0
        val durations: LongArray = tickDurations!!
        for (c in durations) {
            sum += c
        }
        return sum / durations.size
    }


    override fun run() {
        transaction {
            table.insert {
                it[value] = tpsCollector.averageTPS
                it[nanoAVG] = getTickDurationAverage()
                it[nanoMAX] = getTickDurationMax()
                it[nanoMIN] = getTickDurationMin()
                it[nanoMedian] = getTickDurationMedian()
            }
        }
    }
}