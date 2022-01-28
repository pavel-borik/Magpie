package com.pb.serialization

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

object ObjectMapperProvider {
    val mapper by lazy {
        ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, true)
            .registerKotlinModule()
    }
}

inline fun <reified T> ObjectMapper.readValue(value: String): T = readValue(value, T::class.java)