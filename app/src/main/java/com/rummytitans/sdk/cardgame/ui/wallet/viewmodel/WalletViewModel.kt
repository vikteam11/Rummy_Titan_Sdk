package com.rummytitans.sdk.cardgame.ui.wallet.viewmodel

import com.rummytitans.sdk.cardgame.R
import com.rummytitans.sdk.cardgame.analytics.AnalyticsHelper
import com.rummytitans.sdk.cardgame.analytics.AnalyticsKey
import com.rummytitans.sdk.cardgame.api.APIInterface
import com.rummytitans.sdk.cardgame.data.SharedPreferenceStorageRummy
import com.rummytitans.sdk.cardgame.models.LoginResponse
import com.rummytitans.sdk.cardgame.models.WalletInfoModel
import com.rummytitans.sdk.cardgame.ui.BaseViewModel
import com.rummytitans.sdk.cardgame.ui.wallet.WalletNavigator
import com.rummytitans.sdk.cardgame.utils.ConnectionDetector
import com.rummytitans.sdk.cardgame.utils.locationservices.utils.emptyJson
import com.rummytitans.sdk.cardgame.widget.MyDialog
import android.text.TextUtils
import androidx.core.os.bundleOf
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
@HiltViewModel
class WalletViewModel @Inject constructor(
    val prefs: SharedPreferenceStorageRummy,
    val gson: Gson,
    val apiInterface: APIInterface,
    val connectionDetector: ConnectionDetector, val analyticsHelper: AnalyticsHelper
) : BaseViewModel<WalletNavigator>() {

    var myDialog: MyDialog? = null

    var isLoading = ObservableBoolean(false)
    var isSwipeLoading = ObservableBoolean(false)
    val data = MutableLiveData<WalletInfoModel>()
    val walletInfo: LiveData<WalletInfoModel>
        get() = data
    var loginResponse: LoginResponse = gson.fromJson(prefs.loginResponse, LoginResponse::class.java)

    val isOnSafe = ObservableBoolean(false)
    var availableCode = ObservableField<String>()
    var validForRefercode = ObservableBoolean(false)


    val offerTitle = ObservableField<String>("")
    val offerDescription = ObservableField<String>("")

    val withdrawtext = MutableLiveData<String>()

    var isVerified = ObservableBoolean(true)

    val isBottomSheetVisible = ObservableBoolean(false)
    var isGraphVisible = ObservableBoolean(false)

    val isAvtivity = ObservableBoolean(false)
    var isGstBonusShow = ObservableBoolean(false)
    var regularColor = prefs.regularColor
    var safeColor = prefs.safeColor
    val selectedColor = ObservableField(if (prefs.onSafePlay) safeColor else regularColor)
    var bonusSubList= arrayListOf<WalletInfoModel.WalletBonesModel>()
    var depositBonusVal = ObservableField<WalletInfoModel.WalletBonesModel>()
    var gstBonusVal = ObservableField<WalletInfoModel.WalletBonesModel>()
    //first time user fill data code
    var pincode = ObservableField<String>("")
    var date = ObservableField<String>("")
    var isAddressVerified=true
    var addressVerificationRejectMsg=""
    //reset profileForm data of firsttime user
    fun resetFormData() {
        date.set("")
        pincode.set("")
    }

    fun hideSheet() {
        isBottomSheetVisible.set(false)
    }

    fun showRedeemSheet() {
        isBottomSheetVisible.set(true)
    }

    fun setDefaultTheme() {
        isOnSafe.set(prefs.onSafePlay)
    }

    fun setDefaultItems() {
        selectedColor.set(if (prefs.onSafePlay) safeColor else regularColor)
    }

    fun updateThemeCode() {
        regularColor = prefs.regularColor
        safeColor = prefs.safeColor
    }
    //update in case of update user details
    fun updateLoginModel(){
        loginResponse = Gson().fromJson(prefs.loginResponse, LoginResponse::class.java)
    }
    fun fetchWalletData() {
        setDefaultTheme()
        if (!connectionDetector.isConnected) {
            myDialog?.noInternetDialog {
                isLoading.set(true)
                fetchWalletData()
            }
            isLoading.set(false)
            isSwipeLoading.set(false)
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
                    isLoading.set(false)
                    isSwipeLoading.set(false)
                    if (it.TokenExpire) {
                        logoutStatus(apiInterface, loginResponse.UserId, prefs.androidId ?: "", "0")
                        prefs.loginResponse = gson.toJson(LoginResponse())
                        prefs.loginCompleted = false
                        navigator.logoutUser()
                    }
                    isAddressVerified=it.Response?.AddressVerify?:false
                    addressVerificationRejectMsg=it.Message?:""
                    if (it.Status) {
                        isGraphVisible.set(true)

                        //it.Response.Offer = it.Response.Offer.filter { it1 -> it1.IsShow }

                        data.value = it.Response
                        when {
                            !it.Response.BankVerify or !it.Response.PanVerify or !isAddressVerified-> {
                                isVerified.set(false)
                            }
                            it.Response.BankVerify and it.Response.PanVerify and isAddressVerified and (it.Response.Balance.Winning >= 200) -> {
                                isVerified.set(true)
                            }
                            it.Response.BankVerify and it.Response.PanVerify and isAddressVerified and (it.Response.Balance.Winning < 200) -> {
                                isVerified.set(true)
                            }
                        }

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
                        with(it.Response.Balance) {
                            if (prefs.userTotalAmount != TotalAmount.toString()) {
                                var credit = "0.0"
                                BonusList.forEach { bonus ->
                                    if (bonus.isbouns) credit = bonus.value
                                }
                                analyticsHelper.setJsonUserProperty(
                                    emptyJson().apply {
                                        put(AnalyticsKey.Properties.WalletBalance, TotalAmount)
                                        put(AnalyticsKey.Properties.Winning, Winning)
                                        put(AnalyticsKey.Properties.UnUtilize, Unutilized)
                                        put(AnalyticsKey.Properties.Credit, credit)
                                    }
                                )
                                prefs.userTotalAmount = TotalAmount.toString()
                            }
                        }

                        withdrawtext.value = navigator.getStringResource(R.string.withdraw_money)
                    } else {
                        isGraphVisible.set(false)
                        navigator.showError(it.Message)
                    }
                    //hide in case of address not verified.
                    //in that case message key contains rejection message.
                    if (it.IsAddressVerified) {
                        // in case or error message allready display see above code
                        if (it.Status) navigator.showMessage(it.Message)
                    }
                }), ({
                    isSwipeLoading.set(false)
                    isGraphVisible.set(false)
                    isLoading.set(false)
                    navigator.handleError(it)
                }))
        )
    }

    val scratchCardResponse = ObservableField<String>("")

    val isScratchCardVisible = ObservableBoolean(false)

    fun closeScratch() {
        isScratchCardVisible.set(false)
    }

    fun getScratchCard() {
        if (!connectionDetector.isConnected) {
            myDialog?.noInternetDialog {
                isLoading.set(true)
                getScratchCard()
            }
            isLoading.set(false)
            return
        }

        compositeDisposable.add(
            apiInterface.getScratchCard(
                loginResponse.UserId.toString(),
                loginResponse.ExpireToken,
                loginResponse.AuthExpire
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(({
                    isLoading.set(false)
                    isSwipeLoading.set(false)
                    if (it.TokenExpire) {
                        logoutStatus(apiInterface, loginResponse.UserId, prefs.androidId ?: "", "0")
                        prefs.loginResponse = gson.toJson(LoginResponse())
                        prefs.loginCompleted = false
                        navigator.logoutUser()
                    }
                    if (it.Status) {
                        isScratchCardVisible.set(it.Status)
                        scratchCardResponse.set(it.Response)
                    } else {
                        navigator.showError(it.Message)
                    }
                    navigator.showMessage(it.Message)
                }), ({
                    isLoading.set(false)
                    navigator.handleError(it)
                }))
        )
    }

    fun updateScratchCard() {
        if (!connectionDetector.isConnected) {
            myDialog?.noInternetDialog {
                isLoading.set(true)
                getScratchCard()
            }
            isLoading.set(false)
            return
        }

        compositeDisposable.add(
            apiInterface.updateScratchCard(
                loginResponse.UserId.toString(),
                loginResponse.ExpireToken,
                loginResponse.AuthExpire
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(({
                    isLoading.set(false)
                    isSwipeLoading.set(false)
                    if (it.TokenExpire) {
                        logoutStatus(apiInterface, loginResponse.UserId, prefs.androidId ?: "", "0")
                        prefs.loginResponse = gson.toJson(LoginResponse())
                        prefs.loginCompleted = false
                        navigator.logoutUser()
                    }
                    if (it.Status) {

                    } else {
                        navigator.showError(it.Message)
                    }
                    navigator.showMessage(it.Message)
                }), ({
                    isLoading.set(false)
                    navigator.handleError(it)
                }))
        )
    }

    var coupon = ""

    fun onCodeChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        coupon = s.toString()
    }

    fun onAddCashClick() {
       /* if (!loginResponse.IsFirstTime) {
            navigatorAct.fillProfileDataForFirstTimeUser()
        } else*/
        navigatorAct.performOnAddCashClick()
    }

    fun updateProfileFirstTimeUser() {
        if (!connectionDetector.isConnected) {
            myDialog?.noInternetDialog {
                isLoading.set(true)
                updateProfileFirstTimeUser()
            }
            isLoading.set(false)
            return
        }
        isLoading.set(true)
        compositeDisposable.add(
            apiInterface.updatePincode(
                loginResponse.AuthExpire,
                loginResponse.ExpireToken,
                loginResponse.UserId.toString(),
                pincode.get().toString(),
                date.get().toString()
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    isLoading.set(false)
                    if (it.Status) {
                        loginResponse.IsFirstTime = true
                        prefs.loginResponse = gson.toJson(loginResponse)
                        navigatorAct.performOnAddCashClick()
                    } else {

                        navigator.showError(it.Message)
                    }
                }, {
                    isLoading.set(false)
                    navigator.showError(it.message)
                })
        )
    }

    fun redeemCode(coupon: String) {

        if (TextUtils.isEmpty(coupon)) {
            navigator.showError("Please Enter Coupon Code")
            return
        } else if (coupon.length < 4) {
            navigator.showError("Please Enter Valid Coupon Code")
            return
        }

        if (!connectionDetector.isConnected) {
            myDialog?.noInternetDialog {
                isLoading.set(true)
                redeemCode(coupon)
            }
            isLoading.set(false)
            return
        }

        isLoading.set(true)

        compositeDisposable.add(
            apiInterface.redeemCode(
                loginResponse.UserId.toString(),
                loginResponse.ExpireToken,
                loginResponse.AuthExpire, coupon, prefs.androidId ?: ""
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(({
                    isLoading.set(false)
                    if (it.TokenExpire) {
                        logoutStatus(apiInterface, loginResponse.UserId, prefs.androidId ?: "", "0")
                        prefs.loginResponse = gson.toJson(LoginResponse())
                        prefs.loginCompleted = false
                        navigator.logoutUser()
                    }
                    if (it.Status) {
                        analyticsHelper.fireEvent(
                            AnalyticsKey.Names.RedeemCopounDone, bundleOf(
                                AnalyticsKey.Keys.CouponValue to coupon,
                                AnalyticsKey.Keys.Screen to AnalyticsKey.Screens.RedeemCopounPopup
                            )
                        )
                        navigator.showMessage(it.Message)
                        hideSheet()
                    } else {
                        navigator.showError(it.Message)
                    }
                }), ({
                    isLoading.set(false)
                    navigator.handleError(it)
                }))
        )
    }
    private fun getBonusSubItem(pos:Int): WalletInfoModel.WalletBonesModel?{
        return if(bonusSubList.size > pos){
            bonusSubList[pos]
        }else null
    }

}
