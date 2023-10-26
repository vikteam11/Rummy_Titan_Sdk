package com.rummytitans.playcashrummyonline.cardgame.ui.common

import com.rummytitans.playcashrummyonline.cardgame.data.SharedPreferenceStorageRummy
import com.rummytitans.playcashrummyonline.cardgame.ui.BaseViewModel
import com.rummytitans.playcashrummyonline.cardgame.ui.base.BaseNavigator
import android.graphics.Color
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
@HiltViewModel
class CommonViewModel @Inject constructor(val prefs: SharedPreferenceStorageRummy) :
    BaseViewModel<com.rummytitans.playcashrummyonline.cardgame.ui.base.BaseNavigator>() {
    val isOnSafe = prefs.onSafePlay
    val regularColor = prefs.regularColor
    val safeColor = prefs.safeColor
    val selectedColor = ObservableField(if (prefs.onSafePlay) safeColor else regularColor)
    val isWallet = ObservableField(false)
    var toolbarColor = ObservableInt(Color.parseColor(selectedColor.get()))

}
