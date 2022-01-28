package com.pb.config

data class Configuration(
    val token: String,
    val weatherApiToken: String,
    val admins: List<ULong>,
    val disabledServers: List<ULong>,
)