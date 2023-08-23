package com.rummytitans.playcashrummyonline.cardgame.ui

import android.util.Log
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.rummytitans.playcashrummyonline.cardgame.BuildConfig
import com.rummytitans.playcashrummyonline.cardgame.analytics.AnalyticsHelper
import com.rummytitans.playcashrummyonline.cardgame.api.APIInterface
import com.rummytitans.playcashrummyonline.cardgame.data.SharedPreferenceStorage
import com.rummytitans.playcashrummyonline.cardgame.models.LoginResponse
import com.rummytitans.playcashrummyonline.cardgame.models.VersionModel
import com.rummytitans.playcashrummyonline.cardgame.models.WalletBalanceModel
import com.rummytitans.playcashrummyonline.cardgame.utils.ConnectionDetector
import com.rummytitans.playcashrummyonline.cardgame.utils.MyConstants
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
@HiltViewModel
class MainViewModel @Inject constructor(
    val prefs: SharedPreferenceStorage,
    val gson: Gson,
    val apiInterface: APIInterface,
    val connectionDetector: ConnectionDetector, val analyticsHelper: AnalyticsHelper
) : BaseViewModel<ActiveGameNavigator>() {

    var isLoading = ObservableBoolean(false)
    var regularColor = prefs.regularColor
    var safeColor = prefs.safeColor
    val selectedColor = ObservableField(if (prefs.onSafePlay) safeColor else regularColor)
    val balance = ObservableField("")
    val gameTicket = ObservableInt(0)
    val userAvtar = ObservableInt(1)
    val loginResponse: LoginResponse = gson.fromJson(prefs.loginResponse, LoginResponse::class.java)
    var versionResp: VersionModel = gson.fromJson(prefs.splashResponse, VersionModel::class.java)
    val displayHome = ObservableBoolean(false)
    var isAddressVerified = true
    private val _walletBalance = MutableLiveData<WalletBalanceModel>()
    init {
       // fetchVerificationData()
    }

   /* fun fetchVerificationData() {
        if (prefs.KycStatus == 1) return
        if (!connectionDetector.isConnected) {
            return
        }
        compositeDisposable.add(
            apiInterface.getVerificationInfo(
                loginResponse.UserId.toString(),
                loginResponse.ExpireToken,
                loginResponse.AuthExpire
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(({
                    if (it.Status)
                        prefs.KycStatus =
                            if (it.Response.MobileVerify && it.Response.PanVerify && it.Response.BankVerify)
                                1 else 0
                }), ({
                }))
        )
    }




    fun fetchProfileData() {
        if (!connectionDetector.isConnected) {
            return
        }
        compositeDisposable.add(
            apiInterface.getProfileIno(
                loginResponse.UserId.toString(),
                loginResponse.ExpireToken,
                loginResponse.AuthExpire
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(({
                    if (it.Status) {
                        userAvtar.set(it.Response.AvtarId)
                        prefs.PinCode = it.Response.PinCode
                    }
                }), ({
                }))
        )
    }

    *//*call for fetch addresse verify or not remove it later*//*
    private fun fetchWalletData() {
        if (!connectionDetector.isConnected) {
            return
        }

        compositeDisposable.add(
            apiInterface.getWalletIno(
                loginResponse.UserId.toString(),
                loginResponse.ExpireToken,
                loginResponse.AuthExpire,
                prefs.seletedLanguage ?: "en"
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(({
                    isAddressVerified = it.IsAddressVerified
                }), ({

                }))
        )
    }*/

    fun getWalletDetail() {
        //remove fetchWalletData when we get isAddressVerified in getWalletDetail APi
        //fetchWalletData()

        if (!connectionDetector.isConnected) {
            return
        }
        val apis = getApiEndPointObject(prefs.appUrl2 ?:"")
        compositeDisposable.add(
            apis.getWalletDetails(
                loginResponse.UserId.toString(),
                loginResponse.ExpireToken,
                loginResponse.AuthExpire
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    //isAddressVerified = it.IsAddressVerified
                    if (it.Status) {
                        _walletBalance.value = it.Response
                        balance.set(it.Response.Balance)
                        gameTicket.set(it.Response.Tickets)

                    } else
                        balance.set("")
                }, {
                    balance.set("")
                })
        )
    }

    fun checkForActiveMatch() {
        if (!connectionDetector.isConnected) {
            return
        }
        isLoading.set(true)
        val json = JsonObject()
        json.addProperty("app_version", BuildConfig.VERSION_CODE)
        json.addProperty("app_type", 7)
        json.addProperty("State", prefs.userStateName)
        json.addProperty("DeviceType", "Android")
        json.addProperty("DeviceID", prefs.androidId)
        json.addProperty(
            "IPAddress",
            getIpAddress()
        )
        json.addProperty("KYCStatus", prefs.KycStatus)
        json.addProperty("PINCode", prefs.PinCode)
        val apis = getApiEndPointObject(MyConstants.GAME_PLAY_URL)

        compositeDisposable.add(
            apis.checkForActiveMatch(
                loginResponse.UserId,
                loginResponse.ExpireToken,
                loginResponse.AuthExpire,
                json
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(({
                    isLoading.set(false)
                    if (it.TokenExpire) {
                        logoutStatus(apis, loginResponse.UserId, prefs.androidId ?: "", "0")
                        prefs.loginResponse = gson.toJson(LoginResponse())
                        prefs.loginCompleted = false
                        navigator.logoutUser()
                    }
                    Log.d("checkForActiveMatch", " " + gson.toJson(it))
                    if (it.Status) {
                       if(it.Response.matchFound){
                           navigatorAct.onFindActiveGame(it.Response.gameplayUrl)
                       }
                    } else {
                        navigator.showError(it.Message)
                    }
                }), ({
                    isLoading.set(false)
                    //navigator.showError(it.message)
                }))
        )
    }

}

interface ActiveGameNavigator {
    fun onFindActiveGame(webUrl: String)
}
