package com.pb.core.data

import dev.kord.core.entity.Message

interface Filter {
    suspend fun doFilter(message: Message)
}