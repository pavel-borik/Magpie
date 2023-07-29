package com.pb.core.commands

import com.pb.core.data.ChatCommand
import com.pb.core.data.CommandExecutionException
import com.pb.core.data.ExecutionData
import com.pb.core.data.InvalidCommandUsageException
import com.pb.core.data.SlashCommand
import com.pb.core.utils.withAuthor
import com.pb.scheduling.ScheduledActionService
import com.pb.scheduling.data.ScheduleRequest
import dev.kord.core.behavior.channel.MessageChannelBehavior
import dev.kord.core.behavior.interaction.response.DeferredPublicMessageInteractionResponseBehavior
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.Message
import dev.kord.core.entity.User
import dev.kord.core.entity.interaction.GuildChatInputCommandInteraction
import dev.kord.rest.builder.interaction.GlobalChatInputCreateBuilder
import dev.kord.rest.builder.interaction.integer
import dev.kord.rest.builder.interaction.string
import mu.KotlinLogging

class RemindMe(
    private val scheduledActionService: ScheduledActionService,
) : ChatCommand, SlashCommand {
    private val logger = KotlinLogging.logger {}

    override val trigger = "remindme"
    override val isAdminOnly = false
    override val help = "!remindme [minutes] [message]"
    override val description = "Receive a reminder after specified number of minutes"

    private val MINUTES_ARG = "minutes"
    private val MESSAGE_ARG = "message"

    override suspend fun execute(message: Message, executionData: ExecutionData) = message.withAuthor(logger) { author ->
        val (delay, reminderMsg) = validateAndGetArgs(executionData.args)
        val channel = message.channel
        scheduleReminder(author, delay, channel, reminderMsg)
        channel.createMessage(getSuccessMessage(author, delay))

    }

    override suspend fun execute(interaction: GuildChatInputCommandInteraction, response: DeferredPublicMessageInteractionResponseBehavior) {
        val delay = interaction.command.integers[MINUTES_ARG]!!
        val reminderMsg = interaction.command.strings[MESSAGE_ARG]!!
        val channel = interaction.channel
        val author = interaction.user
        scheduleReminder(author, delay, channel, reminderMsg)
        response.respond { content = getSuccessMessage(author, delay) }
    }


    override fun buildInputCommand(builder: GlobalChatInputCreateBuilder) {
        with(builder) {
            integer(MINUTES_ARG, "Reminder delay in minutes") {
                required = true
                minValue = 1
                maxValue = 10080
            }
            string(MESSAGE_ARG, "Reminder message") {
                required = true
            }
        }
    }

    private fun validateAndGetArgs(args: List<String>): Pair<Long, String> {
        if (args.size in 0..1) throw InvalidCommandUsageException("Invalid number of arguments.", help)
        val delay = args[0].toLongOrNull() ?: -1
        if (delay !in 1..10080) throw CommandExecutionException("Reminder delay must be between 1 and 10080 minutes")
        val reminderMsg = args.subList(1, args.size).joinToString(" ")
        return delay to reminderMsg
    }

    private suspend fun scheduleReminder(author: User, delay: Long, channel: MessageChannelBehavior, reminderMsg: String) {
        val req = ScheduleRequest(author, delay) { channel.createMessage("${author.mention} $reminderMsg") }
        scheduledActionService.scheduleRequest(req)
    }

    private fun getSuccessMessage(author: User, delay: Long) = "User ${author.username} will be reminded in $delay minute${if (delay != 1L) "s" else ""} from now."
}