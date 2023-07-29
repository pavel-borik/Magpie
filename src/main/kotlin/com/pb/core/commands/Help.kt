package com.pb.core.commands

import com.pb.core.CommandRegistrationService
import com.pb.core.data.ChatCommand
import com.pb.core.data.ExecutionData
import com.pb.core.data.SlashCommand
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.behavior.interaction.response.DeferredPublicMessageInteractionResponseBehavior
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.Message
import dev.kord.core.entity.interaction.GuildChatInputCommandInteraction
import dev.kord.rest.builder.interaction.GlobalChatInputCreateBuilder
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.modify.embed

class Help(private val commandRegistrationService: CommandRegistrationService) : ChatCommand, SlashCommand {
    override val trigger = "help"
    override val isAdminOnly = false
    override val help = "!help"
    override val description = "List help for all commands"

    override suspend fun execute(message: Message, executionData: ExecutionData) {
        message.channel.createEmbed { createEmbed() }
    }

    override suspend fun execute(interaction: GuildChatInputCommandInteraction, response: DeferredPublicMessageInteractionResponseBehavior) {
        response.respond {
            embed { createEmbed() }
        }
    }

    override fun buildInputCommand(builder: GlobalChatInputCreateBuilder) {}

    private fun EmbedBuilder.createEmbed() {
        title = "Commands"
        description = commandRegistrationService.getChatCommands().map { it.help }.distinct().joinToString("\n")
    }
}