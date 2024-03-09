package com.rummytitans.sdk.cardgame.ui.payment.viewmodel

import com.rummytitans.sdk.cardgame.R
import com.rummytitans.sdk.cardgame.analytics.AnalyticsHelper
import com.rummytitans.sdk.cardgame.api.APIInterface
import com.rummytitans.sdk.cardgame.data.SharedPreferenceStorageRummy
import com.rummytitans.sdk.cardgame.models.LoginResponseRummy
import com.rummytitans.sdk.cardgame.models.NewPaymentGateWayModel
import com.rummytitans.sdk.cardgame.models.WalletInfoModel
import com.rummytitans.sdk.cardgame.ui.BaseViewModel
import com.rummytitans.sdk.cardgame.ui.payment.PaymentOptionNavigator
import com.rummytitans.sdk.cardgame.utils.ConnectionDetector
import com.rummytitans.sdk.cardgame.utils.alertDialog.AlertdialogModel
import com.rummytitans.sdk.cardgame.utils.setAddMoreGatewayItem
import android.view.View
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.gson.Gson
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
@HiltViewModel
class PaymentOptionViewModel @Inject constructor(
    val prefs: SharedPreferenceStorageRummy,
    val apis: APIInterface,
    val gson: Gson,
    val analyticsHelper: AnalyticsHelper,
    val connectionDetector: ConnectionDetector
) :
    BaseViewModel<PaymentOptionNavigator>() {
    var loginResponse: LoginResponseRummy =
        gson.fromJson(prefs.loginResponse ?: "", LoginResponseRummy::class.java)

    var amount = ObservableField(0.0)
    var balanceModel: WalletInfoModel.Balance? = null
    var paymentThrought = ""
    val validUpiCode = ObservableBoolean(false)

    //show searchBar in BankList BottomSheet
    val isSearchAllow = ObservableBoolean(false)
    val isSearchedBankAvailable = ObservableBoolean(true)
    val regularColor = prefs.regularColor
    val safeColor = prefs.safeColor
    val selectedColor = ObservableField(if (prefs.onSafePlay) safeColor else regularColor)
    val noDataMessage = ObservableField<String>("No Bank Available.")
    val isBottomSheetVisible = ObservableBoolean(false)

    var IsVPAAllow = false
    val isShowAllGatewayList = ObservableBoolean(false)
    var openedGatewayListType = 0

    var _mGateWayResponse = MutableLiveData<NewPaymentGateWayModel>()
    var mGateWayResponse :LiveData<NewPaymentGateWayModel> = _mGateWayResponse
    var returnUrl = ""

    var isUpiApiAllow = true

    var isLoading = ObservableBoolean(true)

    val checkedUpi = ObservableBoolean(false)


    val cardListCount = ObservableInt(0)


    val linkedWallet = ObservableField<NewPaymentGateWayModel.PaymentResponseModel>()
    val _bottomSheetAddUpi = MutableLiveData<Int>(BottomSheetBehavior.STATE_HIDDEN)
    val bottomSheetAddUpi: LiveData<Int>
        get() = _bottomSheetAddUpi

    val _bottomSheetLinkWallet = MutableLiveData<Int>(BottomSheetBehavior.STATE_HIDDEN)
    val bottomSheetLinkWallet: LiveData<Int>
        get() = _bottomSheetLinkWallet

    val _bottomSheetAlert = MutableLiveData<Int>(BottomSheetBehavior.STATE_HIDDEN)
    val bottomSheetAlert: LiveData<Int>
        get() = _bottomSheetAlert

    var validForPinview = ObservableBoolean(false)
    var wrongOtp = ObservableBoolean(false)
    val linkOtpSend = ObservableBoolean(false)
    var linkedOtp = ""
    val isLinkWallet = ObservableBoolean(false)
    val wrongOtpErrorMsg = ObservableField<String>()
    val alertDialogModel = ObservableField<AlertdialogModel>()
    var goldenTicketId = 0
    var addCashCouponID = 0
    var offerIds = ""
    fun onCheckedChange(button: View, b: Boolean) {
        checkedUpi.set(b)
    }

    fun toggleLinkWalletSheet() {
        _bottomSheetLinkWallet.value =
            if (_bottomSheetLinkWallet.value == BottomSheetBehavior.STATE_EXPANDED) {
                if(isShowAllGatewayList.get()){
                    isBottomSheetVisible.set(true)
                }
                BottomSheetBehavior.STATE_HIDDEN
            } else {
                isShowAllGatewayList.set(isBottomSheetVisible.get())
                isBottomSheetVisible.set(false)
                BottomSheetBehavior.STATE_EXPANDED
            }
    }

    fun showHideAlert() {
        _bottomSheetAlert.value =
            if (_bottomSheetAlert.value == BottomSheetBehavior.STATE_EXPANDED) {
                if(isShowAllGatewayList.get()){
                    isBottomSheetVisible.set(true)
                }
                BottomSheetBehavior.STATE_HIDDEN
            } else {
                isShowAllGatewayList.set(isBottomSheetVisible.get())
                isBottomSheetVisible.set(false)
                BottomSheetBehavior.STATE_EXPANDED
            }
    }


    fun hideAllSheet() {
        isBottomSheetVisible.set(false)
        _bottomSheetAddUpi.value = BottomSheetBehavior.STATE_HIDDEN
        navigatorAct.hideKeyboard()
    }

    fun backToLink(){
        linkOtpSend.set(false)
    }

    fun goBack() {
        navigator.goBack()
    }

    fun deleteCardAlert(cardToken: String){
        navigator.let {
            alertDialogModel.set(
                AlertdialogModel("Delete Card",
                    "Are you sure?You want to delete card?",
                    it.getStringResource(R.string.no),
                    it.getStringResource(R.string.yes),
                    { showHideAlert() },
                    {
                        showHideAlert()
                        deleteCard(cardToken)
                    })
            )
        }
        showHideAlert()
    }

    fun delinkWallet(){
        navigator.let {
            alertDialogModel.set(
                AlertdialogModel("Delink Wallet",
                    "Are you sure?You want to Delink Wallet?",
                    it.getStringResource(R.string.no),
                    it.getStringResource(R.string.yes),
                    { showHideAlert() },
                    {
                        showHideAlert()
                        authenticateWallet()
                    })
            )
        }
        showHideAlert()
    }

    fun getPaymentGateWay() {
        isLoading.set(true)
        loginResponse = gson.fromJson(prefs.loginResponse, LoginResponseRummy::class.java)


        val json = JsonObject()
        json.addProperty("Amount",amount.get()?:0.0)
        json.addProperty("OfferIds",offerIds)
        json.addProperty("PassId",goldenTicketId)
        json.addProperty("CoupanId",addCashCouponID)
        val apiInterface = getApiEndPointObject(prefs.appUrl2?:"")
        compositeDisposable.add(
            apiInterface.getPaymentGateWay(
                loginResponse.UserId,
                loginResponse.ExpireToken,
                loginResponse.AuthExpire,
                json
            ).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    isLoading.set(false)
                    if (it.Status) {
                        returnUrl = it.ReturnUrl
                        it.GatewayList.map {model->
                            if (model.Type!=4 || it.IsVPAAllow)
                                setAddMoreGatewayItem(model)
                        }

                        _mGateWayResponse.value = it
                        if(isShowAllGatewayList.get()){
                            isShowAllGatewayList.set(false)
                            _mGateWayResponse.value?.GatewayList?.singleOrNull {
                                it.Type == openedGatewayListType
                            }?.let { gateway ->
                                navigatorAct.showMore(gateway.Type,gateway.List)
                            }
                        }
                    }else{
                        navigator.showError(it.Message)
                    }
                }, {
                    isLoading.set(false)
                    navigator.showError(R.string.something_went_wrong)
                    it.printStackTrace()
                }))
    }

    fun deleteCard(cardToken: String) {
        isLoading.set(true)
        loginResponse = gson.fromJson(prefs.loginResponse, LoginResponseRummy::class.java)
        compositeDisposable.add(
            apis.deleteSaveCard(
                loginResponse.UserId,
                loginResponse.ExpireToken,
                loginResponse.AuthExpire,
                cardToken
            )
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    isLoading.set(false)
                    if (it.Status) {
                        navigator.showMessage(navigator.getStringResource(R.string.card_deleted_succesfully))
                        getPaymentGateWay()
                    }
                }, {
                    isLoading.set(false)
                })
        )
    }

    fun verifyUPI(upiAddress:String){
        loginResponse = gson.fromJson(prefs.loginResponse, LoginResponseRummy::class.java)
        isLoading.set(true)
        compositeDisposable.add(
            apis.verifyUPI(
                loginResponse.UserId,
                loginResponse.ExpireToken,
                loginResponse.AuthExpire,
                upiAddress)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    isLoading.set(false)
                    if (it.Status) {
                        navigatorAct.payViaUpiAddress(upiAddress)
                    }else{
                        navigator.showError(R.string.error_valid_upiCode)
                    }
                }, {
                    isLoading.set(false)
                    navigator.showError(R.string.error_valid_upiCode)
                })
        )
    }

    fun sendOtpForLinkWallet(){
        val json = JsonObject()

        json.addProperty("Wallet",linkedWallet.get()?.Code)
        loginResponse = gson.fromJson(prefs.loginResponse, LoginResponseRummy::class.java)
        isLoading.set(true)
        compositeDisposable.add(
            apis.sendOtpForLinkWallet(
                loginResponse.UserId,
                loginResponse.ExpireToken,
                loginResponse.AuthExpire,
                json
            )
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    isLoading.set(false)
                    if (it.Status) {
                        linkedWallet?.get()?.Id = it.Response.WalletId
                        linkOtpSend.set(true)
                    }else{
                        navigator.showError(it.Message)
                    }
                }, {
                    isLoading.set(false)
                })
        )
    }

    fun authenticateWallet(){
        val json = JsonObject()
        json.addProperty("WalletId",linkedWallet.get()?.Id)
        json.addProperty("Command",if(isLinkWallet.get())"link" else "delink")
        json.addProperty("Otp",linkedOtp)
        json.addProperty("Amount",amount.get())

        loginResponse = gson.fromJson(prefs.loginResponse, LoginResponseRummy::class.java)
        isLoading.set(true)

        compositeDisposable.add(
            apis.authenticateWallet(
                loginResponse.UserId,
                loginResponse.ExpireToken,
                loginResponse.AuthExpire,
                json
            )
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    isLoading.set(false)
                    if (it.Status) {
                        linkOtpSend.set(false)
                        if(bottomSheetLinkWallet.value != BottomSheetBehavior.STATE_HIDDEN){
                            toggleLinkWalletSheet()
                        }
                        if(isLinkWallet.get()){
                            navigatorAct.refreshData(gson.fromJson((gson.toJson(it.Response)),
                                WalletInitializeModel::class.java),linkedWallet.get()?.Id?:"")
                        }else{
                            navigatorAct.refreshData(null,linkedWallet.get()?.Id?:"")
                        }

                        navigator.showMessage(it.Message)
                    }else{
                        navigator.showError(it.Message)
                    }
                }, {
                    isLoading.set(false)
                })
        )
    }

    fun directDebit(token:String, paymentMethod:String){
        val json = JsonObject()
        json.addProperty("OrderId",mGateWayResponse.value?.jusPayData?.OrderId)
        json.addProperty("WalletToken",token)
        json.addProperty("PaymentMethod",paymentMethod)

        loginResponse = gson.fromJson(prefs.loginResponse, LoginResponseRummy::class.java)
        isLoading.set(true)

        compositeDisposable.add(
            apis.directDebit(
                loginResponse.UserId,
                loginResponse.ExpireToken,
                loginResponse.AuthExpire,
                json
            ).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    isLoading.set(false)
                    if (it.Status) {
                        if(it.Response.isNotEmpty()){
                            navigatorAct.directDebited(it.Response)
                        }else{
                            navigator.showMessage(it.Message)
                        }
                    }else{
                        navigator.showError(it.Message)
                    }
                }, {
                    isLoading.set(false)
                    navigator.showError("Something went wrong")
                })
        )
    }

}