package com.rummytitans.sdk.cardgame.games.rummy

import com.rummytitans.sdk.cardgame.R
import com.rummytitans.sdk.cardgame.analytics.AnalyticsHelper
import com.rummytitans.sdk.cardgame.api.APIInterface
import com.rummytitans.sdk.cardgame.data.SharedPreferenceStorageRummy
import com.rummytitans.sdk.cardgame.models.GamesResponseModel
import com.rummytitans.sdk.cardgame.models.LoginResponseRummy
import com.rummytitans.sdk.cardgame.ui.BaseViewModel
import com.rummytitans.sdk.cardgame.utils.ConnectionDetector
import com.rummytitans.sdk.cardgame.utils.MyConstants
import com.rummytitans.sdk.cardgame.widget.MyDialog
import android.text.TextUtils
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.rummytitans.sdk.cardgame.BuildConfig
import com.rummytitans.sdk.cardgame.ui.base.BaseNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

@HiltViewModel
class RummyViewModel @Inject constructor(
    val prefs: SharedPreferenceStorageRummy,
    val gson: Gson,
    val apiInterface: APIInterface,
    val connectionDetector: ConnectionDetector, val analyticsHelper: AnalyticsHelper
) : BaseViewModel<BaseNavigator>(conn = null) {
    var myDialog: MyDialog? = null
    var loginModel = gson.fromJson(prefs.loginResponse, LoginResponseRummy::class.java)
    var isLoading = ObservableBoolean(false)
    var loginResponse: LoginResponseRummy = gson.fromJson(prefs.loginResponse, LoginResponseRummy::class.java)


    fun uploadEvents(url: String, timestamp: Long, eventJson: String) {
        if (!connectionDetector.isConnected) {
            myDialog?.noInternetDialog {
                uploadEvents(url, timestamp, eventJson)
            }
            return
        }

        val json = JsonObject()
        json.addProperty("userId", loginResponse.UserId.toString())
        json.addProperty("url", url)
        json.addProperty("timestamp", timestamp)
        json.addProperty("appType", MyConstants.APP_TYPE)
        json.addProperty("appVersion", BuildConfig.VERSION_CODE.toString())
        json.addProperty("os", "android")
        json.addProperty("meta", eventJson)
        var api = getApiEndPointObject(MyConstants.UPDATE_EVENT_URL)
        compositeDisposable.add(
            api.printRummyLog(
                json
            ).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({

                }, {
                    navigator.handleError(it)
                })
        )
    }

    fun logoutUser() {
        prefs.loginCompleted = false
        prefs.loginResponse = gson.toJson(LoginResponseRummy())
        logoutStatus(apiInterface, loginModel.UserId, prefs.androidId ?: "", "0")
    }
}
