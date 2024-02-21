package com.rummytitans.sdk.cardgame.ui.more.module

import android.text.TextUtils
import com.rummytitans.sdk.cardgame.analytics.AnalyticsHelper
import com.rummytitans.sdk.cardgame.api.APIInterface
import com.rummytitans.sdk.cardgame.data.SharedPreferenceStorageRummy
import com.rummytitans.sdk.cardgame.models.HelpDeskModel
import com.rummytitans.sdk.cardgame.models.LoginResponse
import com.rummytitans.sdk.cardgame.ui.BaseViewModel
import com.rummytitans.sdk.cardgame.ui.more.SupportClick
import com.rummytitans.sdk.cardgame.utils.ConnectionDetector
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.rummytitans.sdk.cardgame.utils.getNotLoginUser
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
@HiltViewModel
class SupportViewModel @Inject constructor(
    val prefs: SharedPreferenceStorageRummy, val apis: APIInterface,
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

    fun userName() = if(TextUtils.isEmpty(loginResponse.Name)){
        "RummyTitans User"
    }else{
        loginResponse.Name
    }
}