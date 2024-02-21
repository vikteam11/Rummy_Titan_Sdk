package com.rummytitans.sdk.cardgame.sdk_callbacks

import android.app.Activity
import android.content.Intent

interface RummyTitansCallback {
    fun openDeeplink(deeplink:String?)
    fun openProfile(){}
    fun sdkFinish()
    fun logoutUser(){}
    fun redirectToHome(){}
    fun checkForUpdate(){}
    fun onResumeUpdate(){}
    fun onStopUpdate(){}
    fun onUpdateActivityResult(requestCode: Int, resultCode: Int, data: Intent?){}
    fun checkIsAppUpdateAvailable(activity: Activity){}

}