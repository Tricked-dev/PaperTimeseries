package dev.tricked.papertimeseries.tps

import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*


class TpsMonitor : Runnable {
    private val tpsQueue = LinkedList<Float>()
    private var lastPoll: Long

    init {
        lastPoll = System.currentTimeMillis()
    }

    override fun run() {
        val now: Long = System.currentTimeMillis()
        val timeSpent = now - lastPoll
        if (timeSpent <= 0) {
            // This would be caused by an invalid poll interval, skip it
            return
        }
        val tps = POLL_INTERVAL / timeSpent.toFloat() * 1000
        log(if (tps > TICKS_PER_SECOND) TICKS_PER_SECOND else tps)
        lastPoll = now
    }

    private fun log(tps: Number) {
        tpsQueue.add(tps.toFloat())
        if (tpsQueue.size > TPS_QUEUE_SIZE) {
            tpsQueue.poll()
        }
    }

    val averageTPS: BigDecimal
        get() {
            if (tpsQueue.isEmpty()) {
                return BigDecimal(20)
            }
            val sum = tpsQueue.sum()
            val size = tpsQueue.size


            val average = BigDecimal(sum.toString()).divide(BigDecimal(size.toString()), 2, RoundingMode.HALF_UP)
            val roundedResult = average.setScale(2, RoundingMode.HALF_UP)
            return roundedResult
        }

    companion object {
        /**
         * Max amount of ticks that should happen per second
         */
        const val TICKS_PER_SECOND = 20

        /**
         * Every 40 ticks (2s ideally) the server will be polled
         */
        const val POLL_INTERVAL = 40

        /**
         * The amount of TPS values to keep for calculating the average
         */
        const val TPS_QUEUE_SIZE = 10
    }
}