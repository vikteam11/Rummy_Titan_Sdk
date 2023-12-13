package com.rummytitans.playcashrummyonline.cardgame.ui.wallet.withdrawal.viewmodel

import com.rummytitans.playcashrummyonline.cardgame.analytics.AnalyticsHelper
import com.rummytitans.playcashrummyonline.cardgame.api.APIInterface
import com.rummytitans.playcashrummyonline.cardgame.data.SharedPreferenceStorage
import com.rummytitans.playcashrummyonline.cardgame.ui.BaseViewModel
import com.rummytitans.playcashrummyonline.cardgame.ui.wallet.withdrawal.WithdrawalDetailNavigator
import com.rummytitans.playcashrummyonline.cardgame.utils.ConnectionDetector
import com.rummytitans.playcashrummyonline.cardgame.utils.WebViewUrls
import com.rummytitans.playcashrummyonline.cardgame.widget.MyDialog
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.rummytitans.playcashrummyonline.cardgame.models.LoginResponse
import com.rummytitans.playcashrummyonline.cardgame.models.WithdrawalDetailModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
@HiltViewModel
class WithdrawDetailViewModel @Inject constructor(
    val prefs: SharedPreferenceStorage,
    val gson: Gson,
    val apiInterface: APIInterface,
    val connectionDetector: ConnectionDetector, val analyticsHelper: AnalyticsHelper
) : BaseViewModel<WithdrawalDetailNavigator>() {
    val loginResponse: LoginResponse = gson.fromJson(prefs.loginResponse, LoginResponse::class.java)

    var myDialog: MyDialog? = null
    val isLoading = ObservableBoolean(false)

    var withdrawalDetail = MutableLiveData<WithdrawalDetailModel>()

    val regularColor = prefs.regularColor
    val safeColor = prefs.safeColor
    val selectedColor = ObservableField(if (prefs.onSafePlay) safeColor else regularColor)
    var mTransactionID=""


    val STATUS_FAILED: Byte = 3
    val STATUS_QUEUE: Byte = 1
    val STATUS_SUCCESS: Byte = 2
    var withdrawalStatus = ObservableField(STATUS_QUEUE)

    fun cancelWithdrawalRequest() {
        if (!connectionDetector.isConnected) {
            myDialog?.retryInternetDialog {
                isLoading.set(true)
                cancelWithdrawalRequest()
            }
            isLoading.set(true)
            return
        }
        isLoading.set(true)
        compositeDisposable.add(
            apiInterface.cancelWithdrawalDetail(
                loginResponse.UserId.toString(),
                loginResponse.ExpireToken,
                loginResponse.AuthExpire,
                mTransactionID
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(({
                    if (it.Status) {
                        navigatorAct.onWithdrawalCancelSuccessful()
                       getWithdrawalDetailAsync()
                    } else {
                        navigator.showError(it.Message)
                    }
                    isLoading.set(false)
                }), ({
                    navigator.handleError(it)
                    isLoading.set(false)
                }))
        )
    }

    fun getWebUrls(url: String): String {
        return WebViewUrls.AppDefaultURL + prefs.seletedLanguage + url
    }

    fun getWithdrawalDetailAsync() {
        if (!connectionDetector.isConnected) {
            myDialog?.retryInternetDialog {
                isLoading.set(true)
                getWithdrawalDetailAsync()
            }
            isLoading.set(true)
            return
        }
        isLoading.set(true)

        compositeDisposable.add(
            apiInterface.getWithdrawalDetail(
                loginResponse.UserId.toString(),
                loginResponse.ExpireToken,
                loginResponse.AuthExpire,
                mTransactionID
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(({
                    if (it.Status) {
                        updateList(it.Response)
                        withdrawalDetail.value = it.Response
                    } else {
                        navigator.showError(it.Message)
                    }
                    isLoading.set(false)
                }), ({
                    navigator.handleError(it)
                    isLoading.set(false)
                }))
        )
    }

    private fun updateList(response: WithdrawalDetailModel) {

        response.statusDetails?.apply {
            when {
                response.statusDetails.indexOfFirst { it.cancel } > -1 ->
                    withdrawalStatus.set(STATUS_FAILED)

                elementAtOrNull(size- 1)?.isDone?:false -> withdrawalStatus.set(STATUS_SUCCESS)

                else -> withdrawalStatus.set(STATUS_QUEUE)
            }
        }

        with(response.statusDetails) {

            indexOfFirst { !it.cancel && !it.isDone }.let { startIndex ->
                if (startIndex == -1) return@let
                for (i in startIndex..size) {
                    elementAtOrNull(i)?.isUpcoming = true
                }
                elementAtOrNull(startIndex - 1)?.isCurrent = true
            }
            indexOfFirst { it.cancel }.let { startIndex ->
                if (startIndex == -1) return@let
                for (i in startIndex + 1..size) {
                    elementAtOrNull(i)?.cancel = true
                }
                elementAtOrNull(startIndex)?.isCurrent = true
            }
            elementAtOrNull(size - 1)?.let {
                if (it.isDone || it.cancel) {
                    it.isCurrent = true
                }
            }
        }
    }


}
