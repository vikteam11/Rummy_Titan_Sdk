package com.rummytitans.playcashrummyonline.cardgame.games

data class GameEventModel(
    val redirectionType:String,
    val redirectionParams: GameEventTypeModel,
)

data class GameEventTypeModel(
    val redirectionUrl:String?="",
    val extraParams: GameEventParamModel?
)

data class GameEventParamModel(
    val message:String?,
    val amount:Int?=0
)