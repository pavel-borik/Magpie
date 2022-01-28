package com.pb.http.weather.data

data class CurrentWeather(
    val weather: List<Weather>,
    val main: Main,
    val wind: Wind,
    val sys: Sys,
    val name: String,
    val timezone: Long,
)

data class Main(
    val temp: Double,
    val humidity: Int,
)

data class Weather(
    val id: Int,
    val main: String,
    val description: String,
    val icon: String,
) {
    fun toIcon(): String {
        return when (id) {
            in 200..202 -> "⛈"
            in 210..221 -> "🌩"
            in 230..232 -> "⛈"
            in 300..321 -> "🌧"
            in 500..504 -> "🌦"
            in 511..531 -> "🌧"
            in 600..622 -> "🌨"
            in 701..771 -> "🌫"
            781 -> "🌪"
            800 -> "☀"
            801 -> "🌤"
            802 -> "⛅"
            803 -> "🌥"
            804 -> "☁"
            else -> ""
        }
    }
}

data class Wind(
    val speed: Double,
    val deg: Long,
)

data class Sys(
    val country: String,
    val sunrise: Long,
    val sunset: Long,
)
