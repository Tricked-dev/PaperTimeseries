package dev.tricked.papertimeseries.collectors

import dev.tricked.papertimeseries.common.BaseCollector
import dev.tricked.papertimeseries.common.Players
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

object UserPresence : Table() {
    val time = timestamp("time").defaultExpression(CurrentTimestamp())
    val online = bool("online")
    val player = (uuid("uuid") references Players.uuid)
    val world = varchar("world", 40)

    override val primaryKey = PrimaryKey(time)
}

class UserPresenceCollector : BaseCollector<UserPresence>() {
    override val table = UserPresence

    private fun update(player: Player, online: Boolean) {
        transaction {
            val pp = Players.select { Players.uuid eq player.uniqueId }.firstOrNull()
            if (pp != null) {
                Players.update(where = { Players.uuid eq player.uniqueId }) {
                    it[this.online] = online
                    it[dim] = player.world.name
                    it[name] = player.name
                    it[lastSeen] = CurrentTimestamp()

                }
            } else {
                Players.insert {
                    it[uuid] = player.uniqueId
                    it[name] = player.name
                    it[dim] = player.world.name
                    it[this.online] = online
                    it[lastSeen] = CurrentTimestamp()
                }
            }

        }
        transaction {
//            Players.upsert(where = { Players.uuid eq player.uniqueId }) {
//                it[uuid] = player.uniqueId
//                it[name] = player.name
//                it[dim] = player.world.name
//                it[Players.online] = online
//                it[time] = CurrentTimestamp()
//            }

            table.insert {
                it[UserPresence.online] = online
                it[this.player] = player.uniqueId
                it[world] = player.world.name
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerJoin(event: PlayerJoinEvent) {
        update(event.player, true)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerLeave(event: PlayerQuitEvent) {
        update(event.player, false)
    }
}