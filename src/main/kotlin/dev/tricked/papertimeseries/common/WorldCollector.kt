package dev.tricked.papertimeseries.common

import org.bukkit.Bukkit
import org.bukkit.World
import org.jetbrains.exposed.sql.Table

abstract class WorldCollector<T : Table> : TimedCollector<T>() {
    abstract fun collect(world: World)
    override fun run() {
        for (world in Bukkit.getWorlds()) {
            collect(world)
        }
    }
}