package com.pb.messages.events

import com.pb.config.Configuration
import com.pb.messages.CommandRegistrationService
import com.pb.messages.data.CommandExecutionException
import com.pb.messages.data.SlashCommand
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.interaction.response.DeferredPublicMessageInteractionResponseBehavior
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.interaction.GuildChatInputCommandInteraction
import mu.KotlinLogging

class InteractionEventHandler(
    private val configuration: Configuration,
    private val commandRegistrationService: CommandRegistrationService,
) {
    private val logger = KotlinLogging.logger {}

    private val admins = configuration.admins.map { Snowflake(it) }

    suspend fun handleGuildChatInputCommandInteractionCreateEvent(interaction: GuildChatInputCommandInteraction) = handleErrors(interaction) { response ->
        val command = commandRegistrationService.getSlashCommandOrNull(interaction.invokedCommandName)
        if (command != null) {
            if (command.isAdminOnly) {
                executeAdminOnly(interaction, command, response)
            } else {
                command.execute(interaction, response)
            }
            return@handleErrors
        }

        response.respond {
            content = "Command '${interaction.invokedCommandName}' was not found."
        }
    }

    private suspend fun handleErrors(interaction: GuildChatInputCommandInteraction, block: suspend (response: DeferredPublicMessageInteractionResponseBehavior) -> Unit) {
        if (interaction.isFromDisabledGuild()) return
        val response = interaction.deferPublicResponse()
        try {
            block(response)
        } catch (e: CommandExecutionException) {
            response.respond { content = "Error: ${e.reason}" }
        } catch (e: Exception) {
            response.respond { content = "Unexpected error during command execution" }
            logger.error(e) { "Unexpected error during command execution" }
        }
    }

    private fun GuildChatInputCommandInteraction.isFromDisabledGuild() = guildId.value in configuration.disabledServers

    private suspend fun executeAdminOnly(interaction: GuildChatInputCommandInteraction, command: SlashCommand, response: DeferredPublicMessageInteractionResponseBehavior) {
        val authorId = interaction.data.user.value?.id
        if (authorId != null && admins.contains(authorId)) {
            command.execute(interaction, response)
        } else {
            response.respond { content = "This command can be run only by an administrator." }
        }
    }
}