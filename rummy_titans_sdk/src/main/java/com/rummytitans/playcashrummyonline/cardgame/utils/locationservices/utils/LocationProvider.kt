package com.rummytitans.playcashrummyonline.cardgame.utils.locationservices.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task

const val TAG="LOCATION_PROVIDER"
class LocationProvider(
    mContext: Context, private val mainLooper: Looper,
    private val onSuccess: (result: Location?) -> Unit, private val onFailed: () -> Unit
) {
    private val LocationClient:FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(mContext)

    @SuppressLint("MissingPermission")
    fun requestLocationAsync() {
        Log.e(TAG, "getLastLocation requestLocationAsync ")
        kotlin.runCatching {
            LocationClient.lastLocation.addOnCompleteListener {
                val result: Task<Location>? = it
                if (it.isSuccessful && result?.result!=null)
                    onSuccess(it.result)
                else
                    getCurrentLocation()
            }.addOnFailureListener {
                Log.e(TAG, "getLastLocation OnFailure $it")
                getCurrentLocation()
            }
        }.onFailure {
            Log.e(TAG, "getLastLocation Exc $it")
            getCurrentLocation()
        }
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        Log.e(TAG, "get current location")
        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 1 * 1000
        locationRequest.fastestInterval = 0
        locationRequest.numUpdates = 1

        /*
        * If we are get any response in 8 sec then exit*/
        val handler = Handler(mainLooper)
        val callback = { onFailed() }
        handler.postDelayed(callback,8*1000)

        LocationClient.requestLocationUpdates(locationRequest, object : LocationCallback() {

            override fun onLocationResult(p0: LocationResult) {
                for (location in p0.locations) {
                    handler.removeCallbacks(callback)
                    if (location != null) {
                        LocationClient.removeLocationUpdates(this)
                        onSuccess(location)
                    } else {
                        onFailed()
                    }
                }
            }
        }, mainLooper)
    }
}