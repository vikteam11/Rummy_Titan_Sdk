package com.rummytitans.playcashrummyonline.cardgame.sdk_callbacks

import com.rummytitans.playcashrummyonline.cardgame.utils.MyConstants

data class RummySdkOptions(
    val currentAppType:Int=8,
    val baseUrl :String = MyConstants.PRODUCTION_URL,
    val gamePlayUrl :String = MyConstants.GAME_PLAY_URL,
    val displayProfileIcon :Boolean = false,
)
