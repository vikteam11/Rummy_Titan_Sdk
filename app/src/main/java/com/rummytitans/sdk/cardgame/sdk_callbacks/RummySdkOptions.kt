package com.rummytitans.sdk.cardgame.sdk_callbacks

import com.rummytitans.sdk.cardgame.utils.MyConstants

data class RummySdkOptions(
    val currentAppType:Int=8,
    val baseUrl :String = MyConstants.APP_CURRENT_URL,
    val baseUrl2 :String = MyConstants.SPLASH_URL,
    val gamePlayUrl :String = MyConstants.GAME_PLAY_URL,
    val gameSplashUrl :String = MyConstants.SPLASH_URL,
    val displayProfileIcon :Boolean = true,
    val showKycOptions :Boolean = true,
    val locationDelay :Long = 0L,
    val fbInstanceId :String = "",
)
