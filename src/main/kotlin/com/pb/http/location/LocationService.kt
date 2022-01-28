package com.pb.http.location

import com.pb.http.client.HttpClientProvider
import com.pb.http.data.HttpCallResult
import com.pb.http.location.data.Location
import com.pb.serialization.ObjectMapperProvider
import com.pb.serialization.readValue
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import mu.KotlinLogging

class LocationService(private val token: String) {
    private val logger = KotlinLogging.logger {}
    private val baseUrl = "http://api.openweathermap.org/geo/1.0/direct"
    private val client = HttpClientProvider.client
    private val mapper = ObjectMapperProvider.mapper

    suspend fun getLocationData(location: String): HttpCallResult<List<Location>> {
        return try {
            val response: HttpResponse = client.get(baseUrl) {
                parameter("q", location)
                parameter("appid", token)
            }
            when (response.status) {
                HttpStatusCode.OK -> {
                    val text = response.readText()
                    val locations = mapper.readValue<Array<Location>>(text).toList()
                    HttpCallResult.Success(locations)
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