package com.rummytitans.sdk.cardgame.ui.joinGame


import com.rummytitans.sdk.cardgame.analytics.AnalyticsHelper
import com.rummytitans.sdk.cardgame.api.APIInterface
import com.rummytitans.sdk.cardgame.data.SharedPreferenceStorageRummy
import com.rummytitans.sdk.cardgame.models.LoginResponseRummy
import com.rummytitans.sdk.cardgame.models.JoinGameConfirmationModel
import com.rummytitans.sdk.cardgame.ui.BaseViewModel
import com.rummytitans.sdk.cardgame.utils.ConnectionDetector
import com.rummytitans.sdk.cardgame.widget.MyDialog
import androidx.core.os.bundleOf
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.rummytitans.sdk.cardgame.BuildConfig
import com.rummytitans.sdk.cardgame.analytics.AnalyticsKey
import com.rummytitans.sdk.cardgame.models.RummyLobbyModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

@HiltViewModel
class JoinContestViewModel @Inject constructor(
    val prefs: SharedPreferenceStorageRummy,
    val apis: APIInterface,
    val gson: Gson,
    val connectionDetector: ConnectionDetector,
    val analyticsHelper: AnalyticsHelper
) : BaseViewModel<JoinGameSheetNavigator>() {
    var myDialog: MyDialog? = null
    val loginResponse: LoginResponseRummy = gson.fromJson(prefs.loginResponse, LoginResponseRummy::class.java)
    var isLoading = ObservableBoolean(false)
    val ispurchaseCoupon = ObservableBoolean(false)
    val mModiPassCount = ObservableInt()

    val joinConfirmModel = ObservableField(JoinGameConfirmationModel())
    var lobby: RummyLobbyModel?= null
    var isAddressVerified = true
    private val _bonusList = MutableLiveData<List<JoinGameConfirmationModel.JoinGameBonus>>()
    val bonusList: LiveData<List<JoinGameConfirmationModel.JoinGameBonus>> = _bonusList

    fun confirmLobby(stakeId:String) {
        if (!connectionDetector.isConnected) {
            myDialog?.noInternetDialog {
                confirmLobby(stakeId)
            }
            return
        }
        isLoading.set(true)
        val json = JsonObject()
        json.addProperty("StakeId",stakeId)
        val apis = getApiEndPointObject(prefs.gamePlayUrl ?:"")
        compositeDisposable.add(
            apis.confirmLobby(
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
                        prefs.loginResponse = gson.toJson(LoginResponseRummy())
                        prefs.loginCompleted = false
                        navigator.logoutUser()
                    }
                    isAddressVerified = it.Response.IsAddressVerified
                    if (it.Status) {
                        joinConfirmModel.set(it.Response)
                        _bonusList.value = it.Response?.bonusList?: listOf()

                        if(lobby?.tagShow() == false && !isAddressVerified){
                            navigatorAct.sendToAddressVerification()
                        }
                    } else {
                        navigatorAct.showError(it.Message)
                    }
                }), ({
                    isLoading.set(false)
                    navigatorAct.showError(it.message)
                }))
        )
    }

    fun joinGame() {
        if (!connectionDetector.isConnected) {
            myDialog?.noInternetDialog {
                joinGame()
            }
            return
        }

        val json = JsonObject()
        json.addProperty("StakeId",lobby?.StakeId?:"")
        json.addProperty("app_version", BuildConfig.VERSION_CODE)
        json.addProperty("app_type", prefs.getRummySdkOption().currentAppType)
        json.addProperty("State",prefs.userStateName)
        json.addProperty("DeviceType","Android")
        json.addProperty("DeviceID",prefs.androidId)
        json.addProperty("IPAddress", getIpAddress())
        json.addProperty("KYCStatus",prefs.KycStatus)
        json.addProperty("PINCode",prefs.PinCode)

        isLoading.set(true)
        val apis = getApiEndPointObject(prefs.gamePlayUrl ?:"")
        compositeDisposable.add(
            apis.joinLobby(
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
                        prefs.loginResponse = gson.toJson(LoginResponseRummy())
                        prefs.loginCompleted = false
                        navigator.logoutUser()
                    }
                    fireJoinGameEvent(it.Status)
                    if (it.Status) {
                        navigatorAct.onSuccess(it.Response.gameplayUrl)
                    } else {
                         navigatorAct.showError(it.Message)
                    }
                }), ({
                    fireJoinGameEvent(false)
                    isLoading.set(false)
                    navigatorAct.showError(it.message)
                }))
        )
    }

    private fun fireJoinGameEvent(isSuccess:Boolean){
        lobby?.let { loby->
            joinConfirmModel.get()?.let { joinModel->
                analyticsHelper.fireEvent(
                    AnalyticsKey.Names.PlayNow, bundleOf(
                        AnalyticsKey.Keys.GameId to 9 ,
                        AnalyticsKey.Keys.GameName to "Rummy",
                        AnalyticsKey.Keys.TableId to loby.StakeId,
                        AnalyticsKey.Keys.GameType to loby.CatName,
                        AnalyticsKey.Keys.UsableBalance to joinModel.usableBalance,
                        AnalyticsKey.Keys.Bonus to joinModel.bonus,
                        AnalyticsKey.Keys.GameTicket to joinModel.ticket,
                        AnalyticsKey.Keys.WinningAmount to joinModel.usableBalance,//
                        AnalyticsKey.Keys.EntryFee to joinModel.entryFee,
                        AnalyticsKey.Keys.Status to if(isSuccess)"Success" else "Fail",
                        AnalyticsKey.Keys.Screen to AnalyticsKey.Values.JoinConfirmation,
                    )
                )
            }

        }

    }
}
