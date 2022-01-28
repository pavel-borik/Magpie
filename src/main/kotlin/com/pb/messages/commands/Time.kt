package com.pb.messages.commands

import com.pb.database.DaoFacade
import com.pb.http.data.HttpCallResult
import com.pb.http.weather.WeatherService
import com.pb.http.weather.data.CurrentWeather
import com.pb.messages.data.CommandExecutionException
import com.pb.messages.data.ExecutionData
import com.pb.messages.utils.withAuthor
import dev.kord.core.entity.Message
import dev.kord.core.entity.User
import mu.KotlinLogging

class Time(
    private val daoFacade: DaoFacade,
    private val weatherService: WeatherService
) : AbstractTimeCommand() {
    private val logger = KotlinLogging.logger {}

    override val triggers = listOf("time", "getTime")
    override val isAdminOnly = false
    override val help = "!time [location]?"

    override suspend fun execute(message: Message, executionData: ExecutionData) = message.withAuthor(logger) { author ->
        val location = getLocation(author, executionData)
        val currentWeather = getWeather(location)

        createTimeMessage(message, currentWeather)
    }

    private suspend fun getLocation(user: User, executionData: ExecutionData): String {
        val (args, guild) = executionData
        return if (args.isNotEmpty()) {
            args.joinToString(" ")
        } else {
            daoFacade.getUserOrNull(user.id.value, guild.id.value)?.location
                ?: throw CommandExecutionException("User ${user.username} has no location set.")
        }
    }

    private suspend fun getWeather(location: String): CurrentWeather {
        return when (val result = weatherService.getCurrentWeather(location)) {
            is HttpCallResult.Success -> result.value
            is HttpCallResult.NotFound -> throw CommandExecutionException("Location '$location' was not recognized.")
            is HttpCallResult.Error -> throw CommandExecutionException("Failed to retrieve time information.")
        }
    }
}