package com.pb.core.commands

import com.pb.core.CommandRegistrationService
import com.pb.core.data.ChatCommand
import com.pb.core.data.ExecutionData
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.entity.Message
import dev.kord.rest.builder.message.EmbedBuilder

class Help(private val commandRegistrationService: CommandRegistrationService) : ChatCommand {
    override val trigger = "help"
    override val isAdminOnly = false
    override val help = "!help"
    override val description = "List help for all commands"

    override suspend fun execute(message: Message, executionData: ExecutionData) {
        message.channel.createEmbed { createEmbed() }
    }

    private fun EmbedBuilder.createEmbed() {
        title = "Commands"
        description = commandRegistrationService.getChatCommands().map { it.help }.distinct().joinToString("\n")
    }
}