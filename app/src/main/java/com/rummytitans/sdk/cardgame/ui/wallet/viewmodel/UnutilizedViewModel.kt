package com.rummytitans.sdk.cardgame.ui.wallet.viewmodel

import com.rummytitans.sdk.cardgame.api.APIInterface
import com.rummytitans.sdk.cardgame.data.SharedPreferenceStorageRummy
import com.rummytitans.sdk.cardgame.models.LoginResponseRummy
import com.rummytitans.sdk.cardgame.ui.BaseViewModel
import com.rummytitans.sdk.cardgame.utils.ConnectionDetector
import com.rummytitans.sdk.cardgame.widget.MyDialog
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.rummytitans.sdk.cardgame.ui.base.BaseNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
@HiltViewModel
class UnutilizedViewModel
@Inject constructor(
    val prefs: SharedPreferenceStorageRummy,
    val gson: Gson,
    val apiInterface: APIInterface,
    val connectionDetector: ConnectionDetector
) : BaseViewModel<BaseNavigator>() {
    val currentBalance = MutableLiveData<Double>(0.0)
    val withdrawAmount = MutableLiveData(200)
    val withdrawtext = MutableLiveData<String>("")
    var winnings = 0.0

    val loginResponse: LoginResponseRummy = gson.fromJson(prefs.loginResponse, LoginResponseRummy::class.java)

    var myDialog: MyDialog? = null
    val isLoading = ObservableBoolean(false)

    val isWithdrawButtonEnabled = ObservableBoolean(true)
    val regularColor = prefs.regularColor
    val safeColor = prefs.safeColor
    val selectedColor = ObservableField(if (prefs.onSafePlay) safeColor else regularColor)
    val amountText = ObservableField("")

    fun withdrawUnutilizedAmount(){
        if (!connectionDetector.isConnected) {
            myDialog?.noInternetDialog {
                isLoading.set(true)
                withdrawUnutilizedAmount()
            }
            isLoading.set(true)
            return
        }

        isWithdrawButtonEnabled.set(false)
        isLoading.set(true)
        compositeDisposable.add(
            apiInterface.withdrawUnutilizedAmount(
                loginResponse.UserId.toString(),
                loginResponse.ExpireToken,
                loginResponse.AuthExpire,
                withdrawAmount.value.toString(),
               "0"
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(({
                    if (it.Status) {
                       /* val withdrawBtn = Bundle()
                        withdrawBtn.putInt(
                            AnalyticsConstants.WITHDRAW_AMOUNT,
                            withdrawAmount.value?.toInt() ?: 0
                        )
                        withdrawBtn.putString(
                            AnalyticsConstants.WITHDRAW_MODE,
                            "1"
                        )
                        withdrawBtn.putInt(
                            AnalyticsConstants.TOTAL_BALANCE,
                            currentBalance.value?.plus(
                                withdrawAmount.value?.toInt() ?: 0
                            )?.toInt() ?: 0
                        )
                        withdrawBtn.putInt(
                            AnalyticsConstants.TOTAL_WINNING,
                            winnings.toInt()
                        )

                        MainApplication.fireEvent(
                            AnalyticsEventsKeys.WITHDRAW_MONEY_DONE,
                            withdrawBtn
                        )*/
                        navigator.showMessage(it.Message)
                    } else {
                        navigator.showError(it.Message)
                        isWithdrawButtonEnabled.set(true)
                    }
                    isLoading.set(false)

                }), ({
                    navigator.handleError(it)
                    isLoading.set(false)
                    isWithdrawButtonEnabled.set(true)
                }))
        )
    }

}