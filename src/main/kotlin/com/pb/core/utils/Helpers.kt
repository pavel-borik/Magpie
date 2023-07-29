package com.pb.core.utils

import com.pb.core.data.CommandExecutionException
import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.Guild
import dev.kord.core.entity.Message
import dev.kord.core.entity.User
import kotlinx.coroutines.flow.firstOrNull
import mu.KLogger

val regex = Regex("^<@!?(\\d+)>\$")

fun getUserIdFromMentionOrNull(mention: String): Long? {
    val result = regex.matchEntire(mention)
    return result?.groups?.get(1)?.value?.toLongOrNull()
}

suspend fun getUserFromText(idOrUsername: String, guild: Guild): User {
    return getUserIdFromMentionOrNull(idOrUsername)
        ?.let { id -> guild.getMemberOrNull(Snowflake(id)) }
        ?: guild.members.firstOrNull { it.username == idOrUsername }
        ?: throw CommandExecutionException("Specified user '${idOrUsername}' was not found.")
}

suspend fun Message.withAuthor(logger: KLogger, callback: suspend (User) -> Unit) {
    if (author != null) {
        callback(this.author!!)
    } else {
        logger.warn { "Message $this had no author" }
    }
}