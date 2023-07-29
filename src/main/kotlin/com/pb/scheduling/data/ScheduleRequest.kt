package com.pb.scheduling.data

import dev.kord.core.entity.User

data class ScheduleRequest(
    val user: User,
    val delayInMinutes: Long,
    val action: suspend () -> Unit,
)