package com.pb.core.commands

import com.pb.core.data.CommandExecutionException
import com.pb.core.data.ExecutionData
import com.pb.core.utils.withAuthor
import com.pb.database.DaoFacade
import com.pb.http.data.ApiOperationResult
import com.pb.http.data.CurrentWeather
import com.pb.http.service.WeatherService
import dev.kord.core.behavior.interaction.response.DeferredPublicMessageInteractionResponseBehavior
import dev.kord.core.entity.Guild
import dev.kord.core.entity.Message
import dev.kord.core.entity.User
import dev.kord.core.entity.interaction.GuildChatInputCommandInteraction
import dev.kord.rest.builder.interaction.GlobalChatInputCreateBuilder
import dev.kord.rest.builder.interaction.string
import mu.KotlinLogging

class Time(
    private val daoFacade: DaoFacade,
    private val weatherService: WeatherService
) : AbstractTimeCommand() {
    private val logger = KotlinLogging.logger {}

    override val trigger = "time"
    override val isAdminOnly = false
    override val help = "!time [location]?"
    override val description = "Display the time"

    private val LOCATION_ARG = "location"

    override suspend fun execute(message: Message, executionData: ExecutionData) = message.withAuthor(logger) { author ->
        val location = getLocation(author, executionData)
        val currentWeather = getWeather(location)

        createTimeMessage(message, currentWeather)
    }

    override suspend fun execute(interaction: GuildChatInputCommandInteraction, response: DeferredPublicMessageInteractionResponseBehavior) {
        val location = interaction.command.strings[LOCATION_ARG] ?: getUserLocation(interaction.user.asUser(), interaction.guild.asGuild())
        val currentWeather = getWeather(location)

        createTimeMessage(response, currentWeather)
    }

    override fun buildInputCommand(builder: GlobalChatInputCreateBuilder) {
        with(builder) {
            string(LOCATION_ARG, "Location") {
                required = false
            }
        }
    }

    private suspend fun getLocation(user: User, executionData: ExecutionData): String {
        val (args, guild) = executionData
        return if (args.isNotEmpty()) {
            args.joinToString(" ")
        } else {
            getUserLocation(user, guild)
        }
    }

    private suspend fun getUserLocation(user: User, guild: Guild): String {
        return daoFacade.getUserOrNull(user.id.value, guild.id.value)?.location
            ?: throw CommandExecutionException("User ${user.username} has no location set.")
    }

    private suspend fun getWeather(location: String): CurrentWeather {
        return when (val result = weatherService.getCurrentWeather(location)) {
            is ApiOperationResult.Success -> result.value
            is ApiOperationResult.NotFound -> throw CommandExecutionException("Location '$location' was not recognized.")
            is ApiOperationResult.Error -> throw CommandExecutionException("Failed to retrieve time information.")
        }
    }
}