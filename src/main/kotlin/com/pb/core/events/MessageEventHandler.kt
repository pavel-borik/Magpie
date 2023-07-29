package com.pb.core.events

import com.pb.PREFIX
import com.pb.config.Configuration
import com.pb.core.CommandRegistrationService
import com.pb.core.FilterRegistrationService
import com.pb.core.data.ChatCommand
import com.pb.core.data.CommandExecutionException
import com.pb.core.data.ExecutionData
import com.pb.core.data.InvalidCommandUsageException
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.MessageChannelBehavior
import dev.kord.core.entity.Guild
import dev.kord.core.entity.Message
import mu.KotlinLogging

class MessageEventHandler(
    private val configuration: Configuration,
    private val commandRegistrationService: CommandRegistrationService,
    filterRegistrationService: FilterRegistrationService,
) {
    private val logger = KotlinLogging.logger {}

    private val admins = configuration.admins.map { Snowflake(it) }
    private val filters = filterRegistrationService.getFilters()

    suspend fun handleMessageCreateEvent(message: Message) = handleErrors(message.channel) {
        if (message.isFromBot() || message.isFromDisabledGuild()) return@handleErrors
        if (message.isCommandInvocation()) {
            message.getGuildOrNull()
                ?.let { guild -> runChatCommand(message, guild) }
                ?: logger.warn { "Command message '${message.content}' has been run outside of valid guild" }
            return@handleErrors
        }
        doFilter(message)
    }

    private suspend fun handleErrors(channel: MessageChannelBehavior, block: suspend () -> Unit) {
        try {
            block()
        } catch (e: InvalidCommandUsageException) {
            channel.createMessage("Invalid command usage: ${e.reason} Expected usage: ${e.help}")
        } catch (e: CommandExecutionException) {
            channel.createMessage("Error: ${e.reason}")
        } catch (e: Exception) {
            logger.error(e) { "Unexpected error during command execution" }
        }
    }

    private suspend fun runChatCommand(message: Message, guild: Guild) {
        val commandTrigger = message.content.substringBefore(" ").removePrefix(PREFIX).lowercase()

        val command = commandRegistrationService.getChatCommandOrNull(commandTrigger)
        if (command != null) {
            doRunChatCommand(message, guild, command)
            return
        }

        message.channel.createMessage("Command '$commandTrigger' was not found.")
    }

    private suspend fun doRunChatCommand(message: Message, guild: Guild, command: ChatCommand) {
        val args = message.content.split(" ").drop(1)
        val commandExecutionData = ExecutionData(args, guild)
        if (command.isAdminOnly) {
            executeAdminOnly(message, command, commandExecutionData)
        } else {
            command.execute(message, commandExecutionData)
        }
    }

    private suspend fun executeAdminOnly(message: Message, command: ChatCommand, commandExecutionData: ExecutionData) {
        if (message.author != null && admins.contains(message.author!!.id)) {
            command.execute(message, commandExecutionData)
        } else {
            message.channel.createMessage("This command can be run only by an administrator.")
        }
    }

    private fun Message.isCommandInvocation() = content.startsWith(PREFIX)

    private fun Message.isFromBot() = author?.isBot ?: false

    private suspend fun Message.isFromDisabledGuild() = getGuildOrNull()?.let { it.id.value in configuration.disabledServers } ?: false

    private suspend fun doFilter(message: Message) = filters.forEach { it.doFilter(message) }
}