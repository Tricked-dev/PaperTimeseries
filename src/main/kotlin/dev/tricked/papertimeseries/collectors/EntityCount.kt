package dev.tricked.papertimeseries.collectors

import dev.tricked.papertimeseries.common.WorldCollector
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import org.bukkit.World
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp
import org.jetbrains.exposed.sql.transactions.transaction

typealias MapEntityTypesToCounts = Map<String, Int>

object EntityCounts : Table() {
    val time = timestamp("time").defaultExpression(CurrentTimestamp())
    val value = json<MapEntityTypesToCounts>("value", Json, MapSerializer(String.serializer(), Int.serializer()))
    val world = varchar("world", 40)

    override val primaryKey = PrimaryKey(time)
}

class EntityCountCollector : WorldCollector<EntityCounts>() {
    override val table = EntityCounts
    override fun collect(world: World) {
        val mapEntityTypesToCounts = world.entities.filter { it !is Player }.groupingBy { it.name }.eachCount()
        transaction {
            table.insert {
                it[value] = mapEntityTypesToCounts
                it[this.world] = world.name
            }
        }
    }
}