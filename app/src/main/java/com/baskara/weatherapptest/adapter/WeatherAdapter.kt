package com.baskara.weatherapptest.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.baskara.weatherapptest.R
import com.baskara.weatherapptest.databinding.ItemWeatherListBinding
import com.baskara.weatherapptest.model.WeatherResponse
import com.baskara.weatherapptest.utils.Constants
import com.baskara.weatherapptest.utils.DateUtil
import com.baskara.weatherapptest.utils.TemperatureUtil
import com.bumptech.glide.Glide

class WeatherAdapter(private val context: Context)
    : RecyclerView.Adapter<WeatherAdapter.WeatherViewHolder>() {

    private var weatherLists = ArrayList<WeatherResponse>()
    private lateinit var onItemCallback: OnItemCallback

    fun addData(weatherResponse: WeatherResponse) {
        weatherLists.add(weatherResponse)
        notifyDataSetChanged()
    }

    fun setOnItemCallback(onItemCallback: OnItemCallback) {
        this.onItemCallback = onItemCallback
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): WeatherAdapter.WeatherViewHolder {
        val binding = ItemWeatherListBinding.inflate(LayoutInflater.from(context), parent, false)
        return WeatherViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WeatherAdapter.WeatherViewHolder, position: Int) {
        with(holder.binding){
            val weatherResponse = weatherLists[position]
            tvCurrentDate.text = DateUtil.convertUnixTimestamp(weatherResponse.current.dt)
            tvCurrentStatus.text = buildString {
                append(weatherResponse.current.weather[0].main)
                append(context.getString(R.string.symbol_colon))
                append(context.getString(R.string.symbol_space))
                append(weatherResponse.current.weather[0].description)
            }
            tvCurrentTemp.text = buildString {
                append(TemperatureUtil.kelvinToCelsius(weatherResponse.current.temp))
                append("\u2103")
            }
            tvCurrentFeelsLike.text = buildString {
                append(context.getString(R.string.feels_like))
                append(context.getString(R.string.symbol_space))
                append(TemperatureUtil.kelvinToCelsius(weatherResponse.current.feels_like))
                append(context.getString(R.string.celcius))
            }
            tvCurrentWindSpeed.text = buildString {
                append(context.getString(R.string.wind))
                append(context.getString(R.string.symbol_space))
                append(weatherResponse.current.wind_speed)
                append(context.getString(R.string.ms))
            }
            tvCurrentWindGust.text = buildString {
                append(context.getString(R.string.gust))
                append(context.getString(R.string.symbol_space))
                append(weatherResponse.current.wind_gust)
                append(context.getString(R.string.ms))
            }
            tvCurrentUv.text = buildString {
                append(context.getString(R.string.uv))
                append(weatherResponse.current.uvi)
                append(context.getString(R.string.symbol_space))
                append(context.getString(R.string.ms))
            }
            val url = buildString {
                append(Constants.link_icon)
                append(weatherResponse.current.weather[0].icon)
                append(Constants.icon_size_2x)
            }
            Glide.with(context).load(url).into(ivWeatherIcon)
            tvCurrentLocation.text = weatherResponse.timezone

            when {
                weatherResponse.current.weather[0].description.contains(context.getString(R.string.cloud), ignoreCase = true) -> {
                    layoutItemWeather.setBackgroundColor(ContextCompat.getColor(context, R.color.bg_cloud))
                }
                weatherResponse.current.weather[0].description.contains(context.getString(R.string.clear), ignoreCase = true) -> {
                    layoutItemWeather.setBackgroundColor(ContextCompat.getColor(context, R.color.bg_clear))
                }
                weatherResponse.current.weather[0].description.contains(context.getString(R.string.mist), ignoreCase = true) -> {
                    layoutItemWeather.setBackgroundColor(ContextCompat.getColor(context, R.color.bg_mist))
                }
                weatherResponse.current.weather[0].description.contains(context.getString(R.string.haze), ignoreCase = true) -> {
                    layoutItemWeather.setBackgroundColor(ContextCompat.getColor(context, R.color.bg_haze))
                }
                else -> {
                    layoutItemWeather.setBackgroundColor(ContextCompat.getColor(context, R.color.bg_rain))
                }
            }
        }
    }

    override fun getItemCount(): Int = weatherLists.size

    inner class WeatherViewHolder(val binding: ItemWeatherListBinding) :
            RecyclerView.ViewHolder(binding.root)

    interface OnItemCallback {
        fun onItemClicked(weatherResponse: WeatherResponse)
    }
}