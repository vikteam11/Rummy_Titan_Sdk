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
) : BaseViewModel<RummyNavigator>(conn = null) {
    var myDialog: MyDialog? = null
    val isSwipeRefresh = ObservableBoolean(false)
    var loginModel = gson.fromJson(prefs.loginResponse, LoginResponseRummy::class.java)
    var isLoading = ObservableBoolean(false)
    var regularColor = prefs.regularColor
    var safeColor = prefs.safeColor
    val isHeadersAvailable = ObservableBoolean(false)
    val selectedColor = ObservableField(if (prefs.onSafePlay) safeColor else regularColor)
    private var mGameModel: GamesResponseModel.GamesModel? = null
    var isEmptyData = ObservableBoolean(false)
    var comeFromAllGames = ObservableBoolean(false)
    var loginResponse: LoginResponseRummy = gson.fromJson(prefs.loginResponse, LoginResponseRummy::class.java)

    init {
        // fetchProfileData()
        //fetchVerificationData()
        requestGames()
    }

    /* private fun fetchProfileData() {
         if (!prefs.PinCode.isNullOrEmpty()) return
         if (!connectionDetector.isConnected) {
             return
         }
         compositeDisposable.add(
             apiInterface.getProfileIno(
                 loginModel.UserId.toString(),
                 loginModel.ExpireToken,
                 loginModel.AuthExpire
             )
                 .subscribeOn(Schedulers.io())
                 .observeOn(AndroidSchedulers.mainThread())
                 .subscribe(({
                     if (it.Status) prefs.PinCode = it.Response.PinCode
                 }), ({
                 }))
         )
     }

     private fun fetchVerificationData() {
         if (prefs.KycStatus==1) return
         if (!connectionDetector.isConnected) {
             return
         }
         compositeDisposable.add(
             apiInterface.getVerificationInfo(
                 loginModel.UserId.toString(),
                 loginModel.ExpireToken,
                 loginModel.AuthExpire
             )
                 .subscribeOn(Schedulers.io())
                 .observeOn(AndroidSchedulers.mainThread())
                 .subscribe(({
                     if (it.Status)
                         prefs.KycStatus = if(it.Response.MobileVerify && it.Response.PanVerify && it.Response.BankVerify)
                             1 else 0
                 }), ({
                 }))
         )
     }*/
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

    private fun requestGames() {
        if (isSwipeRefresh.get()) {
            return
        }
        isSwipeRefresh.set(true)

        if (!connectionDetector.isConnected) {
            myDialog?.retryInternetDialog {
                isSwipeRefresh.set(true)
                isLoading.set(true)
                requestGames()
            }
            isLoading.set(false)
            isSwipeRefresh.set(false)
            return
        }

        compositeDisposable.add(
            apiInterface.getGames(
                loginModel.UserId,
                loginModel.ExpireToken,
                loginModel.AuthExpire,
                MyConstants.STANDLONE_GAME_ID
            ).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    isSwipeRefresh.set(false)
                    isLoading.set(false)
                    if (it.TokenExpire) {
                        logoutStatus(apiInterface, loginModel.UserId, prefs.androidId ?: "", "0")
                        prefs.loginResponse = gson.toJson(LoginResponseRummy())
                        prefs.loginCompleted = false
                        navigator.logoutUser()
                    }

                    if (it.Status && it.Response != null) {
                        mGameModel = it.Response
                        navigatorAct.launchRummy(it.Response)
                    } else {
                        val errorMsg = if (TextUtils.isEmpty(it.Message))
                            navigator.getStringResource(R.string.something_went_wrong)
                        else
                            it.Message
                        navigator.showError(errorMsg)
                    }
                }, {
                    isSwipeRefresh.set(false)
                    isLoading.set(false)
                    val msg = if (TextUtils.isEmpty(it.message)) {
                        navigator.getStringResource(R.string.something_went_wrong_try_again)
                    } else
                        it.message
                    navigator.showError(msg)
                })
        )
    }

    fun logoutUser() {
        prefs.loginCompleted = false
        prefs.loginResponse = gson.toJson(LoginResponseRummy())
        logoutStatus(apiInterface, loginModel.UserId, prefs.androidId ?: "", "0")
    }
}
