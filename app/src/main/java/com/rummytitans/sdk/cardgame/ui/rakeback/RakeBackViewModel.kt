package com.rummytitans.sdk.cardgame.ui.rakeback

import androidx.core.os.bundleOf
import androidx.databinding.ObservableBoolean
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.rummytitans.sdk.cardgame.analytics.AnalyticsHelper
import com.rummytitans.sdk.cardgame.analytics.AnalyticsKey
import com.rummytitans.sdk.cardgame.api.APIInterface
import com.rummytitans.sdk.cardgame.data.SharedPreferenceStorageRummy
import com.rummytitans.sdk.cardgame.models.LoginResponseRummy
import com.rummytitans.sdk.cardgame.models.RakeBackDetailModelRummy
import com.rummytitans.sdk.cardgame.ui.BaseViewModel
import com.rummytitans.sdk.cardgame.utils.ConnectionDetector
import com.rummytitans.sdk.cardgame.widget.MyDialog
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
@HiltViewModel
class RakeBackViewModel @Inject constructor(
    val prefs: SharedPreferenceStorageRummy,
    val gson: Gson,
    val apiInterface: APIInterface,
    val connectionDetector: ConnectionDetector, val analyticsHelper: AnalyticsHelper
) : BaseViewModel<RakeBackNavigator>() {

    var loginResponse: LoginResponseRummy = gson.fromJson(prefs.loginResponse, LoginResponseRummy::class.java)
    val isLoading = ObservableBoolean(false)
    val isRefreshing = ObservableBoolean(false)
    val historyAvailable = ObservableBoolean(false)
    var myDialog: MyDialog? = null

    private val _rakeBackDetail = MutableLiveData<RakeBackDetailModelRummy>()
    val rakeBackDetail: LiveData<RakeBackDetailModelRummy> = _rakeBackDetail

    fun fetchRakeBackDetail(dataRefresh:Boolean=false) {
        if (!connectionDetector.isConnected) {
            myDialog?.noInternetDialog {
                fetchRakeBackDetail()
            }
            return
        }
        isLoading.set(!dataRefresh)
        isRefreshing.set(dataRefresh)
        val apis = getApiEndPointObject(prefs.gamePlayUrl ?:"")
        compositeDisposable.add(
            apis.getRakeBackDetail(
                loginResponse.UserId.toString(),
                loginResponse.ExpireToken,
                loginResponse.AuthExpire,
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(({
                    isLoading.set(false)
                    isRefreshing.set(false)
                    if (it.TokenExpire) {
                        logoutStatus(apiInterface, loginResponse.UserId, prefs.androidId ?: "", "0")
                        prefs.loginResponse = gson.toJson(LoginResponseRummy())
                        prefs.loginCompleted = false
                        navigator.logoutUser()
                    }

                    if (it.checkStatus()) {
                        historyAvailable.set(it.Response.redeemHistory.isNotEmpty())
                        _rakeBackDetail.value = it.Response
                    } else {
                        historyAvailable.set(false)
                        navigator.showError(it.Message)
                    }
                }), ({
                    historyAvailable.set(false)
                    isLoading.set(false)
                    isRefreshing.set(false)
                    navigator.showError(it.message)
                }))
        )
    }

    fun redeemRakeBackAmount() {
        if (!connectionDetector.isConnected) {
            myDialog?.noInternetDialog {
                redeemRakeBackAmount()
            }
            return
        }
        val amount = rakeBackDetail.value?.availableRakeBackAmount?:0.0
        isLoading.set(true)
        val body = JsonObject()
        body.addProperty("amount",amount)
        val apis = getApiEndPointObject(prefs.gamePlayUrl ?:"")
        compositeDisposable.add(
            apis.redeemRakeBack(
                loginResponse.UserId.toString(),
                loginResponse.ExpireToken,
                loginResponse.AuthExpire,
                body
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(({
                    isLoading.set(false)
                    if (it.TokenExpire) {
                        logoutStatus(apiInterface, loginResponse.UserId, prefs.androidId ?: "", "0")
                        prefs.loginResponse = gson.toJson(LoginResponseRummy())
                        prefs.loginCompleted = false
                        navigator.logoutUser()
                    }

                    if (it.checkStatus()) {
                        analyticsHelper.fireEvent(
                            AnalyticsKey.Names.RedeemRakebackAmountDone, bundleOf(
                                AnalyticsKey.Keys.RedeemAmount to   amount.toString(),
                                AnalyticsKey.Keys.Screen to AnalyticsKey.Screens.Rakeback,
                            )
                        )
                        navigatorAct.onRedeemAmount(it.Message?:"")
                    } else {
                        navigator.showError(it.Message)
                    }
                }), ({
                    isLoading.set(false)
                    navigator.showError(it.message)
                }))
        )
    }


}