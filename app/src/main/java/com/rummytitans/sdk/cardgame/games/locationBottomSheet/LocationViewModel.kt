package com.rummytitans.sdk.cardgame.games.locationBottomSheet

import com.rummytitans.sdk.cardgame.data.SharedPreferenceStorageRummy
import com.rummytitans.sdk.cardgame.models.LoginResponseRummy
import com.rummytitans.sdk.cardgame.ui.BaseViewModel
import com.rummytitans.sdk.cardgame.widget.MyDialog
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LocationViewModel @Inject constructor(
    val prefs: SharedPreferenceStorageRummy,
    val gson: Gson
) : BaseViewModel<LocationNavigator>() {
    var myDialog: MyDialog? = null
    var loginResponse: LoginResponseRummy = gson.fromJson(prefs.loginResponse, LoginResponseRummy::class.java)

    val _bottomSheetStateEvent = MutableLiveData<Int>()
    val bottomSheetStateEvent: LiveData<Int>
        get() = _bottomSheetStateEvent

    var isLoading = MutableLiveData<Boolean>(false)

    val _bottomSheetInvite = MutableLiveData(BottomSheetBehavior.STATE_HIDDEN)
    val bottomSheetInvite: LiveData<Int>
        get() = _bottomSheetInvite
    var regularColor = prefs.regularColor
    var safeColor = prefs.safeColor
    val selectedColor = ObservableField(if (prefs.onSafePlay) safeColor else regularColor)
    var onAllowLocation = false


    fun toggleLocationBottomSheet() {
        _bottomSheetStateEvent.value =
            if (bottomSheetStateEvent.value == BottomSheetBehavior.STATE_EXPANDED) {
                isLoading.value=false
                BottomSheetBehavior.STATE_HIDDEN
            }else{
                isLoading.value=true
                BottomSheetBehavior.STATE_EXPANDED
            }
    }

    fun requestLocation() {
        onAllowLocation=true
        toggleLocationBottomSheet()
        navigatorAct.requestLocation()
    }
}
