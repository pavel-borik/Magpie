package com.pb.http.client

import com.pb.registerShutdownHookWithLogger
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*

object HttpClientProvider {
    val client by lazy {
        HttpClient(CIO) {
            expectSuccess = false
            install(JsonFeature)
        }
    }

    init {
        registerShutdownHookWithLogger { logger ->
            logger.info { "Going to close HttpClient" }
            client.close()
        }
    }
}