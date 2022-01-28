package com.pb.messages

import com.pb.messages.data.Command
import com.pb.messages.data.Filter

class MessageEntityProvider {
    val commands = mutableMapOf<String, Command>()
    val filters = mutableListOf<Filter>()

    fun registerCommands(vararg commands: Command) {
        commands.forEach { command ->
            command.triggers
                .map { it.lowercase() }
                .forEach { trigger ->
                    if (this.commands.containsKey(trigger)) throw RuntimeException("The map already contains a command with trigger '$trigger'")
                    this.commands[trigger] = command
                }
        }
    }

    fun registerFilters(vararg filters: Filter) {
        this.filters.addAll(filters)
    }
}