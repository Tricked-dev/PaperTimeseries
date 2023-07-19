package dev.tricked.papertimeseries.collectors

import dev.tricked.papertimeseries.common.TimedCollector
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp
import org.jetbrains.exposed.sql.transactions.transaction

object Threads : Table() {
    val time = timestamp("time").defaultExpression(CurrentTimestamp())
    val value = integer("value")
}

class ThreadsCollector : TimedCollector<Threads>() {
    override val table = Threads
    override fun run() {
        Thread.activeCount()
        transaction {
            table.insert {
                it[value] = Thread.activeCount()
            }
        }
    }
}