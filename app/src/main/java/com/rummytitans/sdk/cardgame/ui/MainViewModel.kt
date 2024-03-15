package com.rummytitans.sdk.cardgame.ui

import android.text.TextUtils
import android.util.Log
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.rummytitans.sdk.cardgame.BuildConfig
import com.rummytitans.sdk.cardgame.RummyTitanSDK
import com.rummytitans.sdk.cardgame.analytics.AnalyticsHelper
import com.rummytitans.sdk.cardgame.analytics.AnalyticsKey
import com.rummytitans.sdk.cardgame.api.APIInterface
import com.rummytitans.sdk.cardgame.data.SharedPreferenceStorageRummy
import com.rummytitans.sdk.cardgame.models.*
import com.rummytitans.sdk.cardgame.utils.ConnectionDetector
import com.rummytitans.sdk.cardgame.utils.MyConstants
import com.rummytitans.sdk.cardgame.utils.locationservices.utils.emptyJson
import com.rummytitans.sdk.cardgame.widget.MyDialog
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
@HiltViewModel
class MainViewModel @Inject constructor(
    val prefs: SharedPreferenceStorageRummy,
    val gson: Gson,
    val apiInterface: APIInterface,
    val connectionDetector: ConnectionDetector, val analyticsHelper: AnalyticsHelper
) : BaseViewModel<ActiveGameNavigator>() {
    var myDialog: MyDialog? = null
    var isLoading = ObservableBoolean(false)
    var regularColor = prefs.regularColor
    var safeColor = prefs.safeColor
    val selectedColor = ObservableField(if (prefs.onSafePlay) safeColor else regularColor)
    val balance = ObservableField("")
    val gameTicket = ObservableInt(0)
    val userAvtar = ObservableInt(MyConstants.DefaultAvatarID)
    val loginResponse: LoginResponseRummy = gson.fromJson(prefs.loginResponse, LoginResponseRummy::class.java)
    var versionResp: VersionModelRummy = gson.fromJson(prefs.splashResponse, VersionModelRummy::class.java)
    val displayHome = ObservableBoolean(false)
    var isAddressVerified = true
    var walletLoading = ObservableBoolean(false)
    private val _walletBalance = MutableLiveData<WalletBalanceModel>()
    val walletBalance:LiveData<WalletBalanceModel> = _walletBalance
    private val _walletInfo = MutableLiveData<WalletInfoModel>()
    val walletInfo: LiveData<WalletInfoModel>
        get() = _walletInfo
    var isGraphVisible = ObservableBoolean(false)
    var isMiniWalletOpen = ObservableBoolean(false)
    var isGstBonusShow = ObservableBoolean(false)
    var bonusSubList= arrayListOf<WalletInfoModel.WalletBonesModel>()
    var depositBonusVal = ObservableField<WalletInfoModel.WalletBonesModel>()
    var gstBonusVal = ObservableField<WalletInfoModel.WalletBonesModel>()
    var conversionBonusVal = ObservableField<WalletInfoModel.WalletBonesModel>()
    init {
        fetchVerificationData()
    }

    fun fetchVerificationData() {
        if (prefs.KycStatus == 1) return
        if (!connectionDetector.isConnected) {
            return
        }
        val api = getApiEndPointObject(prefs.appUrl2?:"")
        compositeDisposable.add(
            api.getVerificationInfo(
                loginResponse.UserId.toString(),
                loginResponse.ExpireToken,
                loginResponse.AuthExpire
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(({
                    if (it.Status)
                        prefs.KycStatus =
                            if (it.Response.MobileItem.Verify && it.Response.PancardItem.Verify && it.Response.BankItem.Verify)
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
                loginResponse.AuthExpire,
                prefs.instanceId?:""

            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(({
                    if (it.Status) {
                        userAvtar.set(it.Response.AvtarId)
                        prefs.PinCode = it.Response.PinCode
                        if(!prefs.isUserLoginOnCleverTap){
                            setUserProperty(it.Response,prefs.isUserLoginOnCleverTap)
                            prefs.isUserLoginOnCleverTap = true
                        }
                    }
                }), ({
                }))
        )
    }

    private fun setUserProperty(model: ProfileInfoModel?, update:Boolean = false) {
        RummyTitanSDK.analytiCallback?.setJsonUserPropertySDK(emptyJson().apply {
            put(AnalyticsKey.Properties.Mobile, model?.Mobile)
            put(AnalyticsKey.Properties.Email, model?.Email)
            put(AnalyticsKey.Properties.UserID, model?.UserId)
            put(AnalyticsKey.Properties.FullName, model?.Name)
            put(AnalyticsKey.Properties.Gender, model?.Gender)
            put(AnalyticsKey.Properties.DOB, model?.DOB)
            put(AnalyticsKey.Properties.State, model?.StateName)
            put(AnalyticsKey.Properties.RegistrationDate,model?.AccountCreatedDate)
            model?.Name?.split(" ")?.let {list->
                if (list.isEmpty()) return@let
                list.elementAtOrNull(0)?.let {fName->
                    put(AnalyticsKey.Properties.FirstName, fName)
                }
                list.elementAtOrNull(1)?.let {lName->
                    put(AnalyticsKey.Properties.LastName, lName)
                }
            }
        },update)
    }


    fun toggleMiniWallet(){
        isMiniWalletOpen.set(!isMiniWalletOpen.get())
    }

    fun fetchWalletData() {
        if (!connectionDetector.isConnected) {
            myDialog?.noInternetDialog {
                fetchWalletData()
            }
            walletLoading.set(false)
            return
        }
        walletLoading.set(true)
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
                    walletLoading.set(false)
                    if (it.Status) {
                        if (it.Response is WalletInfoModel) {
                            val apiData = it.Response as WalletInfoModel

                            bonusSubList.clear()
                            it.Response.Balance.BonusList?.iterator()?.let { iterator ->
                                while (iterator.hasNext()) {
                                    iterator.next().let { bonus ->
                                        if(!bonus.value.contains("₹")){
                                            bonus.value = "₹"+bonus.value
                                        }
                                        if(isGstBonusShow.get() && bonus.isDeposit){
                                            bonus.isArrowUpDown = true
                                        }
                                        if (bonus.walletType == 3) {
                                            bonusSubList.add(bonus)
                                            iterator.remove()
                                        }
                                    }
                                }
                            }

                            depositBonusVal.set(getBonusSubItem(0))
                            gstBonusVal.set(getBonusSubItem(1))
                            conversionBonusVal.set(getBonusSubItem(2))

                           /* apiData.Balance?.BonusList?.iterator()?.let { iterator ->
                                while (iterator.hasNext()) {
                                    iterator.next().let { bonus ->
                                        if(!bonus.value.contains("₹")){
                                            bonus.value = "₹"+bonus.value
                                        }
                                        if(bonus.isDeposit){
                                            bonus.walletType = 0
                                        }
                                        if (bonus.walletType == 3) {
                                            iterator.remove()
                                        }
                                    }
                                }
                            }*/
                         //   apiData.Offer = apiData.Offer.filter { it1 -> it1.IsShow }
                            _walletInfo.value = apiData
                        }
                    } else {
                        navigator.showError(it.Message)
                    }
                }), ({
                    walletLoading.set(false)
                    navigator.handleError(it)
                }))
        )
    }

    fun addCashListener() {
        navigatorAct.sendToAddCash()
    }

    fun getWalletDetail() {
        fetchWalletData()
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
        if(TextUtils.isEmpty(prefs.gamePlayUrl)){
            prefs.gamePlayUrl = MyConstants.GAME_PLAY_URL
        }
        val apis = getApiEndPointObject(prefs.gamePlayUrl ?:MyConstants.GAME_PLAY_URL)

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
                        prefs.loginResponse = gson.toJson(LoginResponseRummy())
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

    private fun getBonusSubItem(pos:Int): WalletInfoModel.WalletBonesModel?{
        return if(bonusSubList.size > pos){
            bonusSubList[pos]
        }else null
    }

}

interface ActiveGameNavigator {
    fun onFindActiveGame(webUrl: String)
    fun sendToAddCash()
}
