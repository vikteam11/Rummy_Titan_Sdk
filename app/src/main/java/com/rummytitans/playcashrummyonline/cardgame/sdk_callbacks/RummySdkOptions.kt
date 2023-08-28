package com.rummytitans.playcashrummyonline.cardgame.sdk_callbacks

import com.rummytitans.playcashrummyonline.cardgame.utils.MyConstants

data class RummySdkOptions(
    val currentAppType:Int=8,
    val baseUrl :String = MyConstants.PRODUCTION_URL,
    val baseUrl2 :String = "https://api.myteam11.com",
    val gamePlayUrl :String = MyConstants.GAME_PLAY_URL,
    val gameSplashUrl :String = MyConstants.SPLASH_URL,
    val displayProfileIcon :Boolean = false,
    val showKycOptions :Boolean = false,
)
