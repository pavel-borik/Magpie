package com.pb.database

import com.pb.database.entities.UserEntity
import com.pb.database.entities.Users
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils.createMissingTablesAndColumns
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class DaoFacade(private val db: Database) {
    init {
        transaction(db) {
            createMissingTablesAndColumns(Users)
        }
    }

    suspend fun getUserOrNull(discordId: ULong, guildId: ULong): UserEntity? {
        return newSuspendedTransaction(Dispatchers.IO, db) {
            Users.selectAll().where { Users.discordId.eq(discordId) and Users.guildId.eq(guildId) }
                .map { UserEntity(it[Users.discordId], it[Users.guildId], it[Users.location]) }
                .singleOrNull()
        }
    }

    suspend fun setLocation(discordId: ULong, guildId: ULong, location: String) {
        newSuspendedTransaction(Dispatchers.IO, db) {
            val user = Users.selectAll().where { Users.discordId.eq(discordId) and Users.guildId.eq(guildId) }
                .map { UserEntity(it[Users.discordId], it[Users.guildId], it[Users.location]) }
                .singleOrNull()

            if (user == null) {
                Users.insert {
                    it[this.discordId] = discordId
                    it[this.guildId] = guildId
                    it[this.location] = location
                }
            } else {
                Users.update({ Users.discordId.eq(discordId) and Users.guildId.eq(guildId) }) {
                    it[this.location] = location
                }
            }
        }
    }

    suspend fun removeLocation(discordId: ULong, guildId: ULong) {
        newSuspendedTransaction(Dispatchers.IO, db) {
            val location = Users.select(Users.location)
                .where { Users.discordId.eq(discordId) and Users.guildId.eq(guildId) }
                .map { it[Users.location] }
                .singleOrNull()

            if (location != null) {
                Users.update({ Users.discordId.eq(discordId) and Users.guildId.eq(guildId) }) {
                    it[this.location] = null
                }
            }
        }
    }
}