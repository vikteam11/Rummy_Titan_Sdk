package com.rummytitans.playcashrummyonline.cardgame.ui.payment.viewmodel

import com.rummytitans.playcashrummyonline.cardgame.models.NewPaymentGateWayModel
import com.rummytitans.playcashrummyonline.cardgame.ui.payment.PaymentOptionNavigator
import androidx.databinding.ObservableField

class ItemGatewayDataModel(var data: NewPaymentGateWayModel.PaymentResponseModel, var listner: PaymentOptionNavigator, var themeColor: Int){
    val cvv = ObservableField("")

    fun onCVVChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        cvv.set(s.toString())
    }
}
