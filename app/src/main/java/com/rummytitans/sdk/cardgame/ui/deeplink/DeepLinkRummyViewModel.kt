package com.rummytitans.sdk.cardgame.ui.deeplink

import com.rummytitans.sdk.cardgame.BuildConfig
import com.rummytitans.sdk.cardgame.api.APIInterface
import com.rummytitans.sdk.cardgame.data.SharedPreferenceStorageRummy
import com.rummytitans.sdk.cardgame.models.LoginResponseRummy
import com.rummytitans.sdk.cardgame.models.MatchModel
import com.rummytitans.sdk.cardgame.models.VersionModelRummy
import com.rummytitans.sdk.cardgame.ui.BaseViewModel
import com.rummytitans.sdk.cardgame.utils.ConnectionDetector
import com.rummytitans.sdk.cardgame.widget.MyDialog
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.rummytitans.sdk.cardgame.RummyTitanSDK
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
@HiltViewModel
class DeepLinkRummyViewModel @Inject constructor(
    val prefs: SharedPreferenceStorageRummy, val apis: APIInterface,
    val gson: Gson, val connectionDetector: ConnectionDetector
) : BaseViewModel<DeepLinkNavigator>() {
    var matchModel: MatchModel? = null
    var myDialog: MyDialog? = null
    val selectedColor = if (prefs.onSafePlay) prefs.safeColor else prefs.regularColor
    var leagueId = 0
    var leagueFees = 0
    var leagueMembers = 0
    val loginResponse = gson.fromJson(prefs.loginResponse, LoginResponseRummy::class.java)
    val isForceUpdate = MutableLiveData<VersionModelRummy>()

    fun checkForceUpdate() {
        val apis = getApiEndPointObject(RummyTitanSDK.getOption().gameSplashUrl)
        apiCall(apis.getVersion(
            loginResponse?.UserId ?: 0, BuildConfig.VERSION_CODE, ""
        ), {
            if (it.Status) {
                prefs.splashResponse = gson.toJson(it.Response)
                isForceUpdate.value = it.Response
            }else{
                navigatorAct.showErrorAndFinish(it.Message?: "")
            }
        }, unSuccess = {
            navigatorAct.showErrorAndFinish(it.message?: "")
        })
    }



    fun getDeeplinkUrl(action:String) {
        if (!connectionDetector.isConnected) {
            myDialog?.noInternetDialog {
                getDeeplinkUrl(action)
            }
            return
        }
        val json = JsonObject()
        json.addProperty("actionVal", action)
        isParentLoading.set(true)
        val apiInterface = getApiEndPointObject(prefs.appUrl2?:"")
        compositeDisposable.add(
            apiInterface.getDeeplinkUrl(
                loginResponse.UserId,
                loginResponse.ExpireToken,
                loginResponse.AuthExpire,
                json
            ).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    isParentLoading.set(false)
                    if (it.Status) {
                        navigatorAct.openWebView(it.Response.Title,it.Response.DeeplinksUrl)
                    }else{
                        navigatorAct.showErrorAndFinish(it.Message?: "")
                    }
                }, {
                    isParentLoading.set(false)
                    //navigator.handleError(it)
                    navigatorAct.showErrorAndFinish(it.message?: "")
                })
        )
    }


}