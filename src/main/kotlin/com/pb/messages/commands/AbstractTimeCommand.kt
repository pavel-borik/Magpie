package com.pb.messages.commands

import com.pb.http.data.CurrentWeather
import com.pb.messages.data.Command
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.entity.Message
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*

abstract class AbstractTimeCommand : Command {
    private val dayFormatter = DateTimeFormatter.ofPattern("E, dd MMM YYYY", Locale.US)
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.US)

    protected suspend fun createTimeMessage(message: Message, currentWeather: CurrentWeather) {
        val time = LocalDateTime.now(ZoneOffset.UTC).plusSeconds(currentWeather.timezone)
        val sunrise = LocalDateTime.ofEpochSecond(currentWeather.sys.sunrise, 0, ZoneOffset.UTC)
            .plusSeconds(currentWeather.timezone)
            .truncatedTo(ChronoUnit.MINUTES)
        val sunset = LocalDateTime.ofEpochSecond(currentWeather.sys.sunset, 0, ZoneOffset.UTC)
            .plusSeconds(currentWeather.timezone)
            .truncatedTo(ChronoUnit.MINUTES)
        val fullLocation = "${currentWeather.name}, ${currentWeather.sys.country}"
        val dayLength = LocalTime.MIN.plus(Duration.between(sunrise, sunset))
        message.channel.createEmbed {
            title = "Time in $fullLocation"
            description = """
                |🕑 **Local time:** ${timeFormatter.format(time)}
                |🌅 **Sunrise:** ${timeFormatter.format(sunrise)}
                |🌇 **Sunset:** ${timeFormatter.format(sunset)} (${dayLength.hour}h ${dayLength.minute}m)
            """.trimMargin()
            footer { text = dayFormatter.format(time) }
        }
    }
}