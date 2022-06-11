package com.pb.messages.commands

import com.pb.messages.data.Command
import com.pb.messages.data.ExecutionData
import com.pb.messages.utils.withAuthor
import com.pb.scheduling.ScheduledActionService
import dev.kord.core.entity.Message
import mu.KotlinLogging

class UnRemindMe(
    private val scheduledActionService: ScheduledActionService,
) : Command {
    private val logger = KotlinLogging.logger {}

    override val triggers = listOf("unremindme")
    override val isAdminOnly = false
    override val help = "!unremindme"

    override suspend fun execute(message: Message, executionData: ExecutionData) = message.withAuthor(logger) { author ->
        logger.info { "Cancelling reminder for user ${author.username}" }
        val result = scheduledActionService.cancelRequest(author)
        if (result) {
            message.channel.createMessage("Reminder for user ${author.username} has been cancelled")
        } else {
            message.channel.createMessage("Reminder for user ${author.username} does not exist")
        }
    }
}