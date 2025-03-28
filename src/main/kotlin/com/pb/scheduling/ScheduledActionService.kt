package com.pb.scheduling

import com.pb.scheduling.data.ScheduleRequest
import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.User
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class ScheduledActionService(private val scope: CoroutineScope) {
    private val logger = KotlinLogging.logger {}

    private val jobs = ConcurrentHashMap<Snowflake, Job>()
    private val mutex = Mutex()

    suspend fun scheduleRequest(scheduleRequest: ScheduleRequest) {
        logger.info { "Scheduling action for user ${scheduleRequest.user.username}" }
        val userId = scheduleRequest.user.id
        mutex.withLock {
            jobs.remove(userId)?.cancelAndJoin()
            val job = scope.launch {
                val delay = scheduleRequest.delayInMinutes.toDuration(DurationUnit.MINUTES)
                logger.debug { "Going to sleep for $delay" }
                delay(delay)
                scheduleRequest.action()
                mutex.withLock { jobs.remove(userId) }
            }
            jobs[userId] = job
        }
    }

    suspend fun cancelRequest(user: User) = mutex.withLock {
        logger.info { "Cancelling reminder for user ${user.username}" }
        val job = jobs.remove(user.id)
        return@withLock if (job != null) {
            job.cancelAndJoin()
            true
        } else {
            false
        }
    }
}