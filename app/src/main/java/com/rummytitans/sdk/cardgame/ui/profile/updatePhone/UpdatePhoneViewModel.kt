package com.rummytitans.sdk.cardgame.ui.profile.updatePhone

import com.rummytitans.sdk.cardgame.R
import com.rummytitans.sdk.cardgame.api.APIInterface
import com.rummytitans.sdk.cardgame.data.SharedPreferenceStorageRummy
import com.rummytitans.sdk.cardgame.models.LoginResponse
import com.rummytitans.sdk.cardgame.ui.BaseViewModel
import com.rummytitans.sdk.cardgame.utils.ConnectionDetector
import com.rummytitans.sdk.cardgame.utils.validMobile
import com.rummytitans.sdk.cardgame.utils.validOTP
import android.os.CountDownTimer
import android.text.TextUtils
import androidx.core.os.bundleOf
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.rummytitans.sdk.cardgame.analytics.AnalyticsHelper
import com.rummytitans.sdk.cardgame.analytics.AnalyticsKey
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.concurrent.TimeUnit
import javax.inject.Inject
@HiltViewModel
class UpdatePhoneViewModel @Inject constructor(
    val prefs: SharedPreferenceStorageRummy,
    val gson: Gson,
    val apiInterface: APIInterface,
    val connectionDetector: ConnectionDetector,
    val analyticsHelper: AnalyticsHelper
) : BaseViewModel<UpdatePhoneNavigator>() {
    companion object{
        const val UPDATE_REQUEST_OTP=1
        const val VERIFY_OTP=2
    }

    val loginResponse: LoginResponse = gson.fromJson(prefs.loginResponse, LoginResponse::class.java)
    val isLoading = ObservableBoolean(false)
    val safeSelected = MutableLiveData(false)

    val regularColor = prefs.regularColor
    val safeColor = prefs.safeColor
    val selectedColor = ObservableField(if (prefs.onSafePlay) safeColor else regularColor)
    val showError=ObservableBoolean(false)
    val mobileNumber = ObservableField("")
    var timeIsOver = ObservableBoolean(false)
    var remainTimeText = ObservableField<String>()
    var updateStep= ObservableInt(UPDATE_REQUEST_OTP)
    var otpText=ObservableField<String>("")
    private var otpTimer: CountDownTimer? = null


    fun onSubmitClick(){
        if (updateStep.get()== UPDATE_REQUEST_OTP){
            if (!TextUtils.isEmpty(mobileNumber.get()) && validMobile(mobileNumber.get()?:""))
                requestOTP()
            else
                navigatorAct.showMobileError(R.string.err_invalid_mobile_number)
        }else if(updateStep.get() == VERIFY_OTP){
            if (!TextUtils.isEmpty(otpText.get()) && validOTP(otpText.get()?:""))
                verifyOTP()
            else
                 showError.set(true)
        }
    }

    fun onMobileSubmit(){
        if (updateStep.get()== UPDATE_REQUEST_OTP){
            if (!TextUtils.isEmpty(mobileNumber.get()) && validMobile(mobileNumber.get()?:""))
                requestOTP()
        }
    }

    fun onEditMobileClick(){
        updateStep.set(UPDATE_REQUEST_OTP)
        finishTimer()
        showError.set(false)
    }

    fun onOTPSubmit(){
        if (!TextUtils.isEmpty(otpText.get()) && validOTP(otpText.get()?:""))
            verifyOTP()
    }

    fun requestOTP(resendOtp : Boolean = false) {
        apiCall(apiInterface.requestOtpForUpdate(
            loginResponse.UserId, mobileNumber.get().toString(),
            prefs.androidId.toString(), loginResponse.ExpireToken, loginResponse.AuthExpire
        ), {
            updateStep.set(VERIFY_OTP)
            if(resendOtp){
                timeIsOver.set(false)
                navigator.showMessage(it.Message)
            }
            startTimer()
            navigatorAct.onRequestFocusOtp()
        },expireTokan = {
                logoutStatus(apiInterface, loginResponse.UserId, prefs.androidId ?: "", "0")
                prefs.loginResponse = gson.toJson(LoginResponse())
                prefs.loginCompleted = false
                navigator.logoutUser()
        })
    }

    private  fun verifyOTP() {
        apiCall(apiInterface.verifyOtpForUpdateMobile(
            mobileNumber.get().toString(),
            otpText.get()?:"",
            loginResponse.UserId,
            loginResponse.ExpireToken,
            loginResponse.AuthExpire ?: ""
        ), {
            loginResponse.Mobile=mobileNumber.get()
            prefs.loginResponse=gson.toJson(loginResponse)
            navigator.showMessage(it.Message)
            analyticsHelper.fireEvent(
                AnalyticsKey.Names.MobileNoUpdateDone, bundleOf(
                    AnalyticsKey.Keys.NewMobileNumber to mobileNumber.get().toString(),
                    AnalyticsKey.Keys.Screen to AnalyticsKey.Screens.UpdatePhone,
                )
            )
            navigatorAct.onSuccessUpdate()
        },expireTokan = {
                logoutStatus(apiInterface, loginResponse.UserId, prefs.androidId ?: "", "0")
                prefs.loginResponse = gson.toJson(LoginResponse())
                prefs.loginCompleted = false
                navigator.logoutUser()
        })
    }

    private fun startTimer() {
        otpTimer?.cancel()
        otpTimer = object : CountDownTimer(59 * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                String.format(
                    "%02d",
                    TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(
                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)
                    )
                ).let {
                    remainTimeText.set(it)
                }
            }
            override fun onFinish() {
                timeIsOver.set(true)
            }
        }.start()
    }

    fun finishTimer() {
        otpTimer?.onFinish()
        timeIsOver.set(false)
        showError.set(false)
    }

}