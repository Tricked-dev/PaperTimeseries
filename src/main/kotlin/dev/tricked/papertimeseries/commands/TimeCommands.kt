package dev.tricked.papertimeseries.commands

import dev.tricked.papertimeseries.PaperTimeSeries
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter


class TimeCommands(private val plugin: PaperTimeSeries) : CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        if (args?.size == 0) {
            return false
        }
        if (args!![0].equals("enable", ignoreCase = true)) {
            plugin.onEnable()
            sender.sendMessage("Plugin enabled.")
        } else if (args[0].equals("disable", ignoreCase = true)) {
            plugin.onDisable()
            sender.sendMessage("Plugin disabled.")
        } else if (args[0].equals("reload", ignoreCase = true)) {
            plugin.onDisable()
            plugin.onEnable()
            sender.sendMessage("Plugin reloaded.")
        } else {
            sender.sendMessage("Invalid command. Usage: /pts <enable|disable|reload>")
            return false
        }
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>?
    ): MutableList<String> {
        val completions: MutableList<String> = ArrayList()

        @Suppress("DEPRECATED_IDENTITY_EQUALS")
        if (args != null) if (args.size === 1) {
            // Add subcommand options
            completions.add("enable")
            completions.add("disable")
            completions.add("reload")
        }

        return completions
    }
}

