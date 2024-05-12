package com.baskara.weatherapptest.model

data class WeatherResponse(
    val lat: Double,
    val lon: Double,
    val timezone: String,
    val timezone_offset: Int,
    val current: CurrentWeather,
    val hourly: ArrayList<HourlyWeather>,
    val daily: ArrayList<DailyWeather>
)
