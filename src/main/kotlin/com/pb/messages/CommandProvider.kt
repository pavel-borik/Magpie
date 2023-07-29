package com.pb.messages

import com.pb.config.Configuration
import com.pb.database.DaoFacade
import com.pb.http.service.LocationService
import com.pb.http.service.WeatherService
import com.pb.messages.commands.Avatar
import com.pb.messages.commands.Help
import com.pb.messages.commands.Location
import com.pb.messages.commands.RemindMe
import com.pb.messages.commands.RemoveLocation
import com.pb.messages.commands.SetLocation
import com.pb.messages.commands.Time
import com.pb.messages.commands.TimeOf
import com.pb.messages.commands.UnRemindMe
import com.pb.messages.commands.Weather
import com.pb.messages.data.Command
import com.pb.scheduling.ScheduledActionService

class CommandProvider(
    private val dao: DaoFacade,
    private val scheduledActionService: ScheduledActionService,
    private val configuration: Configuration,
    private val weatherService: WeatherService,
    private val commandRegistrationService: CommandRegistrationService
) {

    fun getCommands(): List<Command> {
        return listOf(
            Avatar(),
            Location(dao),
            RemindMe(scheduledActionService),
            UnRemindMe(scheduledActionService),
            SetLocation(dao, LocationService(configuration.weatherApiToken)),
            RemoveLocation(dao),
            Weather(dao, weatherService),
            Time(dao, weatherService),
            TimeOf(dao, weatherService),
            Help(commandRegistrationService),
        )
    }
}