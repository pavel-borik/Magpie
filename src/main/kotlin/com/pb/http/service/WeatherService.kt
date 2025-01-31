package com.pb.http.service

import com.pb.http.client.HttpClientProvider
import com.pb.http.data.ApiOperationResult
import com.pb.http.data.CurrentWeather
import com.pb.serialization.ObjectMapperProvider
import com.pb.serialization.readValue
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import java.time.Duration

class WeatherService(private val token: String) : CacheableService<String, CurrentWeather>(Duration.ofMinutes(5)) {
    private val logger = KotlinLogging.logger {}
    private val baseUrl = "http://api.openweathermap.org/data/2.5/weather"
    private val client = HttpClientProvider.client
    private val mapper = ObjectMapperProvider.mapper

    override suspend fun compute(key: String): ApiOperationResult<CurrentWeather> {
        return try {
            val response = client.get(baseUrl) {
                parameter("q", key)
                parameter("units", "metric")
                parameter("appid", token)
            }
            when (response.status) {
                HttpStatusCode.OK -> {
                    val text = response.bodyAsText()
                    val currentWeather = mapper.readValue<CurrentWeather>(text)
                    ApiOperationResult.Success(currentWeather)
                }
                HttpStatusCode.NotFound -> {
                    logger.warn { "HTTP call failed with status ${response.status}" }
                    ApiOperationResult.NotFound
                }
                else -> {
                    logger.error { "HTTP call failed with status ${response.status}" }
                    ApiOperationResult.Error
                }
            }
        } catch (e: Exception) {
            logger.error(e) { "Failed to retrieve data: ${e.message}" }
            ApiOperationResult.Error
        }
    }

    suspend fun getCurrentWeather(location: String): ApiOperationResult<CurrentWeather> {
        val locationLowercase = location.lowercase()
        logger.debug { "Getting current weather data for '$locationLowercase'" }
        return getOrCompute(locationLowercase)
    }
}