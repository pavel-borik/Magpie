package com.pb

import com.pb.config.ConfigurationProvider
import com.pb.database.DaoFacade
import com.pb.http.service.WeatherService
import com.pb.messages.CommandProvider
import com.pb.messages.CommandRegistrationService
import com.pb.messages.FilterProvider
import com.pb.messages.FilterRegistrationService
import com.pb.messages.events.InteractionEventHandler
import com.pb.messages.events.MessageEventHandler
import com.pb.scheduling.ScheduledActionService
import com.zaxxer.hikari.HikariDataSource
import dev.kord.core.Kord
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.on
import dev.kord.gateway.Intents
import dev.kord.gateway.PrivilegedIntent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
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
    val scheduleScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    registerShutdownHookWithLogger {
        shutdownDataSource()
        scheduleScope.cancel("Application shutdown")
    }
    val configuration = ConfigurationProvider.getConfiguration(configLocation)
    logger.info { "Loaded configuration file from ${configLocation.canonicalFile.absolutePath}" }
    logger.info { "Loaded database file from ${dbLocation.canonicalFile.absolutePath}" }

    val client = Kord(configuration.token)
    val dao = DaoFacade(Database.connect(ds))

    val commandRegistrationService = CommandRegistrationService(client)
    val filterRegistrationService = FilterRegistrationService()
    val weatherService = WeatherService(configuration.weatherApiToken)
    val scheduledActionService = ScheduledActionService(scheduleScope)
    val commandProvider = CommandProvider(dao, scheduledActionService, configuration, weatherService, commandRegistrationService)
    val filterProvider = FilterProvider()

    commandRegistrationService.registerCommands(*commandProvider.getCommands().toTypedArray())
    filterRegistrationService.registerFilters(*filterProvider.getFilters().toTypedArray())

    val interactionEventHandler = InteractionEventHandler(configuration, commandRegistrationService)
    val messageEventHandler = MessageEventHandler(configuration, commandRegistrationService, filterRegistrationService)

    client.on<MessageCreateEvent> {
        messageEventHandler.handleMessageCreateEvent(message)
    }

    client.on<GuildChatInputCommandInteractionCreateEvent> {
        interactionEventHandler.handleGuildChatInputCommandInteractionCreateEvent(interaction)
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