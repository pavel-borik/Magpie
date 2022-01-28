package com.pb.messages.commands

import com.pb.database.DaoFacade
import com.pb.messages.data.Command
import com.pb.messages.data.ExecutionData
import com.pb.messages.utils.withAuthor
import dev.kord.core.entity.Message
import mu.KotlinLogging

class RemoveLocation(private val dao: DaoFacade) : Command {
    private val logger = KotlinLogging.logger {}

    override val triggers = listOf("removeLocation")
    override val isAdminOnly = false
    override val help = "!removeLocation"

    override suspend fun execute(message: Message, executionData: ExecutionData) = message.withAuthor(logger) { author ->
        val userId = author.id
        val guildId = executionData.guild.id

        dao.removeLocation(userId.value, guildId.value)
        message.channel.createMessage("Location of user ${author.username} has been removed.")
    }
}