package com.pb.http.location.data

data class LocationData(
    val locations: List<Location>,
)

data class Location(
    val name: String,
    val country: String,
    val state: String?,
)