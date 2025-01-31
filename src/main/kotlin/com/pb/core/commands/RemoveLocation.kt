package com.pb.core.commands

import com.pb.core.data.ChatCommand
import com.pb.core.data.ExecutionData
import com.pb.core.data.SlashCommand
import com.pb.core.utils.withAuthor
import com.pb.database.DaoFacade
import dev.kord.core.behavior.interaction.response.DeferredPublicMessageInteractionResponseBehavior
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.Message
import dev.kord.core.entity.User
import dev.kord.core.entity.interaction.GuildChatInputCommandInteraction
import dev.kord.rest.builder.interaction.GlobalChatInputCreateBuilder
import io.github.oshai.kotlinlogging.KotlinLogging

class RemoveLocation(private val dao: DaoFacade) : ChatCommand, SlashCommand {
    private val logger = KotlinLogging.logger {}

    override val trigger = "removelocation"
    override val isAdminOnly = false
    override val help = "!removelocation"
    override val description = "Remove location"

    override suspend fun execute(message: Message, executionData: ExecutionData) = message.withAuthor(logger) { author ->
        val userId = author.id
        val guildId = executionData.guild.id

        dao.removeLocation(userId.value, guildId.value)
        message.channel.createMessage(getContent(author))
    }

    override suspend fun execute(interaction: GuildChatInputCommandInteraction, response: DeferredPublicMessageInteractionResponseBehavior) {
        val userId = interaction.user.id
        val guildId = interaction.guildId

        dao.removeLocation(userId.value, guildId.value)
        response.respond { content = getContent(interaction.user) }
    }

    override fun buildInputCommand(builder: GlobalChatInputCreateBuilder) {}

    private fun getContent(author: User) = "Location of user ${author.username} has been removed."
}
