package com.rummytitans.playcashrummyonline.cardgame.ui.profile.verify

import android.graphics.Color
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes

data class ProfileVerificationItem(
    val type:Int=0,
    val title:String? = "",
    val value:String? = "",
    val selectedColor:String? = "",
    val isVerified:Boolean=false,
    val isDeleteAble:Boolean = false,
    @DrawableRes val icon:Int = 0,
    @ColorRes val textColor:Int = 0,
    @ColorRes val verifyColor: Int=0,
    @IdRes val buttonId:Int=0
    )
