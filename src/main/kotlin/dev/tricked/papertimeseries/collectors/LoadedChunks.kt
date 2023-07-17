package dev.tricked.papertimeseries.collectors

import dev.tricked.papertimeseries.common.WorldCollector
import org.bukkit.World
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp
import org.jetbrains.exposed.sql.transactions.transaction

object LoadedChunks : Table() {
    val time = timestamp("time").defaultExpression(CurrentTimestamp())
    val value = integer("value")
    val world = varchar("world", 40)

    override val primaryKey = PrimaryKey(time)
}

class LoadedChunksCollector : WorldCollector<LoadedChunks>() {
    override val table = LoadedChunks
    override fun collect(world: World) {
        transaction {
            table.insert {
                it[value] = world.loadedChunks.size
                it[this.world] = world.name
            }
        }
    }
}