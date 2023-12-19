package com.rummytitans.sdk.cardgame.sdk_callbacks

import android.content.Intent

interface RummyTitansCallback {
    fun openDeeplink(deeplink:String?)
    fun openProfile()
    fun sdkFinish()
    fun logoutUser()
    fun redirectToHome()

}