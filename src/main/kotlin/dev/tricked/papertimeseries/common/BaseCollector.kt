package dev.tricked.papertimeseries.common

import dev.tricked.papertimeseries.PaperTimeSeries
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.jetbrains.exposed.sql.Table


abstract class BaseCollector<T : Table> : Listener {
    abstract val table: T
    open fun enable(plugin: PaperTimeSeries, collectionTime: Int) {
        registerSelf(plugin)
    }

    open fun disable(plugin: PaperTimeSeries) {
        unregisterSelf()
    }

    fun registerSelf(plugin: PaperTimeSeries) {
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    fun unregisterSelf() {
        HandlerList.unregisterAll(this)
    }
}





