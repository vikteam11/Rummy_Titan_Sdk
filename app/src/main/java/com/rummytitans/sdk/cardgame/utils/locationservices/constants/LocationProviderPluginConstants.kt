package com.rummytitans.sdk.cardgame.utils.locationservices.constants

import android.Manifest
import android.app.Activity

object LocationProviderPluginConstants {
    const val LOCATION_PERMISSION_REQ_CODE = 801
    const val RESOLUTION_FOR_RESULT_REQ_CODE = 802

    const val EXTRA_REQUEST_ONLY_FINE = "OnlyFineLoc"
    const val EXTRA_IS_LOC_PERM_OPTIONAL = "IsLocPermOpt"

    const val PERM_ACCESS_FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION
    const val PERM_ACCESS_COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION
    val PERM_LOCATION_ALL = arrayOf(PERM_ACCESS_FINE_LOCATION, PERM_ACCESS_COARSE_LOCATION)

    const val LOCATION_REQ_INTERVAL = 10000L
    const val LOCATION_REQ_FASTEST_INTERVAL = 5000L


    const val DIALOG_MSG_GPS_SETTING =
        "Location is not enabled on your device, please turn on location from settings :"
    const val DIALOG_MSG_GPS_REQUEST = "Please enable device location to play game."
    const val DIALOG_MSG_NOT_ALLOW = "Please enable device location to play game."


}

object LocationConstants {
    const val RESPONSE_LOCATION_OK = Activity.RESULT_OK
    const val RESPONSE__LOCATION_FAILED = 1001
    const val REQUEST_PERMISSION_LOCATION = 123123
    const val REQUEST_USER_LOCATION = 222
    const val REQUEST_OPEN_MOBILE_SETTINGS = 333
    const val REQUEST_OPEN_APP_SETTINGS = 444
    const val RESPONSE_LATITUDE = "LATITUDE"
    const val RESPONSE_LOGITUDE = "LOGITUDE"
    const val RESPONSE_DEFAULT_LAT_LOG = -1.0
}