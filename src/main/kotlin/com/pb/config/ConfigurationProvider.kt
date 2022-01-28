package com.pb.config

import com.pb.serialization.ObjectMapperProvider
import java.io.File
import java.io.FileNotFoundException

object ConfigurationProvider {
    private val mapper = ObjectMapperProvider.mapper

    fun getConfiguration(config: File): Configuration {
        if (!config.exists()) throw FileNotFoundException("Configuration file is missing at '${config.path}'")
        return mapper.readValue(config.readText(), Configuration::class.java)
    }
}