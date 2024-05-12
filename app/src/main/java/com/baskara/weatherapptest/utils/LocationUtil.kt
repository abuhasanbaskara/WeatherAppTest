package com.baskara.weatherapptest.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.util.Locale

object LocationUtil {

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    interface LocationListener {
        fun onLocationAvailable(latitude: Double, longitude: Double, cityName: String?)
        fun onLocationUnavailable()
        fun onLocationFailure(exception: Exception)
    }

    fun getCurrentLocation(context: Context, listener: LocationListener) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    val cityName = getCityName(context, location.latitude, location.longitude)
                    listener.onLocationAvailable(location.latitude, location.longitude, cityName)
                } else {
                    listener.onLocationUnavailable()
                }
            }
            .addOnFailureListener { e ->
                listener.onLocationFailure(e)
            }
    }

    @Suppress("DEPRECATION")
    private fun getCityName(context: Context, latitude: Double, longitude: Double): String? {
        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses = geocoder.getFromLocation(latitude, longitude, 1)
        if (addresses != null) {
            return addresses[0].subAdminArea
        }
        return null
    }
}