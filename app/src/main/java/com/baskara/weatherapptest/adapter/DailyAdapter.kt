package com.baskara.weatherapptest.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.baskara.weatherapptest.R
import com.baskara.weatherapptest.databinding.ItemForecastBinding
import com.baskara.weatherapptest.model.DailyWeather
import com.baskara.weatherapptest.utils.Constants
import com.baskara.weatherapptest.utils.DateUtil
import com.baskara.weatherapptest.utils.TemperatureUtil
import com.bumptech.glide.Glide

class DailyAdapter(private val context: Context)
    : RecyclerView.Adapter<DailyAdapter.DailyViewHolder>(){

    private var dailyLists = ArrayList<DailyWeather>()
    private lateinit var onItemCallback: OnItemCallback

    fun updateAdapter(dailyList: ArrayList<DailyWeather>){
        dailyLists.clear()
        dailyLists.addAll(dailyList)
        notifyDataSetChanged()
    }

    fun setOnItemCallback(onItemCallback: OnItemCallback){
        this.onItemCallback = onItemCallback
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): DailyAdapter.DailyViewHolder {
        val binding = ItemForecastBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DailyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DailyAdapter.DailyViewHolder, position: Int) {
        with(holder.binding){
            val dailyWeather = dailyLists[position]
            tvDate.text = DateUtil.convertUnixTimestampDay(dailyWeather.dt)
            val url = buildString {
                append(Constants.link_icon)
                append(dailyWeather.weather[0].icon)
                append(Constants.icon_size_1x)
            }
            Glide.with(context).load(url).into(ivIcon)
            tvHumid.text = buildString {
                append(dailyWeather.humidity)
                append(context.getString(R.string.percent))
            }
            tvTemperature.text = buildString {
                append(TemperatureUtil.kelvinToCelsius(dailyWeather.temp.min))
                append(context.getString(R.string.degree))
                append(context.getString(R.string.slash))
                append(TemperatureUtil.kelvinToCelsius(dailyWeather.temp.max))
                append(context.getString(R.string.degree))
            }

            layoutDailyWeather.setOnClickListener {
                onItemCallback.onItemClicked(dailyWeather)
            }
        }
    }

    override fun getItemCount(): Int = dailyLists.size

    inner class DailyViewHolder(val binding: ItemForecastBinding) :
            RecyclerView.ViewHolder(binding.root)

    interface OnItemCallback {
        fun onItemClicked(dailyWeather: DailyWeather)
    }
}