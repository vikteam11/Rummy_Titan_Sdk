package com.rummytitans.sdk.cardgame.ui.games.tickets

import com.rummytitans.sdk.cardgame.analytics.AnalyticsHelper
import com.rummytitans.sdk.cardgame.analytics.AnalyticsKey
import com.rummytitans.sdk.cardgame.api.APIInterface
import com.rummytitans.sdk.cardgame.data.SharedPreferenceStorageRummy
import com.rummytitans.sdk.cardgame.models.GameTicketModel
import com.rummytitans.sdk.cardgame.models.LoginResponse
import com.rummytitans.sdk.cardgame.ui.BaseViewModel
import com.rummytitans.sdk.cardgame.utils.ConnectionDetector
import com.rummytitans.sdk.cardgame.utils.MyConstants
import com.rummytitans.sdk.cardgame.widget.MyDialog
import android.text.TextUtils
import androidx.core.os.bundleOf
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.rummytitans.sdk.cardgame.models.UserCurrentLocationModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
@HiltViewModel
class GamesTicketViewModel @Inject constructor(
    val prefs: SharedPreferenceStorageRummy, val gson: Gson, val api: APIInterface,
    val connectionDetector: ConnectionDetector, val analyticsHelper: AnalyticsHelper
) : BaseViewModel<GameTicketNavigator>(conn = connectionDetector) {
    var myDialog: MyDialog? = null
    var loginModel: LoginResponse = gson.fromJson(prefs.loginResponse, LoginResponse::class.java)
    var regularColor = prefs.regularColor
    var safeColor = prefs.safeColor
    val selectedColor = ObservableField(if (prefs.onSafePlay) safeColor else regularColor)
    val gamesTicketList = MutableLiveData<GameTicketModel>()
    val bgImgUrl=ObservableField<String>()
    val noDataAvailable=ObservableBoolean(false)
    var savedTicketModel: GameTicketModel.TicketsItemModel?=null
    val headerUrl = ObservableField("")

    var isAddressVerified = true
    var addressVerificationRejectMsg = ""

    var userCurrentState=""
    var userCurrentStateLatLog=""


    fun saveStateInLoginResponse(state: String) {
        loginModel.gameState = state
        prefs.loginResponse = gson.toJson(loginModel)
    }

    var locationModel: UserCurrentLocationModel?=null

    fun saveUserLocation(userLatLog:String="") {
        locationModel?.updateData("",userLatLog,"")
        fetchUserStateName(userLatLog)
    }

    fun isLocationRequired():Boolean{
        when {
            //savedTicketModel?.gameId!=newGameId -> return true
            prefs.locationApiTimeLimit <= Date().time -> {
                prefs.locationApiTimeLimit = 0L
                prefs.locationApiPendingMinutes = 0
                if (locationModel==null)
                    locationModel=
                        UserCurrentLocationModel()
                else
                    locationModel?.resetModel()
                return true
            }
            else -> {
                return try{
                    locationModel = gson.fromJson(prefs.userCurrentLocationRes,
                        UserCurrentLocationModel::class.java)
                    false
                }catch (e:Exception) {
                    locationModel =
                        UserCurrentLocationModel()
                    true
                }
            }
        }
    }

    fun saveLocationDisableTime(minute: Int=5,resetTime:Boolean=false) {
        if (resetTime){
            prefs.locationApiTimeLimit = 0L
            prefs.locationApiPendingMinutes =0
            locationModel?.resetModel()
            prefs.userCurrentLocationRes=""
            return
        }
        if (prefs.locationApiPendingMinutes!=minute){
            val df = SimpleDateFormat("HH:mm")
            val d = Date()
            val cal = Calendar.getInstance()
            cal.time = d
            cal.add(Calendar.MINUTE, minute)
            prefs.locationApiTimeLimit = cal.time.time
            prefs.locationApiPendingMinutes =minute
            println(" ${df.format(cal.time)}")
        }
    }

    fun fetchUserStateName(userLatLog:String){
        if (!connectionDetector.isConnected) {
            myDialog?.noInternetDialog {
                isParentLoading.set(true)
                fetchUserStateName(userLatLog)
            }
            isParentLoading.set(false)
            return
        }
        isParentLoading.set(true)

        val json = JsonObject()
        json.addProperty(APIInterface.COORDINATES,userLatLog)
        compositeDisposable.add(
            api.checkUserIsBanned(
                loginModel.UserId.toString(),
                loginModel.ExpireToken,
                loginModel.AuthExpire,
                json)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    isParentLoading.set(false)
                    if (!it.Status) {
                        navigator.showError(it.Message)
                        return@subscribe
                    }else if (it.Status){
                        if (it.AllowRummyGame){
                            prefs.userCurrentLocationRes=gson.toJson(locationModel)
                            saveLocationDisableTime(it.DelayMinutes)
                            requestRedeem(savedTicketModel?.gameId?:0)
                        }else{
                            prefs.userCurrentLocationRes=gson.toJson(locationModel)
                            saveLocationDisableTime(it.DelayMinutes)
                            navigatorAct.onLocationRestricted(it.Message)
                        }
                    }
                },{
                    isParentLoading.set(false)
                    saveUserLocation(userLatLog)
                }))
    }

    fun requestGamesTickets() {
        isParentLoading.set(true)
        if (!connectionDetector.isConnected) {
            myDialog?.noInternetDialog {
                isParentLoading.set(true)
                requestGamesTickets()
            }
            isParentLoading.set(false)
            return
        }
        apiCall(api.getGamesTicket(
            loginModel.UserId,
            loginModel.AuthExpire,
            loginModel.ExpireToken,
            MyConstants.STANDLONE_GAME_ID
        ), {
            noDataAvailable.set(it.Response.availableTickets.isEmpty() && it.Response.usersTickets.isEmpty())
            isAddressVerified = it.IsAddressVerified
            addressVerificationRejectMsg = it.Message ?: ""
            if (it.Status) {
                bgImgUrl.set(it.Response.headerImage)
                gamesTicketList.value = it.Response
                headerUrl.set(it.Response.headerUrl)
            } else navigator.showError(it.Message)
        }, failed = {
            noDataAvailable.set(true)
            isParentLoading.set(true)
        }, unSuccess = {
            noDataAvailable.set(true)
            isParentLoading.set(true)
        })
    }

    fun onInfoClick(){
        gamesTicketList.value?.let {
            navigatorAct.onInfoClick(it.headerUrl)
        }
    }

    fun requestRedeem(gameID:Int,state: String="",lat:Double=.0,log:Double=.0) {
        isParentLoading.set(true)
        if (!connectionDetector.isConnected) {
            myParentDialog?.noInternetDialog {
                isParentLoading.set(true)
                requestRedeem(gameID,state,lat, log)
            }
            isParentLoading.set(false)
            return
        }
        if (!TextUtils.isEmpty(state)) {
            loginModel.gameState = state
            prefs.loginResponse = gson.toJson(loginModel)
        }
        apiCall(api.getGameDetails(
            loginModel.UserId,
            loginModel.ExpireToken,
            loginModel.AuthExpire,
            loginModel.gameState,
            gameID,
            "$lat,$log"
        ), {
            savedTicketModel=null
            if (it.IsBanned) {
                navigatorAct.onLocationRestricted(MyConstants.RESTRICT_LOC_MESSAGE)
                return@apiCall
            }

            if (it.Status) {
                analyticsHelper.fireEvent(
                    AnalyticsKey.Names.RedeemGameTicketClicked, bundleOf(
                        AnalyticsKey.Keys.GameName to it.Response.name
                    )
                )
                saveStateInLoginResponse(it.StateName)
                navigatorAct.onGameDetailsRecieve(it.Response)
            } else navigator.showError(it.Message)
        },unSuccess={
            savedTicketModel=null
            navigator.showError(it.Message)
        })
    }
}