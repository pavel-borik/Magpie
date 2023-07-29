package com.pb.core

import com.pb.config.Configuration
import com.pb.core.commands.Avatar
import com.pb.core.commands.Help
import com.pb.core.commands.Location
import com.pb.core.commands.RemindMe
import com.pb.core.commands.RemoveLocation
import com.pb.core.commands.SetLocation
import com.pb.core.commands.Time
import com.pb.core.commands.TimeOf
import com.pb.core.commands.UnRemindMe
import com.pb.core.commands.Weather
import com.pb.core.data.Command
import com.pb.database.DaoFacade
import com.pb.http.service.LocationService
import com.pb.http.service.WeatherService
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