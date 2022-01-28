package com.pb.messages.commands

import com.pb.messages.MessageEntityProvider
import com.pb.messages.data.Command
import com.pb.messages.data.ExecutionData
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.entity.Message

class Help(private val messageEntityProvider: MessageEntityProvider) : Command {
    override val triggers = listOf("help")
    override val isAdminOnly = false
    override val help = "!help"

    override suspend fun execute(message: Message, executionData: ExecutionData) {
        message.channel.createEmbed {
            title = "Commands"
            description = messageEntityProvider.commands.values.map { it.help }.distinct().joinToString("\n")
        }
    }
}