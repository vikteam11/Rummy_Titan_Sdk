package com.rummytitans.playcashrummyonline.cardgame.ui.wallet.model

import com.rummytitans.playcashrummyonline.cardgame.data.SharedPreferenceStorageRummy
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField


class WalletRedeemCodeModel(prefs:SharedPreferenceStorageRummy) {
    var availableCode= ObservableField<String>()
    var regularColor = prefs.regularColor
    var safeColor = prefs.safeColor
    val selectedColor = ObservableField(if (prefs.onSafePlay) safeColor else regularColor)
    var validForRefercode = ObservableBoolean(false)

    var coupon = ""
    fun onCodeChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        coupon = s.toString()
    }
}