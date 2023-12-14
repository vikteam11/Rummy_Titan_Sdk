package com.rummytitans.sdk.cardgame.ui.wallet.viewmodel

import android.app.Activity
import android.content.Context
import com.rummytitans.sdk.cardgame.R
import com.rummytitans.sdk.cardgame.analytics.AnalyticsHelper
import com.rummytitans.sdk.cardgame.analytics.AnalyticsKey
import com.rummytitans.sdk.cardgame.api.APIInterface
import com.rummytitans.sdk.cardgame.data.SharedPreferenceStorageRummy
import com.rummytitans.sdk.cardgame.models.*
import com.rummytitans.sdk.cardgame.ui.BaseViewModel
import com.rummytitans.sdk.cardgame.ui.wallet.WithdrawalNavigator
import com.rummytitans.sdk.cardgame.utils.ConnectionDetector
import com.rummytitans.sdk.cardgame.widget.MyDialog
import android.text.TextUtils
import android.view.inputmethod.InputMethodManager
import androidx.core.os.bundleOf
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.rummytitans.sdk.cardgame.models.*
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.text.DecimalFormat
import javax.inject.Inject
@HiltViewModel
class WithdrawViewModel @Inject constructor(
    val prefs: SharedPreferenceStorageRummy,
    val gson: Gson,
    val apiInterface: APIInterface,
    val connectionDetector: ConnectionDetector, val analyticsHelper: AnalyticsHelper
) : BaseViewModel<WithdrawalNavigator>() {
    val currentBalance = MutableLiveData(0.0)
    val isValidAmount = ObservableBoolean(false)
    val withdrawtext = MutableLiveData("")
    val withdrawalModel = MutableLiveData<WithdrawModel>()
    var isWithdrawalEnable = ObservableBoolean(true)
    val isBankDetailOpen = ObservableBoolean(false)

    var winnings = 0.0

    val loginResponse: LoginResponse = gson.fromJson(prefs.loginResponse, LoginResponse::class.java)

    var myDialog: MyDialog? = null
    val isLoading = ObservableBoolean(false)


    var withdrawalMethod = ObservableField<WithdrawalMethodModel>()
    var withdrawalStatusModel= ObservableField<WithdrawalSuccessCustomModel>()
    val showWithdrawalStatusView = ObservableInt(0)
    val instantWithdrawalWarning = ObservableField("")

    val isWithdrawButtonEnabled = ObservableBoolean(true)
    val isTdsCalculating = ObservableBoolean(false)

    val regularColor = prefs.regularColor
    val safeColor = prefs.safeColor
    val selectedColor = ObservableField(if (prefs.onSafePlay) safeColor else regularColor)
    private val _tdsList = MutableLiveData<List<WithdrawalTdsModel>>()
    val tdsList: LiveData<List<WithdrawalTdsModel>> = _tdsList
    private var lastCalculateAmount= -1
    val finalWithdrawalAmount = ObservableField("")
    var withdrawalAmount =ObservableField(0)
    var finalWithdrawal: WithdrawalTdsModel?= null

    fun withdrawalMyAmount() {
        if (!connectionDetector.isConnected) {
            myDialog?.retryInternetDialog {
                isLoading.set(true)
                withdrawalMyAmount()
            }
            isLoading.set(true)
            return
        }

        analyticsHelper.fireEvent(
            AnalyticsKey.Names.WithdrawMoneyClicked, bundleOf(
                AnalyticsKey.Keys.Amount to finalWithdrawalAmount.get(),
                AnalyticsKey.Keys.WithdrawType to withdrawalMethod.get()?.Name,
                AnalyticsKey.Keys.Screen to AnalyticsKey.Screens.Withdraw
            )
        )

        isWithdrawButtonEnabled.set(false)
        isLoading.set(true)
        val json = JsonObject()
        json.addProperty("Amount",withdrawalAmount.get().toString())
        json.addProperty("WithdrawalOption", withdrawalMethod.get()?.ID.toString())
        compositeDisposable.add(
            apiInterface.withdrawMoney(
                loginResponse.UserId.toString(),
                loginResponse.ExpireToken,
                loginResponse.AuthExpire,
              json
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(({
                    if (it.Status) {

                        analyticsHelper.fireEvent(
                            AnalyticsKey.Names.WithdrawalRequested, bundleOf(
                                AnalyticsKey.Keys.Amount to withdrawalAmount.get(),
                                AnalyticsKey.Keys.Screen to AnalyticsKey.Screens.Withdraw
                            )
                        )
                    } else {
                        isWithdrawButtonEnabled.set(true)
                    }
                    isLoading.set(false)
                    onWithdrawalResponseReceive(it)
                }), ({
                    onWithdrawalResponseReceive(null,it.message?:navigator.getStringResource(R.string.something_went_wrong_try_again))
                    isLoading.set(false)
                    isWithdrawButtonEnabled.set(true)
                }))
        )
    }
    fun toggleBankDetails(){
        isBankDetailOpen.set(!isBankDetailOpen.get())
    }

    fun onWithdrawalResponseReceive(apiResponse: BaseModel<String>?, extraMsg:String=""){
        withdrawalMethod.get()?.let { withdrawalMethod ->
            WithdrawalSuccessCustomModel()
                .let {
                it.colorCode = selectedColor.get()
                it.ID = withdrawalMethod.ID

                it.AccountNo = if (withdrawalMethod.ID == "2")
                    withdrawalModel.value?.PaytmBankNo
                else
                    withdrawalModel.value?.BankDetail?.AccountNo

                it.TnxId = apiResponse?.TnxId?:""
                    it.Title =  apiResponse?.Title?:""
                it.RequestDate = apiResponse?.ReauestDate?:""
                it.Message = apiResponse?.Message?:extraMsg
                it.AmountWithdrawal = finalWithdrawal?.amount?:0.0
                currentBalance.value?.minus(withdrawalAmount.get()?.toDouble()?:0.0)?.let { value->
                    it.AmountAvailable=String.format("%.2f", value)
                }
                it.withdrawalStatus = apiResponse?.Status ?: false
                it.withdrawalHeader=if (withdrawalMethod.isWithdrawalInstant)
                    navigator.getStringResource(R.string.instant_withdrawal_done_header)
                else navigator.getStringResource(R.string.normal_withdrawal_done_header)
                withdrawalStatusModel.set(it)
            }
        }
        navigatorAct.onWithdrawalResponseReceived(apiResponse?.Status?:false)
    }

    fun getWithdrawalOption() {
        if (!connectionDetector.isConnected) {
            myDialog?.noInternetDialog {
                isLoading.set(true)
                getWithdrawalOption()
            }
            isLoading.set(true)
            return
        }

        isLoading.set(true)
        compositeDisposable.add(
            apiInterface.getWithdrawalOptions(
                loginResponse.UserId,
                loginResponse.ExpireToken,
                loginResponse.AuthExpire,
                prefs.seletedLanguage?:"en"
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(({
                    isLoading.set(false)
                    if (it.isUpdate) {
                        navigatorAct.onUpdateRequired(it.updateMessage)
                    }
                    if (it.Status) {
                        it.Response.WithdrawalOptions.sortByDescending { it.ID.toInt() }
                        it.Response.WithdrawalOptions.apply {
                            filter { !it.Disable }.also { filterList->
                                filterList.elementAtOrNull(0)?.let {walletModel->
                                    walletModel.isFirstItem=true
                                    walletModel.applicableMethod=true
                                    withdrawalMethod.set(walletModel)
                                }
                                filterList.elementAtOrNull(1)?.let {walletModel->
                                    walletModel.isFirstItem=false
                                }
                            }
                            val model=firstOrNull { it.Disable && !TextUtils.isEmpty(it.DisableMessage) }
                            instantWithdrawalWarning.set(model?.DisableMessage?:"")
                        }
                        withdrawalModel.value = it.Response
                    }
                    else
                        navigator.showError(it.Message)

                }), ({
                    navigator.handleError(it)
                    isLoading.set(false)
                }))
        )
    }

    fun getTdsOnAmount(amount:Int,context: Activity) {
        if (!connectionDetector.isConnected) {
            myDialog?.noInternetDialog {
                isLoading.set(true)
                getTdsOnAmount(amount,context)
            }
            isLoading.set(true)
            return
        }
        lastCalculateAmount = amount
        isTdsCalculating.set(true)
        val apis = getApiEndPointObject(prefs.appUrl2 ?:"")
        val json = JsonObject()
        json.addProperty("amount",amount)

        compositeDisposable.add(
            apis.getTdsOnAmount(
                loginResponse.UserId,
                loginResponse.ExpireToken,
                loginResponse.AuthExpire,
                prefs.seletedLanguage?:"en",
                json
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(({
                    isTdsCalculating.set(false)
                    hideKeyboard(context)
                    isWithdrawButtonEnabled.set(it.Status)
                    if (it.Status){
                        if(it.Response.isNotEmpty()){
                            it.Response.elementAtOrNull(2)?.isNegative=true
                            finalWithdrawal = it.Response[it.Response.size-1]
                            finalWithdrawalAmount.set(DecimalFormat("##.##").format(finalWithdrawal?.amount?:0.0))
                        }
                        _tdsList.value = it.Response
                    } else
                        navigator.showError(it.Message)
                }), ({
                    isWithdrawButtonEnabled.set(false)
                    navigator.handleError(it)
                    isTdsCalculating.set(false)
                }))
        )
    }

    private fun hideKeyboard(context: Activity) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(context.currentFocus?.windowToken, 0)
    }

}
