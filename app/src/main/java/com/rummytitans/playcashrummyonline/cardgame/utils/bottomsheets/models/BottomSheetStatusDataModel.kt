package com.rummytitans.playcashrummyonline.cardgame.utils.bottomsheets.models

import com.rummytitans.playcashrummyonline.cardgame.R
import com.rummytitans.playcashrummyonline.cardgame.utils.bottomsheets.models.BottomSheetBaseModel

class BottomSheetStatusDataModel(
    var animationFileId:Int = 0,
    isSuccess:Boolean = false,
    var loading:Boolean = false,
    var allowCross:Boolean=true,
    btnColorRes:Int = R.color.turtle_Green,
    var showSuccessAnim:Int = 0,

    ): BottomSheetBaseModel(isSuccess=isSuccess,btnColorRes=btnColorRes)
