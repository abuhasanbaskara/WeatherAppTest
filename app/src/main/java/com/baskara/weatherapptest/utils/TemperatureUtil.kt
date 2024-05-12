package com.baskara.weatherapptest.utils

object TemperatureUtil {

    fun kelvinToCelsius(kelvin: Double): String {
        val celsius = kelvin - 273.15
        return String.format("%.1f", celsius)
    }

}