package com.pb.messages.data

import dev.kord.core.entity.Guild
import dev.kord.core.entity.Message

interface Command {
    val triggers: List<String>
    val isAdminOnly: Boolean
    val help: String
    suspend fun execute(message: Message, executionData: ExecutionData)
}

data class ExecutionData(
    val args: List<String>,
    val guild: Guild,
)

class InvalidCommandUsageException(val reason: String, val help: String) : RuntimeException()
class CommandExecutionException(val reason: String) : RuntimeException(reason)