package dev.tricked.papertimeseries.tps

import dev.tricked.papertimeseries.PaperTimeSeries
import org.bukkit.Bukkit
import org.bukkit.Server
import java.lang.reflect.Method
import java.util.logging.Level

object TickDurationCollector {
    /**
     * Returns either the internal minecraft long array for tick times in ns,
     * or a long array containing just one element of value -1 if reflection
     * was unable to locate the minecraft tick times buffer
     */
    /**
     * If reflection is successful, this will hold a reference directly to the
     * MinecraftServer internal tick duration tracker
     */
    var tickDurations: LongArray? = null
        private set

    fun init(plugin: PaperTimeSeries) {
        /*
      * If there is not yet a handle to the internal tick duration buffer, try
      * to acquire one using reflection.
      *
      * This searches for any long[] array in the MinecraftServer class. It should
      * work across many versions of Spigot/Paper and various obfuscation mappings
      */
        if (tickDurations == null) {
            var longestArray: LongArray? = null
            try {
                /* Get the actual minecraft server class */
                val server: Server = Bukkit.getServer()
                val getServerMethod: Method = server.javaClass.getMethod("getServer")
                val minecraftServer: Any = getServerMethod.invoke(server)

                /* Look for the only array of longs in that class, which is tick duration */
                for (field in minecraftServer.javaClass.superclass.declaredFields) {
                    if (field.type.isArray && field.type.componentType.equals(Long::class.javaPrimitiveType)) {
                        /* Check all the long[] items in this class, and remember the one with the most elements */
                        val array = field[minecraftServer] as LongArray
                        if (longestArray == null || array.size > longestArray.size) {
                            longestArray = array
                        }
                    }
                }
            } catch (e: Exception) {
                plugin.logger.log(Level.FINE, "Caught exception looking for tick times array: ", e)
            }
            if (longestArray != null) {
                tickDurations = longestArray
            } else {
                /* No array was found, use a placeholder */
                tickDurations = LongArray(1)
                tickDurations!![0] = -1
                plugin.logger.log(
                    Level.WARNING,
                    "Failed to find tick times buffer via reflection. Tick duration metrics will not be available."
                )
            }
        }
    }
}
