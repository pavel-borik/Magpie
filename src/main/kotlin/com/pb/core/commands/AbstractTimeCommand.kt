package com.pb.core.commands

import com.pb.core.data.ChatCommand
import com.pb.core.data.SlashCommand
import com.pb.http.data.CurrentWeather
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.behavior.interaction.response.DeferredPublicMessageInteractionResponseBehavior
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.Message
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.embed
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

abstract class AbstractTimeCommand : ChatCommand, SlashCommand {
    private val dayFormatter = DateTimeFormatter.ofPattern("E, dd MMM YYYY", Locale.US)
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.US)

    protected suspend fun createTimeMessage(message: Message, currentWeather: CurrentWeather) {
        message.channel.createEmbed {
            createEmbed(currentWeather)
        }
    }

    protected suspend fun createTimeMessage(response: DeferredPublicMessageInteractionResponseBehavior, currentWeather: CurrentWeather) {
        response.respond {
            embed { createEmbed(currentWeather) }
        }
    }

    private fun EmbedBuilder.createEmbed(currentWeather: CurrentWeather) {
        val time = LocalDateTime.now(ZoneOffset.UTC).plusSeconds(currentWeather.timezone)
        val sunrise = LocalDateTime.ofEpochSecond(currentWeather.sys.sunrise, 0, ZoneOffset.UTC).plusSeconds(currentWeather.timezone).truncatedTo(ChronoUnit.MINUTES)
        val sunset = LocalDateTime.ofEpochSecond(currentWeather.sys.sunset, 0, ZoneOffset.UTC).plusSeconds(currentWeather.timezone).truncatedTo(ChronoUnit.MINUTES)
        val fullLocation = "${currentWeather.name}, ${currentWeather.sys.country}"
        val dayLength = LocalTime.MIN.plus(Duration.between(sunrise, sunset))
        title = "Time in $fullLocation"
        description = """
                |🕑 **Local time:** ${timeFormatter.format(time)}
                |🌅 **Sunrise:** ${timeFormatter.format(sunrise)}
                |🌇 **Sunset:** ${timeFormatter.format(sunset)} (${dayLength.hour}h ${dayLength.minute}m)
            """.trimMargin()
        footer { text = dayFormatter.format(time) }
    }
}