package dev.tricked.papertimeseries.common

import dev.tricked.papertimeseries.PaperTimeSeries
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import org.jetbrains.exposed.sql.Table

abstract class TimedCollector<T : Table> : BaseCollector<T>(), Runnable {
    private var collectionTime: Int = 20
    private var collectingTask: Int? = null
    override fun enable(plugin: PaperTimeSeries, collectionTime: Int) {
        super.enable(plugin, collectionTime)
        this.collectionTime = collectionTime
        collectingTask = startCollecting(plugin)
    }

    override fun disable(plugin: PaperTimeSeries) {
        super.disable(plugin)
        if (collectingTask != null)
            Bukkit.getScheduler().cancelTask(collectingTask!!)
    }

    private fun startCollecting(plugin: Plugin): Int {
        return Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this, 0L, collectionTime.toLong())
    }
}

