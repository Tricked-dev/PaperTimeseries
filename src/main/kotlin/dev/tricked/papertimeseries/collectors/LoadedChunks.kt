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
//        var holders = io.papermc.paper.chunk.system.ChunkSystem.getVisibleChunkHolders(world)

//       var count= world.chunkCount
////        var status = world.loadedChunks[0].getFullStatus()
//        var entitiesLoaded = world.loadedChunks.count { it.isEntitiesLoaded }
//        var forceLoaded = world.loadedChunks.count { it.isForceLoaded }
//        println("c: $count e: $entitiesLoaded f: $forceLoaded a: ${world.loadedChunks.size}")
//        var entitiesLoaded = world.loadedChunks.count { it. }
//        var a = (Bukkit.getServer() as org.bukkit.craftbukkit.CraftServer)
        transaction {
            table.insert {
                it[value] = world.loadedChunks.size
                it[this.world] = world.name
            }
        }
    }
}