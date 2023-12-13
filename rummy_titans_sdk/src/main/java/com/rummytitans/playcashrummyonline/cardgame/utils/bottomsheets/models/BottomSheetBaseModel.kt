package com.rummytitans.playcashrummyonline.cardgame.utils.bottomsheets.models

import com.rummytitans.playcashrummyonline.cardgame.R
import androidx.annotation.ColorRes

open class BottomSheetBaseModel( var title:String="",
                                 var description:String="",
                                 var positiveButtonName:String="",
                                 var negativeButtonName:String="",
                                 var imageUrl:String="",
                                 var animationFile:String="",
                                 var imageIcon:Int = 0,
                                 var showNegativeButton:Boolean = false,
                                 var isSuccess:Boolean = false,
                                 var cancelAble:Boolean = false,
                                 @ColorRes var btnColorRes:Int = R.color.turtle_Green)