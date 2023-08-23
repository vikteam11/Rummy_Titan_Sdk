package com.rummytitans.playcashrummyonline.cardgame.ui.more.module

import android.text.TextUtils
import com.rummytitans.playcashrummyonline.cardgame.analytics.AnalyticsHelper
import com.rummytitans.playcashrummyonline.cardgame.api.APIInterface
import com.rummytitans.playcashrummyonline.cardgame.data.SharedPreferenceStorage
import com.rummytitans.playcashrummyonline.cardgame.models.HelpDeskModel
import com.rummytitans.playcashrummyonline.cardgame.models.LoginResponse
import com.rummytitans.playcashrummyonline.cardgame.ui.BaseViewModel
import com.rummytitans.playcashrummyonline.cardgame.ui.more.SupportClick
import com.rummytitans.playcashrummyonline.cardgame.utils.ConnectionDetector
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.rummytitans.playcashrummyonline.cardgame.utils.getNotLoginUser
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
@HiltViewModel
class SupportViewModel @Inject constructor(
    val prefs: SharedPreferenceStorage, val apis: APIInterface,
    val gson: Gson, val analyticsHelper: AnalyticsHelper,
    connectionDetector: ConnectionDetector
) : BaseViewModel<SupportClick>(connectionDetector) {
    val loginResponse = prefs.loginResponse.let {
        try {
            if (TextUtils.isEmpty(it)) getNotLoginUser()
            else gson.fromJson(prefs.loginResponse, LoginResponse::class.java)
        } catch (e: Exception) {
            getNotLoginUser()
        }
    }
    val supportResponse = MutableLiveData<HelpDeskModel>()
    val selectedColor = ObservableField(prefs.safeColor)

    fun getHelpDesk() {
        apiCall(apis.getHelpDesk(
            loginResponse?.UserId?.toString()?:"",
            loginResponse?.ExpireToken?:"",
            loginResponse?.AuthExpire?:""
        ), {
            supportResponse.value = it.Response
        })
    }
}