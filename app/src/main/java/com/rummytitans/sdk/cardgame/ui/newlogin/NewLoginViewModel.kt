package com.rummytitans.sdk.cardgame.ui.newlogin

import android.os.CountDownTimer
import android.widget.CompoundButton
import androidx.core.os.bundleOf
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt
import com.appsflyer.AppsFlyerLib
import com.google.gson.Gson
import com.rummytitans.sdk.cardgame.BuildConfig
import com.rummytitans.sdk.cardgame.R
import com.rummytitans.sdk.cardgame.analytics.AnalyticsHelper
import com.rummytitans.sdk.cardgame.analytics.AnalyticsKey
import com.rummytitans.sdk.cardgame.api.APIInterface
import com.rummytitans.sdk.cardgame.data.SharedPreferenceStorageRummy
import com.rummytitans.sdk.cardgame.models.BaseModel
import com.rummytitans.sdk.cardgame.models.LoginResponse
import com.rummytitans.sdk.cardgame.ui.BaseViewModel
import com.rummytitans.sdk.cardgame.utils.AnalyticsConstants
import com.rummytitans.sdk.cardgame.utils.AnalyticsEventsKeys
import com.rummytitans.sdk.cardgame.utils.ConnectionDetector
import com.rummytitans.sdk.cardgame.utils.locationservices.utils.emptyJson
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class NewLoginViewModel @Inject constructor(
    val apiInterface: APIInterface, val prefs: SharedPreferenceStorageRummy,
    val gson: Gson, val connectionDetector: ConnectionDetector, val analyticsHelper: AnalyticsHelper
) : BaseViewModel<NewLoginNavigator>(connectionDetector) {

    val MOBILE_NUMBER = 1
    val OTP = 2
    var TWO_WAY_PROCESS = 3
    val loginStep = ObservableInt(MOBILE_NUMBER)

    val mobileNumber = ObservableField("")
    val referCode = ObservableBoolean(false)
    val userReferCode = ObservableField("")
    private var otpTimer: CountDownTimer? = null
    var remainTimeText = ObservableField<String>()
    var timeIsOver = ObservableBoolean(false)
    var isHidePassword = ObservableBoolean(true)
    var wrongOtp = ObservableBoolean(false)
    var wrongOtpErrorMSg = ObservableField("")
    var validForEmail = ObservableBoolean(false)
    var validForPassword = ObservableBoolean(false)
    var validForMobile = ObservableBoolean(false)
    var validForPinview = ObservableBoolean(false)
    var validForRefercode = ObservableBoolean(false)
    var clikableTextArray = ArrayList<String>()
    var emailTextArray = ArrayList<String>()
    var phoneTextArray = ArrayList<String>()
    var colorArray = ArrayList<Int>()
    var loginModel: LoginResponse? = null
    val confirmByUser = ObservableBoolean(false)

    var regularColor = prefs.regularColor
    var safeColor = prefs.safeColor
    val selectedColor = ObservableField(safeColor)
    var plainText = ObservableField<String>()
    var mEmail = ""
    var mPassword = ""

    var appsFlyerId = ""

    init {
        kotlin.runCatching {
            wrongOtpErrorMSg.set(navigator.getStringResource(R.string.you_have_entered_wrong_verification_code))
        }
    }

    fun onConfirmByUser(button: CompoundButton?, check: Boolean) {
        confirmByUser.set(check)
    }

    fun addRemoveReferCode(b: Boolean) {
        if (b) navigatorAct.showReferCodeDialog()
        else {
            userReferCode.set("")
            referCode.set(b)
        }
    }

    fun togglePasswordVisibility() {
        isHidePassword.set(!isHidePassword.get())
    }

    fun loginOrRegisterByMobile(isResendOTP: Boolean = false) {
        analyticsHelper.fireEvent(
            AnalyticsKey.Names.ButtonClick,
            bundleOf(
                AnalyticsKey.Keys.ButtonName to if (isResendOTP) AnalyticsKey.Values.ReSendOTP else AnalyticsKey.Values.SendOTP,
                AnalyticsKey.Keys.Screen to AnalyticsKey.Screens.LOGIN,
                AnalyticsKey.Keys.MobileNo to mobileNumber.get()
            )
        )

        if (isParentLoading.get())
            return
        apiCall(apiInterface.requestOtp(
            mobileNumber.get() ?: "",
            prefs.loginAuthTokan ?: "",
            userReferCode.get() ?: "",
            prefs.campaignId ?: ""
        ), {
            loginStep.set(OTP)
            startTimer()
            navigatorAct.onRequestFocusOtp()
        }, { navigator.showError(it.message) })
    }

    fun verifyOTP(otp: String) {

        analyticsHelper.fireEvent(
            AnalyticsKey.Names.ButtonClick, bundleOf(
                AnalyticsKey.Keys.ButtonName to AnalyticsKey.Values.VerifyOTP,
                AnalyticsKey.Keys.Screen to AnalyticsKey.Screens.LOGIN,
                AnalyticsKey.Keys.MobileNo to mobileNumber.get(),
                AnalyticsKey.Keys.OTP to otp
            )
        )

        if (isParentLoading.get())
            return
        apiCall(
            apiInterface.verifyLoginOtp(
                mobileNumber.get().toString(),
                prefs.loginAuthTokan ?: "",
                userReferCode.get().toString(),
                prefs.campaignId.toString(),
                otp,
                prefs.androidId.toString(),
                prefs.firebaseToken.toString(),
                prefs.advertisingId?:"",
                appsFlyerId
            ), {
                prefs.referCode=""
                prefs.loginType = "MOBILE"
                loginModel = gson.fromJson(gson.toJson(it.Response), LoginResponse::class.java)
                fireEvent(loginModel, it.IsRegister)
                verificationCheckup(it)
            })
    }

    fun loginByEmail(email: String, password: String) {
        if (isParentLoading.get())
            return
        mEmail = email
        mPassword = password

        apiCall(
            apiInterface.loginWithEmail(
                email, password,
                prefs.campaignId.toString(),
                prefs.androidId.toString(),
                prefs.advertisingId?:"", appsFlyerId
            ), {
                prefs.loginType = "EMAIL"
                loginModel = gson.fromJson(gson.toJson(it.Response), LoginResponse::class.java)
                loginModel?.apply {
                    if (isTwo) {
                        mobileNumber.set(Mobile)
                        loginStep.set(TWO_WAY_PROCESS)
                        startTimer()
                        navigatorAct.onRequestFocusOtp()
                        return@apiCall
                    }
                }
                fireEvent(loginModel, it.IsRegister)
                verificationCheckup(it)
            }, {
                if (it is IllegalArgumentException)
                    navigator.showError(navigator.getStringResource(R.string.invalid_detail_msg))
                else
                navigator.showError(it.message)
            })
    }

    fun fireEvent(loginModel: LoginResponse?, isRegister: Boolean) {

        AppsFlyerLib.getInstance().setCustomerUserId(loginModel?.UserId.toString())

        analyticsHelper.setUserID("" + loginModel?.UserId)
        analyticsHelper.setUserDataToTools(loginModel)

        analyticsHelper.setJsonUserProperty(emptyJson().apply {
            put(AnalyticsKey.Properties.LoginType, "Mobile")
            put(AnalyticsKey.Properties.Mobile, loginModel?.Mobile)
            put(AnalyticsKey.Properties.Email, loginModel?.Email)
            put(AnalyticsKey.Properties.UserID, loginModel?.UserId)
            put(AnalyticsKey.Properties.FullName, loginModel?.Name)
            put(AnalyticsKey.Properties.DeviceID, prefs.androidId.toString())
            //put(AnalyticsKey.Properties.Token, prefs.firebaseToken.toString())
            loginModel?.Name?.split(" ")?.let {list->
                if (list.isEmpty()) return@let
                list.elementAtOrNull(0)?.let {fName->
                    put(AnalyticsKey.Properties.FirstName, fName)
                }
                list.elementAtOrNull(1)?.let {lName->
                    put(AnalyticsKey.Properties.LastName, lName)
                }
            }
        })

        if (isRegister) {
            prefs.isUserProperySet=false
            analyticsHelper.fireEvent(
                AnalyticsKey.Names.UserRegister, bundleOf(
                    AnalyticsKey.Keys.LoginType to AnalyticsKey.Values.TypeMobile,
                    AnalyticsKey.Keys.ReferCode to userReferCode.get(),
                    AnalyticsKey.Keys.MobileNo to mobileNumber.get(),
                    AnalyticsKey.Keys.UserID to loginModel?.UserId,
                    AnalyticsKey.Keys.IPAddress to prefs.androidId.toString(),
                    AnalyticsKey.Keys.Screen to AnalyticsKey.Screens.LOGIN
                )
            )

           /* analyticsHelper.fireAttributesEvent(
                AnalyticsEventsKeys.ACCOUNT_CREATE, loginModel?.UserId.toString()
            )*/
            if (BuildConfig.isPlayStoreApk==1){
                //this event need to send on firebase for playStore APK
                analyticsHelper.sendEventToFireBase(
                    AnalyticsEventsKeys.ACCOUNT_CREATE, bundleOf(Pair(AnalyticsConstants.USER_ID,loginModel?.UserId.toString()))
                )
            }
        } else {
            analyticsHelper.fireEvent(
                AnalyticsKey.Names.UserLogin, bundleOf(
                    AnalyticsKey.Keys.LoginType to AnalyticsKey.Values.TypeMobile,
                    AnalyticsKey.Keys.ReferCode to userReferCode.get(),
                    AnalyticsKey.Keys.MobileNo to mobileNumber.get(),
                    AnalyticsKey.Keys.UserID to loginModel?.UserId,
                    AnalyticsKey.Keys.UserName to loginModel?.Name,
                    AnalyticsKey.Keys.Email to loginModel?.Email,
                    AnalyticsKey.Keys.IPAddress to prefs.androidId.toString(),
                    AnalyticsKey.Keys.Screen to AnalyticsKey.Screens.LOGIN
                )
            )

            analyticsHelper.fireAttributesEvent(
                AnalyticsEventsKeys.LOGIN_DONE, loginModel?.UserId.toString()
            )
        }

    }

    private fun verificationCheckup(response: BaseModel<Any>) {
        navigator.showMessage(response.Message)
        loginModel = gson.fromJson(gson.toJson(response.Response), LoginResponse::class.java)
        analyticsHelper.updateEndPointValue(loginModel)
        if (loginModel?.IsFairPlay == true)
            navigatorAct.showFairplayVoilationDialog(loginModel?.FairPlayMessage ?: "")
        else loginResponseCheckup()
    }

    fun loginResponseCheckup() {
        loginModel?.apply {
            navigatorAct.dismissFairplayVoilationDialog()
            prefs.loginCompleted = true
            prefs.avtarId=-1
            gameState=prefs.userStateName
            prefs.introductionCompleted = true
            prefs.loginResponse = gson.toJson(loginModel)
            navigatorAct.loginSuccess()
            logoutStatus(apiInterface, loginModel?.UserId ?: 0, prefs.androidId ?: "", "1")
        }
    }

    fun resendOTP() {
        finishTimer()
        if (loginStep.get() == TWO_WAY_PROCESS) loginByEmail(mEmail, mPassword)
        else loginOrRegisterByMobile(isResendOTP = true)
    }

    fun editMobileNumber() {
        loginStep.set(MOBILE_NUMBER)
    }

    fun startTimer() {
        otpTimer?.cancel()
        var hms = ""
        otpTimer = object : CountDownTimer(60 * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                hms = String.format(
                    "%02d",
                    TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(
                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)
                    )
                )
                remainTimeText.set(hms)
            }

            override fun onFinish() {
                timeIsOver.set(true)
            }
        }.start()
    }

    fun finishTimer() {
        otpTimer?.onFinish()
        timeIsOver.set(false)
        wrongOtp.set(false)
    }
}