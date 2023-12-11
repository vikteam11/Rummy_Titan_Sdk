package com.rummytitans.sdk.cardgame.utils.locationservices.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.rummytitans.sdk.cardgame.utils.locationservices.constants.LocationConstants.REQUEST_OPEN_MOBILE_SETTINGS
import com.rummytitans.sdk.cardgame.utils.locationservices.constants.LocationProviderPluginConstants
import android.location.Geocoder
import android.location.LocationManager
import android.provider.Settings
import android.util.Log
import androidx.core.content.ContextCompat
import java.util.*


fun Context.isLocationServiceEnabled(netProvideToo: Boolean = false): Boolean{
    kotlin.runCatching {
        (getSystemService(Context.LOCATION_SERVICE) as LocationManager?)?.let { manager->
            var result = manager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            if (netProvideToo){
                result = result || manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            }
            return result
        }
    }.onFailure {
        Log.e("isLocServiceEnabled", "Exc $it")
    }
    return false
}

fun Context.hasLocationPermissions(): Boolean {
    for (permission in LocationProviderPluginConstants.PERM_LOCATION_ALL) {
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED)
            return false
    }
    return true
}

fun Activity.goToMobileSettings() {
    kotlin.runCatching {
        startActivityForResult(Intent(Settings.ACTION_SETTINGS),REQUEST_OPEN_MOBILE_SETTINGS)
    }
}

fun Context.isLocationEnabled(): Boolean {
    val locationManager = getSystemService(Activity.LOCATION_SERVICE) as LocationManager
    return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
        LocationManager.NETWORK_PROVIDER
    )
}


fun Context.getStateAndSave(lat: Double, log: Double, onLocationFound:(stateName:String , latLog:String)->Unit) {
    runCatching {
        val geocoder = Geocoder(this, Locale.ENGLISH)
        val addresses = geocoder.getFromLocation(lat, log, 1)
        val state = if (addresses.isNullOrEmpty()) ""
        else addresses[0].adminArea
        onLocationFound(state,"$lat,$log")
    }.onFailure {
        onLocationFound("","$lat,$log")
    }
}