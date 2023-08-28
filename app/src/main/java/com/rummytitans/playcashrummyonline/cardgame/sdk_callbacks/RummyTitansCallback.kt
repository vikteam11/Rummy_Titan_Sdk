package com.rummytitans.playcashrummyonline.cardgame.sdk_callbacks

interface RummyTitansCallback {
    fun openDeeplink(deeplink:String?)
    fun openProfile()
    fun sdkFinish()
    fun logoutUser()
    fun redirectToHome()
}