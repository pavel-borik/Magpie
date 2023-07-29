package com.pb.messages.commands

import com.pb.messages.data.ChatCommand
import com.pb.messages.data.ExecutionData
import com.pb.messages.data.InvalidCommandUsageException
import com.pb.messages.data.SlashCommand
import com.pb.messages.utils.getUserFromText
import dev.kord.core.behavior.interaction.response.DeferredPublicMessageInteractionResponseBehavior
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.Message
import dev.kord.core.entity.User
import dev.kord.core.entity.interaction.GuildChatInputCommandInteraction
import dev.kord.rest.builder.interaction.GlobalChatInputCreateBuilder
import dev.kord.rest.builder.interaction.user

class Avatar : ChatCommand, SlashCommand {
    override val trigger = "avatar"
    override val isAdminOnly = false
    override val help = "!avatar [mention|username]?"
    override val description = "Display a user's avatar"

    private val USER_ARG = "user"

    override suspend fun execute(message: Message, executionData: ExecutionData) {
        getUserFromMessage(message, executionData)?.let { user ->
            val msgContent = getMessageContent(user)
            message.channel.createMessage(msgContent)
        }
    }

    override suspend fun execute(interaction: GuildChatInputCommandInteraction, response: DeferredPublicMessageInteractionResponseBehavior) {
        val user = interaction.command.users[USER_ARG] ?: interaction.user.asUser()
        val msgContent = getMessageContent(user)
        response.respond {
            content = msgContent
        }
    }

    override fun buildInputCommand(builder: GlobalChatInputCreateBuilder) {
        with(builder) {
            user(USER_ARG, "User") {
                required = false
            }
        }
    }

    private fun getMessageContent(user: User): String {
        return user.avatar?.url ?: getAvatarNotAvailableMessage(user)
    }

    private suspend fun getUserFromMessage(message: Message, executionData: ExecutionData): User? {
        val (args, guild) = executionData
        return when (args.size) {
            0 -> message.author
            1 -> getUserFromText(args[0], guild)
            else -> throw InvalidCommandUsageException("Invalid number of arguments.", help)
        }
    }

    private fun getAvatarNotAvailableMessage(user: User) = "Avatar of user ${user.username} is not available"
}