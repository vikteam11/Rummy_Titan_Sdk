package com.rummytitans.sdk.cardgame.ui.verifications

data class BottomSheetDataModel(
    val title:String="",
    val description:String="",
    val positiveButtonName:String="",
    val negativeButtonName:String="",
    val imageUrl:String="",
    val animationFile:String="",
    val imageIcon:Int = 0,
    val animationFileId:Int = 0,
    val showNegativeButton:Boolean = false,
    val status:Boolean = false,
    val loading:Boolean = false,
    val cancelAble:Boolean = false,
)
