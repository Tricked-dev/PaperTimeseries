package dev.tricked.papertimeseries.collectors

import dev.tricked.papertimeseries.common.WorldCollector
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.entity.SpawnCategory
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp

object MobCap : Table() {
    val time = timestamp("time").defaultExpression(CurrentTimestamp())
    val value = integer("value")
    val world = varchar("world", 40)
    var type = varchar("type", 40)
}

class MobCapCollector : WorldCollector<MobCap>() {
    override val table = MobCap
    override fun collect(world: World) {
//        world.
//        Bukkit.getServer().mobGoals
//        CraftWorld
//        val level: ServerLevel = world.hand()

        val server = Bukkit.getServer()
        for (cap in SpawnCategory.entries) {
            if (cap == SpawnCategory.MISC) continue
//            val limit = server.spawn(cap)

//            val count =server.getEn
//            println("${world.name}:${cap} = ${limit}")
        }
    }
}