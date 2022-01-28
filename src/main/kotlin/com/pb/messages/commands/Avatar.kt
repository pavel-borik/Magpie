package com.pb.messages.commands

import com.pb.messages.data.Command
import com.pb.messages.data.ExecutionData
import com.pb.messages.data.InvalidCommandUsageException
import com.pb.messages.utils.getUserFromText
import dev.kord.core.entity.Message
import dev.kord.core.entity.User

class Avatar : Command {
    override val triggers = listOf("avatar", "avi")
    override val isAdminOnly = false
    override val help = "!avatar [mention|username]?"

    override suspend fun execute(message: Message, executionData: ExecutionData) {
        getUserFromMessage(message, executionData)?.let { message.channel.createMessage(it.avatar?.url ?: getAvatarNotAvailableMessage(it)) }
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