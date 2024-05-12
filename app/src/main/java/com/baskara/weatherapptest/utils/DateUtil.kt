package com.baskara.weatherapptest.utils

import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Date
import java.util.Locale

object DateUtil {

    fun convertUnixTimestamp(unixTimestamp: Long): String {
        val instant = Instant.ofEpochSecond(unixTimestamp)
        val dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
        val formatter = DateTimeFormatter.ofPattern("E, d MMMM yyyy", Locale.ENGLISH)
        return dateTime.format(formatter)
    }

    fun convertUnixTimestampToTime(unixTimestamp: Long): String {
        val date = Date(unixTimestamp * 1000L)
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(date)
    }

    fun convertUnixTimestampToHour(unixTimestamp: Long): String {
        val date = Date(unixTimestamp * 1000L)
        val sdf = SimpleDateFormat("HH", Locale.getDefault())
        return sdf.format(date)
    }

    fun convertUnixTimestampDay(unixTimestamp: Long): String {
        val now = LocalDateTime.now()
        val instant = Instant.ofEpochSecond(unixTimestamp)
        val dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())

        return when {
            now.toLocalDate().isEqual(dateTime.toLocalDate()) -> "Today"
            now.minusDays(1).toLocalDate().isEqual(dateTime.toLocalDate()) -> "Yesterday"
            else -> {
                val dayOfWeek = dateTime.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
                dayOfWeek
            }
        }
    }
}