package com.rummytitans.playcashrummyonline.cardgame.utils.locationservices.uiModules

import com.rummytitans.playcashrummyonline.cardgame.ui.base.BaseActivity
import com.rummytitans.playcashrummyonline.cardgame.utils.locationservices.constants.LocationConstants
import com.rummytitans.playcashrummyonline.cardgame.utils.showRestrictLocationDialog
import android.content.Intent


abstract class CurrentLocationBaseActivity : BaseActivity() {
    private var requestLocationCount=0

    fun showAllowBottomSheet(){
        //RequestGPSActivity.startActivityForResultGetLatLong(this)
    }

   abstract fun onLocationFound(lat: Double, log: Double)

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LocationConstants.REQUEST_USER_LOCATION) {
            when (resultCode) {
                LocationConstants.RESPONSE_LOCATION_OK -> {
                    data?.extras?.apply {
                        val latitude =
                            getDouble(
                                LocationConstants.RESPONSE_LATITUDE,
                                LocationConstants.RESPONSE_DEFAULT_LAT_LOG
                            )
                        val logitutde =
                            getDouble(
                                LocationConstants.RESPONSE_LOGITUDE,
                                LocationConstants.RESPONSE_DEFAULT_LAT_LOG
                            )
                        if (latitude != LocationConstants.RESPONSE_DEFAULT_LAT_LOG) {
                            onLocationFound(latitude, logitutde)
                        } else {
                            //Alert user to provideLocation
                            requestLocationCount-=1
                            showAllowBottomSheet()
                        }
                    }
                }

                LocationConstants.RESPONSE__LOCATION_FAILED -> {
                        //showAllowBottomSheet()
                }
            }
        }
    }

    fun onRestrictLocationFound(descriptionMsg: String) {
       showRestrictLocationDialog(descriptionMsg)
    }
}