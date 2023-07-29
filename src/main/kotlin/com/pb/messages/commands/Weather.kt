package com.pb.messages.commands

import com.pb.database.DaoFacade
import com.pb.http.data.ApiOperationResult
import com.pb.http.data.CurrentWeather
import com.pb.http.service.WeatherService
import com.pb.messages.data.ChatCommand
import com.pb.messages.data.CommandExecutionException
import com.pb.messages.data.ExecutionData
import com.pb.messages.data.SlashCommand
import com.pb.messages.utils.withAuthor
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.behavior.interaction.response.DeferredPublicMessageInteractionResponseBehavior
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.Guild
import dev.kord.core.entity.Message
import dev.kord.core.entity.User
import dev.kord.core.entity.interaction.GuildChatInputCommandInteraction
import dev.kord.rest.builder.interaction.GlobalChatInputCreateBuilder
import dev.kord.rest.builder.interaction.string
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.modify.embed
import mu.KotlinLogging
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.round

class Weather(
    private val daoFacade: DaoFacade,
    private val weatherService: WeatherService
) : ChatCommand, SlashCommand {
    private val logger = KotlinLogging.logger {}
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.US)

    override val trigger = "weather"
    override val isAdminOnly = false
    override val help = "!weather [location]?"
    override val description = "Show weather"

    private val LOCATION_ARG = "location"

    override suspend fun execute(message: Message, executionData: ExecutionData) = message.withAuthor(logger) { author ->
        val location = getLocation(author, executionData)
        val currentWeather = getWeather(location)

        message.channel.createEmbed {
            createEmbed(currentWeather)
        }
    }

    override suspend fun execute(interaction: GuildChatInputCommandInteraction, response: DeferredPublicMessageInteractionResponseBehavior) {
        val location = interaction.command.strings[LOCATION_ARG] ?: getUserLocation(interaction.user.asUser(), interaction.guild.asGuild())
        val currentWeather = getWeather(location)

        response.respond {
            embed { createEmbed(currentWeather) }
        }
    }

    private suspend fun getUserLocation(user: User, guild: Guild): String {
        return daoFacade.getUserOrNull(user.id.value, guild.id.value)?.location
            ?: throw CommandExecutionException("User ${user.username} has no location set.")
    }

    override fun buildInputCommand(builder: GlobalChatInputCreateBuilder) {
        with(builder) {
            string(LOCATION_ARG, "Location") {
                required = false
            }
        }
    }

    private fun EmbedBuilder.createEmbed(currentWeather: CurrentWeather) {
        val fullLocation = "${currentWeather.name}, ${currentWeather.sys.country}"
        val tempC = currentWeather.main.temp.round(1)
        val tempF = toFahrenheit(tempC)
        val weatherGeneral = currentWeather.weather.firstOrNull()?.main ?: "N/A"
        val icon = currentWeather.weather.firstOrNull()?.toIcon() ?: ""
        val weatherDescription = currentWeather.weather.firstOrNull()?.description ?: "N/A"
        val windKph = (currentWeather.wind.speed * 3.6).round(1)
        val humidity = currentWeather.main.humidity
        val time = LocalDateTime.now(ZoneOffset.UTC).plusSeconds(currentWeather.timezone)

        title = "Weather in $fullLocation"
        description = """
                |$icon **$weatherGeneral** ($weatherDescription)
                |**Temp:** $tempC °C | $tempF °F
                |**Wind:** $windKph km/h
                |**Humidity:** $humidity%
            """.trimMargin()
        footer { text = "Local time: ${timeFormatter.format(time)}" }
    }

    private suspend fun getLocation(user: User, executionData: ExecutionData): String {
        val (args, guild) = executionData
        return if (args.isNotEmpty()) {
            args.joinToString(" ")
        } else {
            getUserLocation(user, guild)
        }
    }

    private suspend fun getWeather(location: String): CurrentWeather {
        return when (val result = weatherService.getCurrentWeather(location)) {
            is ApiOperationResult.Success -> result.value
            is ApiOperationResult.NotFound -> throw CommandExecutionException("Location '$location' was not recognized.")
            is ApiOperationResult.Error -> throw CommandExecutionException("Failed to retrieve weather information.")
        }
    }

    private fun toFahrenheit(temp: Double): Int {
        return round(((9.toDouble() / 5) * temp + 32)).toInt()
    }

    private fun Double.round(decimals: Int): Double {
        var multiplier = 1.0
        repeat(decimals) { multiplier *= 10 }
        return round(this * multiplier) / multiplier
    }
}