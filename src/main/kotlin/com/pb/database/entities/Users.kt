package com.pb.database.entities

import org.jetbrains.exposed.sql.Table

@OptIn(ExperimentalUnsignedTypes::class)
object Users : Table() {
    val discordId = ulong("discord_id")
    val guildId = ulong("guild_id")
    val location = varchar("location", 50).nullable()

    override val primaryKey = PrimaryKey(discordId, guildId)
}

data class UserEntity(
    val discordId: ULong,
    val guildId: ULong,
    val location: String?
)