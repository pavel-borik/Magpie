package com.pb.core.filters

import com.pb.core.data.Filter
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.entity.Message

class Lillie : Filter {
    private val regex = Regex("li([l. ]?|[l. ]+)lie", RegexOption.IGNORE_CASE)

    override suspend fun doFilter(message: Message) {
        val author = message.author ?: return
        if (regex.containsMatchIn(message.content)) {
            message.delete()
            message.channel.createMessage {
                content = "${author.mention} \uD83E\uDD21"
            }
        }
    }
}