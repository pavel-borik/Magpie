package com.pb.http.weather

import com.pb.http.client.HttpClientProvider
import com.pb.http.data.HttpCallResult
import com.pb.http.weather.data.CurrentWeather
import com.pb.serialization.ObjectMapperProvider
import com.pb.serialization.readValue
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import mu.KotlinLogging

class WeatherService(private val token: String) {
    private val logger = KotlinLogging.logger {}
    private val baseUrl = "http://api.openweathermap.org/data/2.5/weather"
    private val client = HttpClientProvider.client
    private val mapper = ObjectMapperProvider.mapper

    suspend fun getCurrentWeather(location: String): HttpCallResult<CurrentWeather> {
        return try {
            val response: HttpResponse = client.get(baseUrl) {
                parameter("q", location)
                parameter("units", "metric")
                parameter("appid", token)
            }
            when (response.status) {
                HttpStatusCode.OK -> {
                    val text = response.bodyAsText()
                    val currentWeather = mapper.readValue<CurrentWeather>(text)
                    HttpCallResult.Success(currentWeather)
                }
                HttpStatusCode.NotFound -> {
                    logger.warn { "HTTP call failed with status ${response.status}" }
                    HttpCallResult.NotFound
                }
                else -> {
                    logger.error { "HTTP call failed with status ${response.status}" }
                    HttpCallResult.Error
                }
            }
        } catch (e: Exception) {
            logger.error(e) { "Failed to retrieve data: ${e.message}" }
            HttpCallResult.Error
        }
    }
}