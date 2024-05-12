package com.baskara.weatherapptest.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.baskara.weatherapptest.BuildConfig
import com.baskara.weatherapptest.R
import com.baskara.weatherapptest.adapter.WeatherAdapter
import com.baskara.weatherapptest.databinding.FragmentWeatherListBinding
import com.baskara.weatherapptest.model.Location
import com.baskara.weatherapptest.model.WeatherResponse
import com.baskara.weatherapptest.utils.Constants
import com.baskara.weatherapptest.utils.NetworkUtil
import com.baskara.weatherapptest.viewmodel.WeatherViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WeatherListFragment : Fragment() {

    private val binding by lazy { FragmentWeatherListBinding.inflate(layoutInflater) }
    private val viewModel: WeatherViewModel by viewModels()
    private lateinit var adapter: WeatherAdapter
    private val locations = listOf(
        Location("New York", 40.7128, -74.0060),
        Location("Singapore", 1.3521, 103.8198),
        Location("Mumbai", 19.0760, 72.8777),
        Location("Delhi", 28.7041, 77.1025),
        Location("Sydney", -33.8688, 151.2093),
        Location("Melbourne", -37.8136, 144.9631)
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRecyclerView()
        callApi()
        observerViewModel()
        setClickacbleView()
    }

    private fun initRecyclerView() {
        binding.apply {
            adapter = WeatherAdapter(binding.root.context)
            rvWeather.setHasFixedSize(true)
            rvWeather.layoutManager = LinearLayoutManager(
                binding.root.context,
                LinearLayoutManager.VERTICAL,
                false
            )
            rvWeather.adapter = adapter

            adapter.setOnItemCallback(object : WeatherAdapter.OnItemCallback{
                override fun onItemClicked(weatherResponse: WeatherResponse) {
                    // do something
                }
            })
        }
    }

    private fun callApi() {
        if (NetworkUtil.isNetworkAvailable(binding.root.context)) {
            CoroutineScope(Dispatchers.IO).launch {
                for (location in locations) {
                    val weatherDeferred = async(Dispatchers.IO) {
                        viewModel.getCurrentWeather(location.latitude, location.longitude, Constants.exclude, BuildConfig.WEATHER_API_KEY)
                    }
                    weatherDeferred.await()
                    delay(1000)
                }

            }
        } else {
            Toast.makeText(binding.root.context, getString(R.string.no_internet), Toast.LENGTH_SHORT).show()
        }
    }

    private fun observerViewModel(){
        viewModel.currentWeatherResponse.observe(viewLifecycleOwner){ response ->
            adapter.addData(response)
        }
    }

    private fun setClickacbleView(){
        binding.apply {
            ivBack.setOnClickListener {
                findNavController().navigateUp()
            }
        }
    }

}