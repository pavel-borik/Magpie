package com.pb

import com.pb.config.ConfigurationProvider
import com.pb.database.DaoFacade
import com.pb.http.location.LocationService
import com.pb.http.weather.WeatherService
import com.pb.messages.MessageEntityProvider
import com.pb.messages.MessageHandler
import com.pb.messages.commands.*
import com.pb.messages.filters.Lillie
import com.zaxxer.hikari.HikariDataSource
import dev.kord.core.Kord
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.on
import dev.kord.gateway.Intents
import dev.kord.gateway.PrivilegedIntent
import mu.KLogger
import mu.KotlinLogging
import org.h2.Driver
import org.jetbrains.exposed.sql.Database
import java.io.File

const val PREFIX = "!"

val configLocation = File(System.getProperty("config.path", "configuration.json"))

val dbLocation = File(System.getProperty("database.path", "build/db"))

val ds = HikariDataSource().apply {
    driverClassName = Driver::class.java.name
    jdbcUrl = "jdbc:h2:file:${dbLocation.canonicalFile.absolutePath}"
    username = ""
    password = ""
    maximumPoolSize = 5
    minimumIdle = 5
}

private val logger = KotlinLogging.logger {}

@PrivilegedIntent
suspend fun main() {
    registerShutdownHookWithLogger() {
        shutdownDataSource()
    }
    val configuration = ConfigurationProvider.getConfiguration(configLocation)
    logger.info { "Loaded configuration file from ${configLocation.canonicalFile.absolutePath}" }
    logger.info { "Loaded database file from ${dbLocation.canonicalFile.absolutePath}" }

    val client = Kord(configuration.token)
    val dao = DaoFacade(Database.connect(ds))

    val messageEntityProvider = MessageEntityProvider()
    val weatherService = WeatherService(configuration.weatherApiToken)
    messageEntityProvider.registerCommands(
        Avatar(),
        Location(dao),
        SetLocation(dao, LocationService(configuration.weatherApiToken)),
        RemoveLocation(dao),
        Weather(dao, weatherService),
        Time(dao, weatherService),
        TimeOf(dao, weatherService),
        Help(messageEntityProvider),
    )
    messageEntityProvider.registerFilters(Lillie())

    val messageHandler = MessageHandler(configuration, messageEntityProvider)

    client.on<MessageCreateEvent> {
        messageHandler.handle(message)
    }

    client.login { intents = Intents.all }
}

private fun shutdownDataSource() {
    logger.info { "Going to close HikariDataSource" }
    ds.close()
    logger.info { "HikariDataSource isClosed=${ds.isClosed}" }
}

fun registerShutdownHookWithLogger(block: (logger: KLogger) -> Unit) {
    Runtime.getRuntime().addShutdownHook(Thread { block(logger) })
}