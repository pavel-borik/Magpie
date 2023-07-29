package com.pb.messages.commands

import com.pb.messages.data.ChatCommand
import com.pb.messages.data.ExecutionData
import com.pb.messages.data.SlashCommand
import com.pb.messages.utils.withAuthor
import com.pb.scheduling.ScheduledActionService
import dev.kord.core.behavior.interaction.response.DeferredPublicMessageInteractionResponseBehavior
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.Message
import dev.kord.core.entity.User
import dev.kord.core.entity.interaction.GuildChatInputCommandInteraction
import dev.kord.rest.builder.interaction.GlobalChatInputCreateBuilder
import mu.KotlinLogging

class UnRemindMe(
    private val scheduledActionService: ScheduledActionService,
) : ChatCommand, SlashCommand {
    private val logger = KotlinLogging.logger {}

    override val trigger = "unremindme"
    override val isAdminOnly = false
    override val help = "!unremindme"
    override val description = "Delete the reminder"

    override suspend fun execute(message: Message, executionData: ExecutionData) = message.withAuthor(logger) { author ->
        val result = scheduledActionService.cancelRequest(author)
        if (result) {
            message.channel.createMessage(getReminderCancelledMessage(author))
        } else {
            message.channel.createMessage(getReminderNotFoundMessage(author))
        }
    }

    override suspend fun execute(interaction: GuildChatInputCommandInteraction, response: DeferredPublicMessageInteractionResponseBehavior) {
        val author = interaction.user
        val result = scheduledActionService.cancelRequest(author)
        if (result) {
            response.respond { content = getReminderCancelledMessage(author) }
        } else {
            response.respond { content = getReminderNotFoundMessage(author) }
        }
    }

    override fun buildInputCommand(builder: GlobalChatInputCreateBuilder) {}

    private fun getReminderCancelledMessage(author: User) = "Reminder for user ${author.username} has been cancelled"

    private fun getReminderNotFoundMessage(author: User) = "Reminder for user ${author.username} does not exist"
}
