package com.rummytitans.sdk.cardgame.ui.refer.viewmodel

import com.rummytitans.sdk.cardgame.models.ReferModel
import androidx.databinding.ObservableBoolean
import kotlin.math.roundToInt

class ItemReferModel(var data: ReferModel.Response, var themeColor: String) {


    //custom key user to hide show view
    var isViewHide = ObservableBoolean(true)

    fun getProgressPrecentage(): Int {
        return ((data.Amount.toFloat() / data.MaxAmount.toFloat()) * 100).roundToInt()
    }
}
