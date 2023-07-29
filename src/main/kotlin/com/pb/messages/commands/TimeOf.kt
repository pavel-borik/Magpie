package com.pb.messages.commands

import com.pb.database.DaoFacade
import com.pb.http.data.ApiOperationResult
import com.pb.http.data.CurrentWeather
import com.pb.http.service.WeatherService
import com.pb.messages.data.CommandExecutionException
import com.pb.messages.data.ExecutionData
import com.pb.messages.data.InvalidCommandUsageException
import com.pb.messages.utils.getUserFromText
import dev.kord.core.behavior.interaction.response.DeferredPublicMessageInteractionResponseBehavior
import dev.kord.core.entity.Guild
import dev.kord.core.entity.Message
import dev.kord.core.entity.User
import dev.kord.core.entity.interaction.GuildChatInputCommandInteraction
import dev.kord.rest.builder.interaction.GlobalChatInputCreateBuilder
import dev.kord.rest.builder.interaction.user

class TimeOf(
    private val daoFacade: DaoFacade,
    private val weatherService: WeatherService
) : AbstractTimeCommand() {
    override val trigger = "timeof"
    override val isAdminOnly = false
    override val help = "!timeof [mention|username]"
    override val description = "Show a user's time information"

    private val USER_ARG = "user"

    override suspend fun execute(message: Message, executionData: ExecutionData) {
        val user = getUserFromMessage(executionData)
        val location = getLocation(user, executionData)
        val currentWeather = getWeather(location)

        createTimeMessage(message, currentWeather)
    }

    override suspend fun execute(interaction: GuildChatInputCommandInteraction, response: DeferredPublicMessageInteractionResponseBehavior) {
        val user = interaction.command.users[USER_ARG]!!
        val location = getUserLocation(user, interaction.guild.asGuild())
        val currentWeather = getWeather(location)

        createTimeMessage(response, currentWeather)
    }

    override fun buildInputCommand(builder: GlobalChatInputCreateBuilder) {
        with(builder) {
            user(USER_ARG, "User") {
                required = true
            }
        }
    }

    private suspend fun getUserFromMessage(executionData: ExecutionData): User {
        val (args, guild) = executionData
        return when (args.size) {
            1 -> getUserFromText(args[0], guild)
            else -> throw InvalidCommandUsageException("Invalid number of arguments.", help)
        }
    }

    private suspend fun getLocation(user: User, executionData: ExecutionData): String {
        return getUserLocation(user, executionData.guild)
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