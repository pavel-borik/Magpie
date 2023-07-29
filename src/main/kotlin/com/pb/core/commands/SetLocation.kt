package com.pb.core.commands

import com.pb.core.data.ChatCommand
import com.pb.core.data.CommandExecutionException
import com.pb.core.data.ExecutionData
import com.pb.core.data.InvalidCommandUsageException
import com.pb.core.data.SlashCommand
import com.pb.core.utils.withAuthor
import com.pb.database.DaoFacade
import com.pb.http.data.ApiOperationResult
import com.pb.http.data.Location
import com.pb.http.service.LocationService
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.interaction.response.DeferredPublicMessageInteractionResponseBehavior
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.Message
import dev.kord.core.entity.User
import dev.kord.core.entity.interaction.GuildChatInputCommandInteraction
import dev.kord.rest.builder.interaction.GlobalChatInputCreateBuilder
import dev.kord.rest.builder.interaction.string
import mu.KotlinLogging

class SetLocation(
    private val dao: DaoFacade,
    private val locationService: LocationService,
) : ChatCommand, SlashCommand {
    private val logger = KotlinLogging.logger {}

    override val trigger = "setlocation"
    override val isAdminOnly = false
    override val help = "!setlocation [location]"
    override val description = "Set the location"

    private val LOCATION_ARG = "location"

    override suspend fun execute(message: Message, executionData: ExecutionData) = message.withAuthor(logger) { author ->
        val locationParam = getLocationParam(executionData.args)
        val location = getLocation(locationParam)

        saveLocation(location, author, executionData.guild.id)
        message.channel.createMessage(getMessageContent(author, executionData.guild.id))
    }

    override suspend fun execute(interaction: GuildChatInputCommandInteraction, response: DeferredPublicMessageInteractionResponseBehavior) {
        val locationParam = interaction.command.strings[LOCATION_ARG]!!
        val location = getLocation(locationParam)

        saveLocation(location, interaction.user, interaction.guildId)
        response.respond { content = getMessageContent(interaction.user, interaction.guildId) }
    }

    override fun buildInputCommand(builder: GlobalChatInputCreateBuilder) {
        with(builder) {
            string(LOCATION_ARG, "Location") {
                required = true
            }
        }
    }

    private suspend fun saveLocation(location: Location, user: User, guildId: Snowflake) {
        val locationToSave = "${location.name}, ${location.country}"
        dao.setLocation(user.id.value, guildId.value, locationToSave)
    }

    private fun getLocationParam(args: List<String>): String {
        return if (args.isNotEmpty()) {
            args.joinToString(" ")
        } else {
            throw InvalidCommandUsageException("No location has been specified.", help)
        }
    }

    private suspend fun getLocation(location: String): Location {
        return when (val result = locationService.getLocationData(location)) {
            is ApiOperationResult.Success -> result.value.firstOrNull()
                ?: throw CommandExecutionException("Location '$location' was not recognized.")

            is ApiOperationResult.NotFound -> throw CommandExecutionException("Location '$location' was not recognized.")
            is ApiOperationResult.Error -> throw CommandExecutionException("Failed to retrieve location information.")
        }
    }

    private suspend fun getMessageContent(user: User, guildId: Snowflake): String {
        return dao.getUserOrNull(user.id.value, guildId.value)
            ?.let { "User ${user.username} has set the location to '${it.location}'." }
            ?: throw CommandExecutionException("Could not find user ${user.username} in the database.")
    }
}