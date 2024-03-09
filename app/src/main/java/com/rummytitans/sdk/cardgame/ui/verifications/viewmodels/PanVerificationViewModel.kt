package com.rummytitans.sdk.cardgame.ui.verifications.viewmodels

import com.rummytitans.sdk.cardgame.R
import com.rummytitans.sdk.cardgame.analytics.AnalyticsHelper
import com.rummytitans.sdk.cardgame.api.APIInterface
import com.rummytitans.sdk.cardgame.data.SharedPreferenceStorageRummy
import com.rummytitans.sdk.cardgame.models.LoginResponseRummy
import com.rummytitans.sdk.cardgame.ui.BaseViewModel
import com.rummytitans.sdk.cardgame.ui.verifications.RequestVarificationInterface
import com.rummytitans.sdk.cardgame.utils.ConnectionDetector
import com.rummytitans.sdk.cardgame.utils.validPancard
import com.rummytitans.sdk.cardgame.widget.MyDialog
import android.text.TextUtils
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.rummytitans.sdk.cardgame.ui.base.BaseNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject

@HiltViewModel
class PanVerificationViewModel @Inject constructor(
    val prefs: SharedPreferenceStorageRummy,
    val gson: Gson,
    val apiInterface: APIInterface,
    val connectionDetector: ConnectionDetector, var analyticsHelper: AnalyticsHelper
) : BaseViewModel<BaseNavigator>() {
    val loginResponse: LoginResponseRummy = gson.fromJson(prefs.loginResponse, LoginResponseRummy::class.java)
    var myDialog: MyDialog? = null
    var isProgressLoading = ObservableBoolean(false)
    var state = ArrayList<String>()
    var stateList = ObservableField<MutableList<String>>(state)
    var imageUrl = MutableLiveData<String>("")
    var selectedState = ""
    val getImageUrl: LiveData<String>
        get() = imageUrl

    val regularColor = prefs.regularColor
    val safeColor = prefs.safeColor
    val selectedColor = ObservableField(if (prefs.onSafePlay) safeColor else regularColor)
    var mRequestVarifyInterface: RequestVarificationInterface?=null

    fun getState() {
        if (!connectionDetector.isConnected) {
            myDialog?.noInternetDialog {
                getState()
            }
            isProgressLoading.set(false)
            return
        }
        compositeDisposable.add(
            apiInterface.getStateList().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (it.Status) {
                        val states = ArrayList<String>()
                        states.add(navigator.getStringResource(R.string.select_your_state))
                        for (item in it.Response) {
                            states.add(item.StateName)
                        }
                        stateList.set(states as MutableList<String>)
                    }
                }, {
                    navigator.handleError(it)
                })

        )
    }

    fun SubmitPanDetails(name: String, number: String, dob: String) {
        if (!isValidDetails(getImageUrl.value.toString(), name, number, dob, selectedState)) return

        if (!connectionDetector.isConnected) {
            myDialog?.noInternetDialog {
                SubmitPanDetails(name, number, dob)
            }
            isProgressLoading.set(false)
            return
        }


        val file = File(getImageUrl.value.toString())
        isProgressLoading.set(true)
        compositeDisposable.add(
            apiInterface.addPanCardDetail(
                loginResponse.UserId.toString(),
                loginResponse.ExpireToken,
                loginResponse.AuthExpire,
                name.toRequestBody("text/plain".toMediaTypeOrNull()),
                number.toRequestBody("text/plain".toMediaTypeOrNull()),
                dob.toRequestBody("text/plain".toMediaTypeOrNull()),
                selectedState.toRequestBody("text/plain".toMediaTypeOrNull()),
                file.asRequestBody("image/*".toMediaTypeOrNull())
            ).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(({
                    if (it.Status) {
                        mRequestVarifyInterface?.fireBranchEvent(loginResponse.UserId)
                        navigator.showMessage(it.Message)
                    } else {
                        navigator.showError(it.Message)
                    }
                    isProgressLoading.set(false)
                }), ({
                    isProgressLoading.set(false)
                    navigator.handleError(it)
                }))
        )
    }

    fun isValidDetails(imageurl: String, name: String, number: String, dob: String, state: String): Boolean {
        if (TextUtils.isEmpty(imageurl)) {
            navigator.showError(navigator.getStringResource(R.string.select_pan_card_image))
            return false
        } else if (TextUtils.isEmpty(name)) {
            navigator.showError(navigator.getStringResource(R.string.enter_name_on_card))
            return false
        } else if (TextUtils.isEmpty(number)) {
            navigator.showError(navigator.getStringResource(R.string.enter_pan_number))
            return false
        } else if (number.length != 10) {
            navigator.showError(navigator.getStringResource(R.string.enter_valid_pan_number))
            return false
        } else if (TextUtils.isEmpty(dob)) {
            navigator.showError(navigator.getStringResource(R.string.select_dob))
            return false
        } else if (!validPancard(number)) {
            navigator.showError(navigator.getStringResource(R.string.enter_valid_pan_number))
            return false
        } else if (TextUtils.isEmpty(state)) {
            navigator.showError(navigator.getStringResource(R.string.select_state))
            return false
        }
        return true
    }
}