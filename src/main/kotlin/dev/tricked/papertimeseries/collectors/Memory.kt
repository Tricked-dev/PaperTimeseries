package dev.tricked.papertimeseries.collectors

import dev.tricked.papertimeseries.common.TimedCollector
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp

object Memory : Table() {
    val time = timestamp("time").defaultExpression(CurrentTimestamp())
    val max = long("max")
    val free = long("free")
    val allocated = long("allocated")

    override val primaryKey = PrimaryKey(time)
}

class MemoryCollector:TimedCollector<Memory>() {
    override val table = Memory
    override fun run() {
        transaction {
            table.insert {
                it[max] = Runtime.getRuntime().maxMemory()
                it[free] = Runtime.getRuntime().freeMemory()
                it[allocated] = Runtime.getRuntime().totalMemory()
            }
        }
    }
}