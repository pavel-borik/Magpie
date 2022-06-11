package com.pb.messages.commands

import com.pb.messages.data.Command
import com.pb.messages.data.CommandExecutionException
import com.pb.messages.data.ExecutionData
import com.pb.messages.data.InvalidCommandUsageException
import com.pb.messages.utils.withAuthor
import com.pb.scheduling.ScheduledActionService
import com.pb.scheduling.data.ScheduleRequest
import dev.kord.core.entity.Message
import mu.KotlinLogging

class RemindMe(
    private val scheduledActionService: ScheduledActionService,
) : Command {
    private val logger = KotlinLogging.logger {}

    override val triggers = listOf("remindme")
    override val isAdminOnly = false
    override val help = "!remindme [minutes] [message]"

    override suspend fun execute(message: Message, executionData: ExecutionData) = message.withAuthor(logger) { author ->
        val (delay, reminderMsg) = validateAndGetArgs(executionData.args)
        val channel = message.channel
        val req = ScheduleRequest(author, delay) { channel.createMessage("${author.mention} $reminderMsg") }
        scheduledActionService.scheduleRequest(req)
        channel.createMessage("User ${author.username} will be reminded in $delay minute${if (delay != 1) "s" else ""} from now.")
    }

    private fun validateAndGetArgs(args: List<String>): Pair<Int, String> {
        if (args.size in 0..1) throw InvalidCommandUsageException("Invalid number of arguments.", help)
        val delay = args[0].toIntOrNull() ?: -1
        if (delay !in 1..10080) throw CommandExecutionException("Reminder delay must be between 1 and 10080 minutes")
        val reminderMsg = args.subList(1, args.size).joinToString(" ")
        return delay to reminderMsg
    }
}