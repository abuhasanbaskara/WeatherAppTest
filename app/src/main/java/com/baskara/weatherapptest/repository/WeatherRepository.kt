package com.baskara.weatherapptest.repository

import com.baskara.weatherapptest.api.ApiService
import javax.inject.Inject

class WeatherRepository
@Inject
constructor(private val apiService: ApiService) {
    suspend fun getCurrentWeather(
        lat: Double,
        lon: Double,
        exclude: String,
        appid: String
    ) = apiService.getCurrentWeather(lat, lon, exclude, appid)
}