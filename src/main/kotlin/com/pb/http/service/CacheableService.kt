package com.pb.http.service

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.Scheduler
import com.pb.http.data.ApiOperationResult
import io.github.oshai.kotlinlogging.KotlinLogging
import java.time.Duration

abstract class CacheableService<K, V>(
    expiration: Duration
) {
    private val logger = KotlinLogging.logger {}

    private val cache: Cache<K, V> = Caffeine.newBuilder()
        .scheduler(Scheduler.systemScheduler())
        .maximumSize(100)
        .evictionListener<K, V> { key, _, cause -> logger.debug { "Evicted entry for '$key' - $cause" } }
        .expireAfterWrite(expiration)
        .build()

    protected abstract suspend fun compute(key: K): ApiOperationResult<V>

    protected suspend fun getOrCompute(key: K): ApiOperationResult<V> {
        val cached = cache.getIfPresent(key)
        return if (cached != null) {
            logger.debug { "Retrieved cached entry for '$key'" }
            ApiOperationResult.Success(cached)
        } else {
            when (val result = compute(key)) {
                is ApiOperationResult.Success -> {
                    cache.put(key, result.value)
                    result
                }
                is ApiOperationResult.Error -> result
                is ApiOperationResult.NotFound -> result
            }
        }
    }
}