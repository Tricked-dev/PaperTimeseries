package dev.tricked.papertimeseries.common

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime

object Players : Table() {
    val time = datetime("time").defaultExpression(CurrentDateTime).uniqueIndex()
    val lastSeen = datetime("last_seen").defaultExpression(CurrentDateTime).uniqueIndex()
    val uuid = uuid("uuid").uniqueIndex()

    // name cant be unique
    val name = varchar("name", 100)
    val online = bool("online")
    val dim = varchar("dim", 40)

    override val primaryKey = PrimaryKey(uuid, time)
}