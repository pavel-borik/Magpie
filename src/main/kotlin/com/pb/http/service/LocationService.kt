package com.pb.http.service

import com.pb.http.client.HttpClientProvider
import com.pb.http.data.ApiOperationResult
import com.pb.http.data.Location
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

    suspend fun getLocationData(location: String): ApiOperationResult<List<Location>> {
        return try {
            val response: HttpResponse = client.get(baseUrl) {
                parameter("q", location)
                parameter("appid", token)
            }
            when (response.status) {
                HttpStatusCode.OK -> {
                    val text = response.bodyAsText()
                    val locations = mapper.readValue<Array<Location>>(text).toList()
                    ApiOperationResult.Success(locations)
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
}