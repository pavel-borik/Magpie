package com.pb.messages.data

import dev.kord.core.behavior.interaction.response.DeferredPublicMessageInteractionResponseBehavior
import dev.kord.core.entity.Guild
import dev.kord.core.entity.Message
import dev.kord.core.entity.interaction.GuildChatInputCommandInteraction
import dev.kord.rest.builder.interaction.GlobalChatInputCreateBuilder

interface Command {
    val trigger: String
    val isAdminOnly: Boolean
    val help: String
    val description: String
}

interface ChatCommand: Command {
    suspend fun execute(message: Message, executionData: ExecutionData)
}

interface SlashCommand: Command {
    suspend fun execute(interaction: GuildChatInputCommandInteraction, response: DeferredPublicMessageInteractionResponseBehavior)
    fun buildInputCommand(builder: GlobalChatInputCreateBuilder)
}

data class ExecutionData(
    val args: List<String>,
    val guild: Guild,
)

class InvalidCommandUsageException(val reason: String, val help: String) : RuntimeException()
class CommandExecutionException(val reason: String) : RuntimeException(reason)