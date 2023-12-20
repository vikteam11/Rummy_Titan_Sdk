package com.rummytitans.sdk.cardgame.ui.wallet.winning_conversion

import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.rummytitans.sdk.cardgame.analytics.AnalyticsHelper
import com.rummytitans.sdk.cardgame.api.APIInterface
import com.rummytitans.sdk.cardgame.data.SharedPreferenceStorageRummy
import com.rummytitans.sdk.cardgame.models.LoginResponse
import com.rummytitans.sdk.cardgame.models.WinningConversionContentModel
import com.rummytitans.sdk.cardgame.ui.BaseViewModel
import com.rummytitans.sdk.cardgame.utils.ConnectionDetector
import com.rummytitans.sdk.cardgame.utils.formatInString
import com.rummytitans.sdk.cardgame.widget.MyDialog
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.text.DecimalFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class WinningConversionViewModel @Inject constructor(
    val prefs: SharedPreferenceStorageRummy,
    val gson: Gson,
    val apiInterface: APIInterface,
    val connectionDetector: ConnectionDetector,
    val analyticsHelper: AnalyticsHelper
) : BaseViewModel<WinningConversionNavigator>(){

    var loginResponse: LoginResponse = gson.fromJson(prefs.loginResponse, LoginResponse::class.java)
    var myDialog: MyDialog? = null

    var isLoading = ObservableBoolean(false)
    var amountDepositedSuccessfully = ObservableBoolean(false)
    var showTds = ObservableBoolean(false)
    var conversionStep = ObservableInt(1)
    var regularColor = prefs.regularColor
    var safeColor = prefs.safeColor
    val selectedColor = ObservableField(if (prefs.onSafePlay) safeColor else regularColor)

    var maxAmountToDeposit = 0.0
    var minAmountToDeposit = 0.0
    val title = ObservableField("Enter Winning Amount")
    private val _ranges = MutableLiveData<List<WinningConversionContentModel.WinningConversionRangeModel>>(ArrayList())
    val ranges: LiveData<List<WinningConversionContentModel.WinningConversionRangeModel>> get() = _ranges

    private val _benefits = MutableLiveData<List<WinningConversionContentModel.WinningConversionBenefitModel>>(ArrayList())
    val benefits: LiveData<List<WinningConversionContentModel.WinningConversionBenefitModel>> get() = _benefits

    private val _availedBenefits = MutableLiveData<List<WinningConversionContentModel.WinningConversionBenefitModel>>(ArrayList())
    val availedBenefits: LiveData<List<WinningConversionContentModel.WinningConversionBenefitModel>> get() = _availedBenefits


    val depositAmount = ObservableField(0.0)
    val depositAmountStr = ObservableField("")
    val depositAmountMessage = ObservableField("")
    val errorMessage = ObservableField("")
    val cashBonusAmt = ObservableField("")
    val tdsSavingAmt = ObservableField("")
    val totalWinningAmount = ObservableField(0.0)
    val selectWinningAmount = ObservableField(0.0)
    val depositAmountList = arrayListOf<String>()
    fun calculateDepositAmount(winningAmount:Double,percent:Double){
        selectWinningAmount.set(winningAmount)
        val bonusAmount = (percent/100)*winningAmount
        depositAmount.set(winningAmount+bonusAmount)
        depositAmountStr.set("₹${depositAmount.get()?.formatInString()} Total Deposit")
    }
    fun changeConversionStep(step:Int){
        conversionStep.set(step)
    }
    fun getWinningConversionRange() {
        if (!connectionDetector.isConnected) {
            return
        }
        isLoading.set(true)
        compositeDisposable.add(
            apiInterface.getWinningConversionRange(
                loginResponse.UserId.toString(),
                loginResponse.ExpireToken,
                loginResponse.AuthExpire,
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

                    if(it.Status){
                        maxAmountToDeposit = it.Response?.rangeList?.maxOf { it.maxAmount }?:0.0
                        minAmountToDeposit = it.Response?.rangeList?.minOf { it.minAmount }?:0.0
                        title.set("Enter Winning Amount(₹${formatAmount(minAmountToDeposit.toInt().toLong())} - " +
                                "₹${formatAmount(maxAmountToDeposit.toInt().toLong())})")

                        it.Response.rangeList.map {
                            val winningAmount = totalWinningAmount.get()?:0.0
                            it.enable = (winningAmount > it.maxAmount || (it.minAmount..it.maxAmount).contains(winningAmount))
                        }

                        it.Response.benefits.forEachIndexed {index,item->
                            item.enable  = it.Response?.benefits?.size != 1
                            item.amount = item.bonusPercentage+"%"
                        }
                        showTds.set((it.Response?.benefits?.size?:0) >=2 )
                        _ranges.value = it.Response.rangeList
                        _benefits.value = it.Response.benefits
                    }else{
                        navigator.showError(it.Message)
                    }
                }), ({
                    isLoading.set(false)
                    navigator.handleError(it)
                }))
        )
    }


    fun formatAmount(amount: Long): String {
        if (amount < 1000) {
            return amount.toString()
        }
        if (amount < 100000) {
            val value = (amount / 1000.0f)
            val formattedValue = DecimalFormat("0.#K").format(value);
            return formattedValue
        } else {
            val value = (amount / 100000.0f)
            val formattedValue = DecimalFormat("0.#L").format(value);
            return formattedValue
        }
    }

    fun convertToDeposit() {
        if (!connectionDetector.isConnected) {
            return
        }

        val json = JsonObject()
        json.addProperty("Amount",selectWinningAmount.get())
        isLoading.set(true)
        compositeDisposable.add(
            apiInterface.convertToDeposit(
                loginResponse.UserId.toString(),
                loginResponse.ExpireToken,
                loginResponse.AuthExpire,
                json
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
                    if(it.Status){
                        depositAmountMessage.set(
                            "Your winnings ₹${selectWinningAmount.get()?.formatInString()} have been successfully converted into deposits " +
                                    "₹${depositAmount.get()?.formatInString()}"
                        )
                        depositAmountList.clear()
                        depositAmountList.add("₹${selectWinningAmount.get()}")
                        depositAmountList.add("₹${depositAmount.get()}")

                        it.Response.forEachIndexed {index,item->
                            item.enable  = it.Response?.size != 1
                        }
                        _availedBenefits.value = it.Response
                        navigatorAct.onDepositSuccessfully()
                    }else{
                        navigator.showError(it.Message)
                        //errorMessage.set(it.Message)
                    }
                }), ({
                    isLoading.set(false)
                    //navigator.handleError(it)
                    amountDepositedSuccessfully.set(false)
                }))
        )
    }
}