package com.baskara.weatherapptest.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.baskara.weatherapptest.R
import com.baskara.weatherapptest.databinding.ItemHourlyBinding
import com.baskara.weatherapptest.model.HourlyWeather
import com.baskara.weatherapptest.utils.Constants
import com.baskara.weatherapptest.utils.DateUtil
import com.baskara.weatherapptest.utils.TemperatureUtil
import com.bumptech.glide.Glide

class HourlyAdapter(private val context: Context)
    : RecyclerView.Adapter<HourlyAdapter.HourlyViewHolder>() {

    private var hourlyLists = ArrayList<HourlyWeather>()
    private lateinit var onItemCallback: OnItemCallback

    fun updateAdapter(newHourlyList: List<HourlyWeather>) {
        hourlyLists.clear()
        hourlyLists.addAll(newHourlyList)
        notifyDataSetChanged()
    }

    fun setOnItemCallback(onItemCallback: OnItemCallback){
        this.onItemCallback = onItemCallback
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): HourlyAdapter.HourlyViewHolder {
        val binding = ItemHourlyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HourlyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HourlyAdapter.HourlyViewHolder, position: Int) {
        with(holder.binding) {
            val hourlyWeather = hourlyLists[position]
            tvTime.text = DateUtil.convertUnixTimestampToHour(hourlyWeather.dt)
            tvTemperature.text = buildString {
                append(TemperatureUtil.kelvinToCelsius(hourlyWeather.temp))
                append(context.getString(R.string.degree))
            }
            val url = buildString {
                append(Constants.link_icon)
                append(hourlyWeather.weather[0].icon)
                append(Constants.icon_size_1x)
            }
            Glide.with(context).load(url).into(ivWeather)

            layoutItemHourly.setOnClickListener {
                onItemCallback.onItemClicked(hourlyWeather)
            }
        }
    }

    override fun getItemCount(): Int = hourlyLists.size

    inner class HourlyViewHolder(val binding: ItemHourlyBinding) :
            RecyclerView.ViewHolder(binding.root)

    interface OnItemCallback {
        fun onItemClicked(hourlyWeather: HourlyWeather)
    }
}