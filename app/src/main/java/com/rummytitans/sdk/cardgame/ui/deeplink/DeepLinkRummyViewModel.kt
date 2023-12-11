package com.rummytitans.sdk.cardgame.ui.deeplink

import com.rummytitans.sdk.cardgame.BuildConfig
import com.rummytitans.sdk.cardgame.api.APIInterface
import com.rummytitans.sdk.cardgame.data.SharedPreferenceStorageRummy
import com.rummytitans.sdk.cardgame.models.LoginResponse
import com.rummytitans.sdk.cardgame.models.MatchModel
import com.rummytitans.sdk.cardgame.models.VersionModel
import com.rummytitans.sdk.cardgame.ui.BaseViewModel
import com.rummytitans.sdk.cardgame.utils.ConnectionDetector
import com.rummytitans.sdk.cardgame.widget.MyDialog
import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.rummytitans.sdk.cardgame.RummyTitanSDK
import dagger.hilt.android.lifecycle.HiltViewModel
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
    val loginResponse = gson.fromJson(prefs.loginResponse, LoginResponse::class.java)
    val isForceUpdate = MutableLiveData<VersionModel>()

    fun checkForceUpdate() {
        val apis = getApiEndPointObject(RummyTitanSDK.getOption().gameSplashUrl)
        apiCall(apis.getVersion(
            loginResponse?.UserId ?: 0, BuildConfig.VERSION_CODE, ""
        ), {
            if (it.Status) {
                prefs.splashResponse = gson.toJson(it.Response)
                isForceUpdate.value = it.Response
            }
        })
    }

    fun getMatchDetails(matchId: String) {
        if (!connectionDetector.isConnected) {
            myDialog?.noInternetDialog { getMatchDetails(matchId) }
            return
        }
        val filteredMatchId = when {
            TextUtils.isEmpty(matchId) -> 0
            TextUtils.isDigitsOnly(matchId) -> matchId.toInt()
            else -> 0
        }
        apiCall(apis.getSingleMatchDetails(
            loginResponse.UserId,
            loginResponse.ExpireToken,
            loginResponse.AuthExpire,
            filteredMatchId
        ), {
            if (it.TokenExpire) {
                logoutStatus(apis, loginResponse.UserId, prefs.androidId ?: "", "0")
                prefs.loginResponse = gson.toJson(LoginResponse())
                prefs.loginCompleted = false
                navigator.logoutUser()
            }
            if (it.Status) {
                navigator.showMessage(it.Message)
                matchModel = it.Response.getUpdatedMatchModel(filteredMatchId)
                 navigatorAct.sendToContestActivity(matchModel)
            } else navigator.showError(it.Message)
        })
    }

    fun getInviteCodeDetails(contestCode: String) {

        if (!connectionDetector.isConnected) {
            myDialog?.noInternetDialog { getInviteCodeDetails(contestCode) }
            return
        }

        apiCall(apis.getPrivateContestDetails(
            loginResponse.UserId, loginResponse.ExpireToken,
            loginResponse.AuthExpire, contestCode
        ), {
            if (it.Status) {
                navigator.showMessage(it.Message)
                leagueFees = it.Response.Fees
                leagueId = it.Response.LeagueID
                leagueMembers = it.Response.NoofMembers
                getMatchDetails(it.Response.MatchID.toString())
            } else {
                navigator.showError(it.Message)
              //  navigatorAct.finishActivity()
            }
        },unSuccess = {
            navigator.showError(it.Message)
           // navigatorAct.finishActivity()
        })
    }

    fun changeSportsType(type: Int) {
        prefs.sportSelected = type
        navigatorAct.finishAllAndCallMainActivity()
    }
}