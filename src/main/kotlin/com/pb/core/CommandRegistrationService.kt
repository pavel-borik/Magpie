package com.pb.core

import com.pb.core.data.ChatCommand
import com.pb.core.data.Command
import com.pb.core.data.SlashCommand
import dev.kord.core.Kord
import kotlinx.coroutines.flow.toList
import mu.KotlinLogging

class CommandRegistrationService(private val client: Kord) {
    private val logger = KotlinLogging.logger {}

    private val chatCommands = mutableMapOf<String, ChatCommand>()
    private val slashCommands = mutableMapOf<String, SlashCommand>()

    suspend fun registerCommands(vararg commands: Command) {
        doRegisterCommands(commands.filterIsInstance<ChatCommand>(), chatCommands)
        doRegisterCommands(commands.filterIsInstance<SlashCommand>(), slashCommands)
        updateApplicationCommands()
    }

    private fun <T : Command> doRegisterCommands(commands: List<T>, destination: MutableMap<String, T>) {
        commands
            .filter { it.trigger.isNotBlank() }
            .forEach { command ->
                val trigger = command.trigger.lowercase()
                if (destination.containsKey(trigger)) throw RuntimeException("The map already contains a command with trigger '$trigger'")
                destination[trigger] = command
            }
    }

    private suspend fun updateApplicationCommands() {
        val globalApplicationCommands = client.getGlobalApplicationCommands().toList()

        val registeredCommandNames = globalApplicationCommands.map { it.name }.toSet()
        logger.debug { "Found Global Chat Input commands: $registeredCommandNames" }

        val slashCommandNames = slashCommands.values.map { it.trigger }.toSet() // do not use keys, they are lowercase
        logger.debug { "Found Slash commands: $slashCommandNames" }

        val commandsToRegister = slashCommandNames.minus(registeredCommandNames)
        val commandsToDelete = registeredCommandNames.minus(slashCommandNames)

        slashCommands.values
            .filter { it.trigger in commandsToRegister }
            .forEach {
                logger.info { "Creating Global Chat Input command ${it.trigger}" }
                client.createGlobalChatInputCommand(it.trigger, it.description) {
                    it.buildInputCommand(this)
                }
            }

        globalApplicationCommands
            .filter { it.name in commandsToDelete }
            .forEach {
                logger.info { "Deleting Global Chat Input command ${it.name}" }
                it.delete()
            }
    }

    fun getChatCommandOrNull(name: String): ChatCommand? = chatCommands[name.lowercase()]

    fun getChatCommands(): List<ChatCommand> = chatCommands.values.toList()

    fun getSlashCommandOrNull(name: String): SlashCommand? = slashCommands[name.lowercase()]
}