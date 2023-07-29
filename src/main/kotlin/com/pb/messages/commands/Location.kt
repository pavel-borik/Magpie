package com.pb.messages.commands

import com.pb.database.DaoFacade
import com.pb.messages.data.ChatCommand
import com.pb.messages.data.ExecutionData
import com.pb.messages.data.InvalidCommandUsageException
import com.pb.messages.data.SlashCommand
import com.pb.messages.utils.getUserFromText
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.interaction.response.DeferredPublicMessageInteractionResponseBehavior
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.Message
import dev.kord.core.entity.User
import dev.kord.core.entity.interaction.GuildChatInputCommandInteraction
import dev.kord.rest.builder.interaction.GlobalChatInputCreateBuilder
import dev.kord.rest.builder.interaction.user

class Location(private val dao: DaoFacade) : ChatCommand, SlashCommand {
    override val trigger = "location"
    override val isAdminOnly = false
    override val help = "!location [mention|username]?"
    override val description = "Display a user's location"

    private val USER_ARG = "user"

    override suspend fun execute(message: Message, executionData: ExecutionData) {
        val user = getUserFromMessage(message, executionData) ?: return
        val location = getUserLocationOrNull(user, executionData.guild.id)

        message.channel.createMessage(getMessageContent(location, user))
    }

    override suspend fun execute(interaction: GuildChatInputCommandInteraction, response: DeferredPublicMessageInteractionResponseBehavior) {
        val user = interaction.command.users[USER_ARG] ?: interaction.user
        val location = getUserLocationOrNull(user, interaction.guild.id)

        response.respond { content = getMessageContent(location, user) }
    }

    override fun buildInputCommand(builder: GlobalChatInputCreateBuilder) {
        with(builder) {
            user(USER_ARG, "User") {
                required = false
            }
        }
    }

    private suspend fun getUserFromMessage(message: Message, executionData: ExecutionData): User? {
        val (args, guild) = executionData
        return when (args.size) {
            0 -> message.author
            1 -> getUserFromText(args[0], guild)
            else -> throw InvalidCommandUsageException("Invalid number of arguments.", help)
        }
    }

    private suspend fun getUserLocationOrNull(user: User, guildId: Snowflake): String? {
        return dao.getUserOrNull(user.id.value, guildId.value)?.location
    }

    private fun getMessageContent(location: String?, user: User): String {
        return if (location != null) "Location of user ${user.username} is '${location}'." else "User ${user.username} has no location set."
    }
}