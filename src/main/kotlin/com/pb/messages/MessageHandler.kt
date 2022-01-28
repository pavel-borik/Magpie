package com.pb.messages

import com.pb.PREFIX
import com.pb.config.Configuration
import com.pb.messages.data.Command
import com.pb.messages.data.CommandExecutionException
import com.pb.messages.data.ExecutionData
import com.pb.messages.data.InvalidCommandUsageException
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.MessageChannelBehavior
import dev.kord.core.entity.Guild
import dev.kord.core.entity.Message
import mu.KotlinLogging

class MessageHandler(
    private val configuration: Configuration,
    private val messageEntityProvider: MessageEntityProvider,
) {
    private val logger = KotlinLogging.logger {}

    private val admins = configuration.admins.map { Snowflake(it) }

    suspend fun handle(message: Message) = handleErrors(message.channel) {
        if (message.isFromBot() || message.isFromDisabledGuild()) return@handleErrors
        if (message.isCommandInvocation()) {
            message.getGuildOrNull()
                ?.let { guild -> runCommandWhenFound(message, guild) }
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

    private suspend fun runCommandWhenFound(message: Message, guild: Guild) {
        val commandTrigger = message.content.substringBefore(" ").removePrefix(PREFIX).lowercase()

        messageEntityProvider.commands[commandTrigger]
            ?.let { command ->
                val args = message.content.split(" ").drop(1)
                val commandExecutionData = ExecutionData(args, guild)
                if (command.isAdminOnly) {
                    executeAdminOnly(message, command, commandExecutionData)
                } else {
                    command.execute(message, commandExecutionData)
                }
            }
            ?: message.channel.createMessage("Command '$commandTrigger' was not found.")
    }

    private suspend fun executeAdminOnly(message: Message, command: Command, commandExecutionData: ExecutionData) {
        if (message.author != null && admins.contains(message.author!!.id)) {
            command.execute(message, commandExecutionData)
        } else {
            message.channel.createMessage("This command can be run only by an administrator.")
        }
    }

    private suspend fun doFilter(message: Message) {
        messageEntityProvider.filters.forEach { it.doFilter(message) }
    }

    private fun Message.isCommandInvocation() = content.startsWith(PREFIX)

    private fun Message.isFromBot() = author?.isBot ?: false

    private suspend fun Message.isFromDisabledGuild() =
        getGuildOrNull() != null && configuration.disabledServers.contains(getGuild().id.value)
}


