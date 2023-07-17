package dev.tricked.papertimeseries.common

import dev.tricked.papertimeseries.PaperTimeSeries
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime


abstract class BaseCollector<T: Table> : Listener {
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





object Players : Table() {
    val time = datetime("time").defaultExpression(CurrentDateTime).uniqueIndex()
    val uuid = uuid("uuid").uniqueIndex()
    // name cant be unique
    val name = varchar("name", 100)
    val online = bool("online")
    val dim = varchar("dim", 40)

    override val primaryKey = PrimaryKey(uuid, time)
}



