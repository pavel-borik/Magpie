package com.pb.messages.commands

import com.pb.database.DaoFacade
import com.pb.http.data.ApiOperationResult
import com.pb.http.data.Location
import com.pb.http.service.LocationService
import com.pb.messages.data.Command
import com.pb.messages.data.CommandExecutionException
import com.pb.messages.data.ExecutionData
import com.pb.messages.data.InvalidCommandUsageException
import com.pb.messages.utils.withAuthor
import dev.kord.core.entity.Message
import mu.KotlinLogging

class SetLocation(
    private val dao: DaoFacade,
    private val locationService: LocationService,
) : Command {
    private val logger = KotlinLogging.logger {}

    override val triggers = listOf("setLocation")
    override val isAdminOnly = false
    override val help = "!setLocation [location]"

    override suspend fun execute(message: Message, executionData: ExecutionData) = message.withAuthor(logger) { author ->
        val locationParam = getLocationParam(executionData.args)

        val userId = author.id
        val guildId = executionData.guild.id

        val location = getLocation(locationParam)
        val locationToSave = "${location.name}, ${location.country}"
        dao.setLocation(userId.value, guildId.value, locationToSave)
        dao.getUserOrNull(userId.value, guildId.value)
            ?.let { message.channel.createMessage("User ${author.username} has set the location to '${it.location}'.") }
            ?: throw CommandExecutionException("Could not find user ${author.username} in the database.")
    }

    private fun getLocationParam(args: List<String>): String {
        return if (args.isNotEmpty()) {
            args.joinToString(" ")
        } else {
            throw InvalidCommandUsageException("No location has been specified.", help)
        }
    }

    private suspend fun getLocation(location: String): Location {
        return when (val result = locationService.getLocationData(location)) {
            is ApiOperationResult.Success -> result.value.firstOrNull()
                ?: throw CommandExecutionException("Location '$location' was not recognized.")
            is ApiOperationResult.NotFound -> throw CommandExecutionException("Location '$location' was not recognized.")
            is ApiOperationResult.Error -> throw CommandExecutionException("Failed to retrieve location information.")
        }
    }
}