package com.baskara.weatherapptest.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.baskara.weatherapptest.BuildConfig
import com.baskara.weatherapptest.R
import com.baskara.weatherapptest.adapter.DailyAdapter
import com.baskara.weatherapptest.adapter.HourlyAdapter
import com.baskara.weatherapptest.databinding.FragmentHomeBinding
import com.baskara.weatherapptest.model.DailyWeather
import com.baskara.weatherapptest.model.HourlyWeather
import com.baskara.weatherapptest.model.WeatherResponse
import com.baskara.weatherapptest.utils.Constants
import com.baskara.weatherapptest.utils.DateUtil
import com.baskara.weatherapptest.utils.LocationUtil
import com.baskara.weatherapptest.utils.NetworkUtil
import com.baskara.weatherapptest.utils.TemperatureUtil
import com.baskara.weatherapptest.viewmodel.WeatherViewModel
import com.bumptech.glide.Glide
import com.github.matteobattilana.weather.PrecipType
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private val binding by lazy { FragmentHomeBinding.inflate(layoutInflater) }
    private val viewModel: WeatherViewModel by viewModels()
    private lateinit var hourlyAdapter: HourlyAdapter
    private lateinit var dailyAdapter: DailyAdapter
    val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "weather_data")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkLocationPermission()
        initHourlyRecyclerView()
        initDailyRecyclerView()
        observeViewModel()
        setClickableView()
        observeWeatherResponse(binding.root.context)
    }

    private fun setClickableView(){
        binding.layoutWeatherMain.apply {
            ivBack.setOnClickListener {
                activity?.finish()
            }
            ivList.setOnClickListener {
                findNavController().navigate(R.id.action_homeFragment_to_weatherListFragment)
            }
        }
    }

    private fun initHourlyRecyclerView(){
        binding.layoutWeatherMain.apply {
            hourlyAdapter = HourlyAdapter(binding.root.context)
            rvHourly.setHasFixedSize(true)
            rvHourly.layoutManager = LinearLayoutManager(
                binding.root.context,
                LinearLayoutManager.HORIZONTAL,
                false)
            rvHourly.adapter = hourlyAdapter

            hourlyAdapter.setOnItemCallback(object : HourlyAdapter.OnItemCallback {
                override fun onItemClicked(hourlyWeather: HourlyWeather) {
                    setBackground(hourlyWeather.weather[0].description)
                    changeWeather(hourlyWeather.weather[0].description)
                }
            })
        }
    }

    private fun initDailyRecyclerView(){
        binding.layoutWeatherMain.apply {
            dailyAdapter = DailyAdapter(binding.root.context)
            rvDaily.setHasFixedSize(true)
            rvDaily.layoutManager = LinearLayoutManager(
                binding.root.context,
                LinearLayoutManager.VERTICAL,
                false)
            rvDaily.adapter = dailyAdapter

            dailyAdapter.setOnItemCallback(object : DailyAdapter.OnItemCallback{
                override fun onItemClicked(dailyWeather: DailyWeather) {
                    setBackground(dailyWeather.weather[0].description)
                    changeWeather(dailyWeather.weather[0].description)
                }
            })
        }
    }

    private fun observeViewModel(){
        viewModel.currentWeatherResponse.observe(viewLifecycleOwner){ response ->
            if (response.current.dt >= viewModel.tempTimestamp){
                showWeather(response)
                viewModel.tempTimestamp = response.current.dt
            }
        }
    }

    private fun saveWeatherResponseAsync(context: Context, weatherResponse: WeatherResponse) {
        CoroutineScope(Dispatchers.IO).launch {
            saveWeatherResponse(context, weatherResponse)
        }
    }

    private fun observeWeatherResponse(context: Context) {
        viewLifecycleOwner.lifecycleScope.launch {
            getWeatherResponse(context).collect { weatherResponse ->
                weatherResponse?.let {
                    if (weatherResponse.current.dt >= viewModel.tempTimestamp){
                        showWeather(weatherResponse)
                        viewModel.tempTimestamp = weatherResponse.current.dt
                    }
                }
            }
        }
    }

    private fun showWeather(weatherResponse: WeatherResponse){
        setCurrentView(weatherResponse)
        hourlyAdapter.updateAdapter(weatherResponse.hourly)
        dailyAdapter.updateAdapter(weatherResponse.daily)
        setBackground(weatherResponse.current.weather[0].description)
        changeWeather(weatherResponse.current.weather[0].description)
        saveWeatherResponseAsync(binding.root.context, weatherResponse)
    }

    private fun setBackground(description: String){
        binding.layoutWeatherMain.apply {
            when {
                description.contains(getString(R.string.cloud), ignoreCase = true) -> {
                    parentWeatherDetail.setBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.bg_cloud))
                    cardMain.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(binding.root.context, R.color.bg_opacity_cloud))
                    cardForecast.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(binding.root.context, R.color.bg_opacity_cloud))
                }
                description.contains(getString(R.string.clear), ignoreCase = true) -> {
                    parentWeatherDetail.setBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.bg_clear))
                    cardMain.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(binding.root.context, R.color.bg_opacity_clear))
                    cardForecast.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(binding.root.context, R.color.bg_opacity_clear))
                }
                description.contains(getString(R.string.mist), ignoreCase = true) -> {
                    parentWeatherDetail.setBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.bg_mist))
                    cardMain.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(binding.root.context, R.color.bg_opacity_mist))
                    cardForecast.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(binding.root.context, R.color.bg_opacity_mist))
                }
                description.contains(getString(R.string.haze), ignoreCase = true) -> {
                    parentWeatherDetail.setBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.bg_haze))
                    cardMain.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(binding.root.context, R.color.bg_opacity_haze))
                    cardForecast.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(binding.root.context, R.color.bg_opacity_haze))
                }
                else -> {
                    parentWeatherDetail.setBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.bg_rain))
                    cardMain.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(binding.root.context, R.color.bg_opacity_rain))
                    cardForecast.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(binding.root.context, R.color.bg_opacity_rain))
                }
            }
        }
    }

    private fun changeWeather(description: String){
        binding.layoutWeatherMain.apply {
            var weather: PrecipType? = null
            var weatherSpeed = 0
            var weatherParticles = 0f

            when {
                description.contains(getString(R.string.clear), ignoreCase = true) -> {
                    weather = PrecipType.CLEAR
                }
                description.contains(getString(R.string.snow), ignoreCase = true) -> {
                    weather = PrecipType.SNOW
                    weatherParticles = 60f
                    weatherSpeed = 200
                }
                description.contains(getString(R.string.rain), ignoreCase = true) -> {
                    weather = PrecipType.RAIN
                    weatherParticles = 100f
                    weatherSpeed = 600
                }
            }

            wvWeatherView.apply {
                weather?.let { setWeatherData(it) }
                speed = weatherSpeed // How fast
                emissionRate = weatherParticles // How many particles
                angle = 0 // The angle of the fall
                fadeOutPercent = .75f // When to fade out (0.0f-1.0f)
            }
        }
    }

    private fun setCurrentView(weatherResponse: WeatherResponse){
        binding.layoutWeatherMain.apply {
            tvCurrentDate.text = DateUtil.convertUnixTimestamp(weatherResponse.current.dt)
            tvCurrentStatus.text = buildString {
                append(weatherResponse.current.weather[0].main)
                append(getString(R.string.symbol_colon))
                append(getString(R.string.symbol_space))
                append(weatherResponse.current.weather[0].description)
            }
            tvCurrentTemp.text = buildString {
                append(TemperatureUtil.kelvinToCelsius(weatherResponse.current.temp))
                append("\u2103")
            }
            tvCurrentFeelsLike.text = buildString {
                append(getString(R.string.feels_like))
                append(getString(R.string.symbol_space))
                append(TemperatureUtil.kelvinToCelsius(weatherResponse.current.feels_like))
                append(getString(R.string.celcius))
            }
            tvCurrentWindSpeed.text = buildString {
                append(getString(R.string.wind))
                append(getString(R.string.symbol_space))
                append(weatherResponse.current.wind_speed)
                append(getString(R.string.ms))
            }
            tvCurrentWindGust.text = buildString {
                append(getString(R.string.gust))
                append(getString(R.string.symbol_space))
                append(weatherResponse.current.wind_gust)
                append(getString(R.string.ms))
            }
            tvCurrentUv.text = buildString {
                append(getString(R.string.uv))
                append(weatherResponse.current.uvi)
                append(getString(R.string.symbol_space))
                append(getString(R.string.ms))
            }
            val url = buildString {
                append(Constants.link_icon)
                append(weatherResponse.current.weather[0].icon)
                append(Constants.icon_size_2x)
            }
            Glide.with(binding.root.context).load(url).into(ivWeatherIcon)
            tvCurrentPressure.text = buildString {
                append(getString(R.string.press))
                append(getString(R.string.symbol_space))
                append(weatherResponse.current.pressure)
                append(getString(R.string.hpa))
            }
            tvCurrentHumidity.text = buildString {
                append(getString(R.string.humid))
                append(getString(R.string.symbol_space))
                append(weatherResponse.current.humidity)
                append(getString(R.string.percent))
            }
            tvCurrentDewpoint.text = buildString {
                append(getString(R.string.dew))
                append(getString(R.string.symbol_space))
                append(TemperatureUtil.kelvinToCelsius(weatherResponse.current.dew_point))
                append(getString(R.string.celcius))
            }
            tvCurrentSunsire.text = buildString {
                append(getString(R.string.sunrise))
                append(getString(R.string.symbol_space))
                append(DateUtil.convertUnixTimestampToTime(weatherResponse.current.sunrise))
            }
            tvCurrentSunset.text = buildString {
                append(getString(R.string.sunset))
                append(getString(R.string.symbol_space))
                append(DateUtil.convertUnixTimestampToTime(weatherResponse.current.sunset))
            }
        }
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation()
        } else {
            requestLocationPermission()
        }
    }

    private val locationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true &&
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            getCurrentLocation()
        } else {
            Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    private fun requestLocationPermission() {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        locationPermissionLauncher.launch(permissions)
    }

    private fun getCurrentLocation(){
        if (NetworkUtil.isNetworkAvailable(binding.root.context)) {
            LocationUtil.getCurrentLocation(binding.root.context, object : LocationUtil.LocationListener {
                override fun onLocationAvailable(latitude: Double, longitude: Double, cityName: String?) {
                    binding.layoutWeatherMain.tvCurrentLocation.text = cityName
                    viewModel.getCurrentWeather(latitude, longitude, Constants.exclude, BuildConfig.WEATHER_API_KEY)
                }

                override fun onLocationUnavailable() {
                    Toast.makeText(requireContext(), "Location is unavailable", Toast.LENGTH_SHORT).show()
                }

                override fun onLocationFailure(exception: Exception) {
                    Toast.makeText(requireContext(), "Failed to get location", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Toast.makeText(binding.root.context, getString(R.string.no_internet), Toast.LENGTH_SHORT).show()
        }
    }

    suspend fun saveWeatherResponse(context: Context, weatherResponse: WeatherResponse) {
        val gson = Gson()
        val dataStoreKey = stringPreferencesKey("weatherResponse")
        val json = gson.toJson(weatherResponse)
        context.dataStore.edit { preferences ->
            preferences[dataStoreKey] = json
        }
    }

    private fun getWeatherResponse(context: Context): Flow<WeatherResponse?> {
        val gson = Gson()
        val dataStoreKey = stringPreferencesKey("weatherResponse")
        return context.dataStore.data.map { preferences ->
            val json = preferences[dataStoreKey] ?: return@map null
            gson.fromJson(json, WeatherResponse::class.java)
        }
    }
}