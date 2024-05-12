package com.baskara.weatherapptest.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.baskara.weatherapptest.model.CurrentWeather
import com.baskara.weatherapptest.model.WeatherResponse
import com.baskara.weatherapptest.repository.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val repository: WeatherRepository
): ViewModel() {

    private val _currentWeatherResponse = MutableLiveData<WeatherResponse>()
    val currentWeatherResponse: LiveData<WeatherResponse>
        get() = _currentWeatherResponse

    var tempTimestamp = 0L

    fun getCurrentWeather(
        lat: Double,
        lon: Double,
        excclude: String,
        apiKey: String) = viewModelScope.launch {
            repository.getCurrentWeather(lat, lon, excclude, apiKey).let { response ->
                if (response.isSuccessful) {
                    _currentWeatherResponse.postValue(response.body())
                } else {
                    println("Error: ${response.code()}")
                }
            }
    }

}