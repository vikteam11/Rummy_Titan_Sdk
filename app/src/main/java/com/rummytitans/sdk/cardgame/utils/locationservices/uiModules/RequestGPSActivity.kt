package com.rummytitans.sdk.cardgame.utils.locationservices.uiModules

import com.rummytitans.sdk.cardgame.R
import com.rummytitans.sdk.cardgame.utils.locationservices.constants.LocationConstants
import com.rummytitans.sdk.cardgame.utils.locationservices.constants.LocationConstants.REQUEST_OPEN_MOBILE_SETTINGS
import com.rummytitans.sdk.cardgame.utils.locationservices.constants.LocationConstants.REQUEST_PERMISSION_LOCATION
import com.rummytitans.sdk.cardgame.utils.locationservices.constants.LocationConstants.REQUEST_USER_LOCATION
import com.rummytitans.sdk.cardgame.utils.locationservices.constants.LocationConstants.RESPONSE_LATITUDE
import com.rummytitans.sdk.cardgame.utils.locationservices.constants.LocationConstants.RESPONSE_LOCATION_OK
import com.rummytitans.sdk.cardgame.utils.locationservices.constants.LocationConstants.RESPONSE_LOGITUDE
import com.rummytitans.sdk.cardgame.utils.locationservices.constants.LocationConstants.RESPONSE__LOCATION_FAILED
import com.rummytitans.sdk.cardgame.utils.locationservices.constants.LocationProviderPluginConstants
import com.rummytitans.sdk.cardgame.utils.locationservices.models.ModelPermission
import com.rummytitans.sdk.cardgame.utils.locationservices.utils.*
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.Keep
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*

class RequestGPSActivity : Activity() {

    companion object {
        private const val TAG = "RequestLocationActivity"
        private const val EXTRA_KEY_IS_MANDATORY = "isMandatory"

        @JvmStatic
        @Keep
        fun startActivityForResultGetLatLong(
            activity: Activity
        ) {
            activity.startActivityForResult(
                Intent(activity, RequestGPSActivity::class.java)
                    .putExtra(EXTRA_KEY_IS_MANDATORY, true),
                REQUEST_USER_LOCATION
            )
        }
    }

    private var alertDialog: AlertDialog? = null
    private var isMandatory = false
    private val REQUEST_LIMIT = 2
    private val EMPTY_REQUEST = 0
    private var requestForAppPermission = REQUEST_LIMIT
    private var requestForMobileGPS = REQUEST_LIMIT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.request_location_rummy)
        isMandatory = intent.getBooleanExtra(EXTRA_KEY_IS_MANDATORY, false)
        showProgressBar()
        initRequests()
    }

    private fun hideProgressBar(){
        findViewById<View>(R.id.progressBar)?.visibility=View.GONE
    }
    private fun showProgressBar(){
        findViewById<View>(R.id.progressBar)?.visibility=View.VISIBLE
    }

    private fun initRequests() {
        if (requestForMobileGPS != EMPTY_REQUEST) {
            requestForMobileGPS -= 1
            if (requestForAppPermission != EMPTY_REQUEST) {
                requestForAppPermission -= 1
                checkMobileLocationServiceStatus()
            } else {
                onFailedExit()
            }
        } else
            onFailedExit()
    }

    private fun checkMobileLocationServiceStatus() {
        if (isLocationServiceEnabled(true)) {
            requestAppPermissions()
        /*if (!hasLocationPermissions()) {
                Log.e(TAG, "request grant permission")
                requestAppPermissions()
            } else {
                Log.e(TAG, "gps enabled location enabled get location")
                provideLocation()
            }*/
        } else
            requestMobileGPS()
    }

    private fun provideLocation() {
        showProgressBar()
        LocationProviderRummy(this,
            mainLooper,
            { onSuccessSendLocation(it) },
            { onFailedExit() }
        ).requestLocationAsync()
    }

    private fun requestMobileGPS() {
        Log.e(TAG, "request GPS on")
        kotlin.runCatching {
            val locationRequest = LocationRequest.create()
            locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            val builder = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)

            LocationServices.getSettingsClient(this)
                .checkLocationSettings(builder.build()).addOnCompleteListener {
                    try {
                        val response: LocationSettingsResponse =
                            it.getResult(ApiException::class.java)
                    } catch (e: ApiException) {
                        when (e.statusCode) {
                            LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                                try {
                                    val resolvable: ResolvableApiException =
                                        e as ResolvableApiException
                                    // Show the dialog by calling startResolutionForResult(),
                                    // and check the result in onActivityResult().
                                    resolvable.startResolutionForResult(
                                        this,
                                        LocationRequest.PRIORITY_HIGH_ACCURACY
                                    )
                                } catch (e: IntentSender.SendIntentException) { /*Ignore*/
                                } catch (e: Exception) {
                                    alertDialog(
                                        false,
                                        LocationProviderPluginConstants.DIALOG_MSG_GPS_SETTING,
                                        "Go To Settings",
                                        "Cancel"
                                    )
                                }
                            }
                            LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                                // Location settings are not satisfied. However, we have no way to fix the
                                // settings so we won't show the dialog.
                                alertDialog(
                                    false, LocationProviderPluginConstants.DIALOG_MSG_GPS_SETTING,
                                    "Go To Settings", "Cancel"
                                )
                            }
                        }
                    }
                }
        }.onFailure {
            alertDialog(
                false, LocationProviderPluginConstants.DIALOG_MSG_GPS_SETTING,
                "Go To Settings", "Cancel"
            )
        }
    }

    private fun requestAppPermissions() {
        hideProgressBar()
        RequestPermissionActivity.startActivityForPermissionWithResult(
            this, listOf(
                ModelPermission(
                    LocationProviderPluginConstants.PERM_ACCESS_COARSE_LOCATION,
                    LocationProviderPluginConstants.PERM_ACCESS_COARSE_LOCATION,
                    false,
                    isMandatory
                ),
                ModelPermission(
                    LocationProviderPluginConstants.PERM_ACCESS_FINE_LOCATION,
                    LocationProviderPluginConstants.PERM_ACCESS_FINE_LOCATION,
                    false,
                    isMandatory
                )
            ), REQUEST_PERMISSION_LOCATION
        )
    }

    private fun onSuccessSendLocation(location: Location?) {
        Log.e(TAG, "${location?.latitude} ${location?.latitude}")
        onLocationReceived(
            location?.latitude, location?.longitude,
            RESPONSE_LOCATION_OK
        )
    }

    private fun onFailedExit() {
        dismissDialog()
        Log.e(TAG, "failed to getlocation")
        onLocationReceived(
            LocationConstants.RESPONSE_DEFAULT_LAT_LOG,
            LocationConstants.RESPONSE_DEFAULT_LAT_LOG, RESPONSE__LOCATION_FAILED
        )
    }

    fun onLocationReceived(latitude: Double?, longitude: Double?, responseCode: Int) {
        hideProgressBar()
        val intent =
            Intent().putExtra(RESPONSE_LATITUDE, latitude).putExtra(RESPONSE_LOGITUDE, longitude)
        setResult(responseCode, intent)
        finish()
    }

    private fun alertDialog(
        reqDialog: Boolean, msg: String, posTxt: String,
        negTxt: String? = null, finishProcess: Boolean = false
    ) {
        dismissDialog()
        AlertDialog.Builder(this).let { builder ->
            builder.setMessage(msg)
                .setCancelable(false)
                .setPositiveButton(posTxt) { dialog, _ ->
                    dialog.dismiss()
                    if (finishProcess) {
                        onFailedExit()
                        return@setPositiveButton
                    }
                    if (reqDialog) initRequests()
                    else {
                        goToMobileSettings()
                        onFailedExit()
                    }
                }
            if (!negTxt.isNullOrBlank()) {
                builder.setNegativeButton(negTxt) { dialog, _ ->
                    onFailedExit()
                    dialog.dismiss()
                    finish()
                }
            }
            alertDialog = builder.create()
            alertDialog?.show()
        }
    }

    private fun dismissDialog() {
        alertDialog?.let { dialog ->
            if (dialog.isShowing) dialog.dismiss()
            alertDialog = null
        }
    }

    private fun alertUser() {
        data class DialogTypeModel(
            val alertMessage: String, val finishProcess: Boolean, val posTxt: String
        )

         if (requestForMobileGPS == EMPTY_REQUEST)
            onFailedExit()
        else
            DialogTypeModel(LocationProviderPluginConstants.DIALOG_MSG_GPS_REQUEST, false, "Ok").apply {
            alertDialog(true, alertMessage, posTxt, finishProcess = finishProcess)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.e(TAG, "on activity result")
        when (requestCode) {
            LocationRequest.PRIORITY_HIGH_ACCURACY -> when (resultCode) {
                RESULT_OK -> requestAppPermissions()
                else -> {
                    if (isMandatory) alertUser()
                    else onFailedExit()
                }
            }
            REQUEST_PERMISSION_LOCATION -> when (resultCode) {
                RESPONSE_LOCATION_OK -> provideLocation()
                RESPONSE__LOCATION_FAILED -> onFailedExit()
            }
            REQUEST_OPEN_MOBILE_SETTINGS -> initRequests()
        }
    }

}
