package com.rummytitans.playcashrummyonline.cardgame.ui.verifications.viewmodels

import com.rummytitans.playcashrummyonline.cardgame.R
import com.rummytitans.playcashrummyonline.cardgame.analytics.AnalyticsHelper
import com.rummytitans.playcashrummyonline.cardgame.api.APIInterface
import com.rummytitans.playcashrummyonline.cardgame.data.SharedPreferenceStorageRummy
import com.rummytitans.playcashrummyonline.cardgame.models.LoginResponse
import com.rummytitans.playcashrummyonline.cardgame.ui.BaseViewModel
import com.rummytitans.playcashrummyonline.cardgame.ui.verifications.OnIFSCCodeCheck
import com.rummytitans.playcashrummyonline.cardgame.ui.verifications.RequestVarificationInterface
import com.rummytitans.playcashrummyonline.cardgame.utils.ConnectionDetector
import com.rummytitans.playcashrummyonline.cardgame.widget.MyDialog
import android.text.TextUtils
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.rummytitans.playcashrummyonline.cardgame.ui.base.BaseNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject
@HiltViewModel
class BankVerificationViewModel @Inject constructor(
    val prefs: SharedPreferenceStorageRummy,
    val gson: Gson,
    val apiInterface: APIInterface,
    val connectionDetector: ConnectionDetector, var analyticsHelper: AnalyticsHelper
) : BaseViewModel<BaseNavigator>() {
    var isProgressLoading = ObservableBoolean(false)
    var state = ArrayList<String>()
    var stateList = ObservableField<MutableList<String>>(state)
    var imageUrl = MutableLiveData<String>("")
    var myDialog: MyDialog? = null
    val getImageUrl: LiveData<String>
        get() = imageUrl

    var mRequestVarifyInterface:RequestVarificationInterface?=null
    lateinit var ifscCheck: OnIFSCCodeCheck

    var ifscCodeInterface: APIInterface? = null

    val regularColor = prefs.regularColor
    val safeColor = prefs.safeColor
    val selectedColor = ObservableField(if (prefs.onSafePlay) safeColor else regularColor)
    val loginResponse: LoginResponse = gson.fromJson(prefs.loginResponse, LoginResponse::class.java)

    fun SubmitBankDetails(
        username: String, number: String, renumber: String,
        ifsc: String, bank_name: String, bank_branch: String
    ) {
        if (!isValidDetails(username, number, renumber, ifsc, bank_name, bank_branch)) return

        if (!connectionDetector.isConnected) {
            myDialog?.noInternetDialog {
                SubmitBankDetails(username, number, renumber, ifsc, bank_name, bank_branch)
            }
            isProgressLoading.set(false)
            return
        }

        val file = File(getImageUrl.value.toString())
        isProgressLoading.set(true)
        compositeDisposable.add(
            apiInterface.addBankDetail(
                loginResponse.UserId.toString(),
                loginResponse.ExpireToken,
                loginResponse.AuthExpire,
                username.toRequestBody("text/plain".toMediaTypeOrNull()),
                number.toRequestBody("text/plain".toMediaTypeOrNull()),
                ifsc.toRequestBody("text/plain".toMediaTypeOrNull()),
                bank_name.toRequestBody("text/plain".toMediaTypeOrNull()),
                bank_branch.toRequestBody("text/plain".toMediaTypeOrNull()),
                file.asRequestBody("image/*".toMediaTypeOrNull())
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(({
                    if (it.Status) {
                        navigator.showMessage(it.Message)
                    mRequestVarifyInterface?.fireBranchEvent(loginResponse.UserId)
                    } else
                        navigator.showError(it.Message)
                    isProgressLoading.set(false)
                }), ({
                    isProgressLoading.set(false)
                    navigator.handleError(it)
                }))
        )
    }

    fun isValidDetails(
        username: String,
        number: String,
        renumber: String,
        ifsc: String,
        bank_name: String,
        bank_branch: String
    ): Boolean {
        if (TextUtils.isEmpty(imageUrl.value)) {
            navigator.showError(navigator.getStringResource(R.string.select_bank_proof))
            return false
        } else if (TextUtils.isEmpty(username)) {
            navigator.showError(navigator.getStringResource(R.string.enter_your_name))
            return false
        } else if (TextUtils.isEmpty(number)) {
            navigator.showError(navigator.getStringResource(R.string.enter_your_acc_number))
            return false
        } else if (TextUtils.isEmpty(renumber)) {
            navigator.showError(navigator.getStringResource(R.string.reenter_your_acc_number))
            return false
        } else if (!number.equals(renumber, ignoreCase = true)) {
            navigator.showError(navigator.getStringResource(R.string.acc_number_not_matched))
            return false
        } else if (TextUtils.isEmpty(ifsc)) {
            navigator.showError(navigator.getStringResource(R.string.enter_ifsc_code))
            return false
        } else if (ifsc.length != 11) {
            navigator.showError(navigator.getStringResource(R.string.enter_valid_ifsc_code))
            return false
        } else if (TextUtils.isEmpty(bank_name)) {
            navigator.showError(navigator.getStringResource(R.string.enter_bank_name))
            return false
        } else if (TextUtils.isEmpty(bank_branch)) {
            navigator.showError(navigator.getStringResource(R.string.enter_bank_branch))
            return false
        }
        return true
    }
}