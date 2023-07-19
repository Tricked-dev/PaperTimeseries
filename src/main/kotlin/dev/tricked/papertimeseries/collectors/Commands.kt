package dev.tricked.papertimeseries.collectors

import dev.tricked.papertimeseries.common.BaseCollector
import dev.tricked.papertimeseries.common.Players
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp
import org.jetbrains.exposed.sql.transactions.transaction


object Commands : Table() {
    val time = timestamp("time").defaultExpression(CurrentTimestamp())
    val value = text("value")
    val args = text("args")
    val world = varchar("world", 40)
    val player = (uuid("player") references Players.uuid)
    override val primaryKey = PrimaryKey(time)
}

class CommandsCollector : BaseCollector<Commands>() {
    override val table = Commands

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onServerCommand(event: PlayerCommandPreprocessEvent) {
        val commandMessage = event.message

        val commandParts = commandMessage.split(" ".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()

        val commandName = commandParts[0].substring(1)

        var res = ""
        for (i in 1 until commandParts.size) {
            res += commandParts[i] + " "
        }

        transaction {
            table.insert {
                it[value] = commandName
                it[args] = res.trim()
                it[player] = event.player.uniqueId
                it[world] = event.player.world.name
            }
        }
    }
}