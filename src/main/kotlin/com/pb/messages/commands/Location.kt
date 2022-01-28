package com.pb.messages.commands

import com.pb.database.DaoFacade
import com.pb.messages.data.Command
import com.pb.messages.data.ExecutionData
import com.pb.messages.data.InvalidCommandUsageException
import com.pb.messages.utils.getUserFromText
import dev.kord.core.entity.Guild
import dev.kord.core.entity.Message
import dev.kord.core.entity.User

class Location(private val dao: DaoFacade) : Command {
    override val triggers = listOf("location", "getLocation")
    override val isAdminOnly = false
    override val help = "!location [mention|username]?"

    override suspend fun execute(message: Message, executionData: ExecutionData) {
        getUserFromMessage(message, executionData)?.let { createLocationMessage(it, executionData.guild, message) }
    }

    private suspend fun getUserFromMessage(message: Message, executionData: ExecutionData): User? {
        val (args, guild) = executionData
        return when (args.size) {
            0 -> message.author
            1 -> getUserFromText(args[0], guild)
            else -> throw InvalidCommandUsageException("Invalid number of arguments.", help)
        }
    }

    private suspend fun createLocationMessage(user: User, guild: Guild, message: Message) {
        dao.getUserOrNull(user.id.value, guild.id.value)?.location
            ?.let { message.channel.createMessage("Location of user ${user.username} is '${it}'.") }
            ?: run { message.channel.createMessage("User ${user.username} has no location set.") }
    }
}