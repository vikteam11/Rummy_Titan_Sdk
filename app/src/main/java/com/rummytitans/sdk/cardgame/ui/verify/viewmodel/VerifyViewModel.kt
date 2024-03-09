package com.rummytitans.sdk.cardgame.ui.verify.viewmodel

import android.os.CountDownTimer
import com.rummytitans.sdk.cardgame.R
import com.rummytitans.sdk.cardgame.analytics.AnalyticsHelper
import com.rummytitans.sdk.cardgame.analytics.AnalyticsKey
import com.rummytitans.sdk.cardgame.api.APIInterface
import com.rummytitans.sdk.cardgame.data.SharedPreferenceStorageRummy
import com.rummytitans.sdk.cardgame.models.LoginResponseRummy
import com.rummytitans.sdk.cardgame.models.ProfileVerificationModel
import com.rummytitans.sdk.cardgame.ui.BaseViewModel
import com.rummytitans.sdk.cardgame.ui.profile.verify.ProfileVerificationItem
import com.rummytitans.sdk.cardgame.ui.verify.VerificationNavigator
import com.rummytitans.sdk.cardgame.utils.ConnectionDetector
import com.rummytitans.sdk.cardgame.utils.MyConstants
import com.rummytitans.sdk.cardgame.utils.locationservices.utils.emptyJson
import com.rummytitans.sdk.cardgame.widget.MyDialog
import android.text.TextUtils
import android.util.Log
import androidx.core.os.bundleOf
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.rummytitans.sdk.cardgame.utils.bottomsheets.models.BottomSheetStatusDataModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
@HiltViewModel
class VerifyViewModel @Inject constructor(
    val prefs: SharedPreferenceStorageRummy,
    val gson: Gson,
    val apiInterface: APIInterface,
    val connectionDetector: ConnectionDetector,
    val analyticsHelper: AnalyticsHelper
) : BaseViewModel<VerificationNavigator>() {
    val loginStep = MyConstants.OTP
    val data = MutableLiveData<ProfileVerificationModel>()
    val loginResponse: LoginResponseRummy = gson.fromJson(prefs.loginResponse, LoginResponseRummy::class.java)
    var isLoading = ObservableBoolean(false)
    var isValidEmailVerify = ObservableBoolean(false)
    var isShowPinViewError = ObservableBoolean(false)
    var myDialog: MyDialog? = null
    var isSwipeLoading = ObservableBoolean(false)
    var wrongOtp = ObservableBoolean(false)
    var wrongOtpErrorMSg = ObservableField("")

    var email = ObservableField("")
    private var otpTimer: CountDownTimer? = null
    var remainTimeText = ObservableField<String>()
    val verificationInfo: LiveData<ProfileVerificationModel>
        get() = data
    var timer : Timer?= Timer()
    private var intervalEmailStatus = 5000
    var timeIsOver = ObservableBoolean(false)
    val regularColor = prefs.regularColor
    val safeColor = prefs.safeColor
    val selectedColor = ObservableField(if (prefs.onSafePlay) safeColor else regularColor)
    var isVrified = ObservableBoolean(false)
    private var countTimer = 0;
    init {
        kotlin.runCatching {
            wrongOtpErrorMSg.set(navigator.getStringResource(R.string.you_have_entered_wrong_verification_code))
        }
    }


    fun fetchVerificationData() {
        if (!connectionDetector.isConnected) {
            myDialog?.noInternetDialog {
                isLoading.set(true)
                fetchVerificationData()
            }
            isLoading.set(false)
            isSwipeLoading.set(false)
            return
        }

        val api = getApiEndPointObject(prefs.appUrl2?:"")

        compositeDisposable.add(
            api.getVerificationInfo(
                loginResponse.UserId.toString(),
                loginResponse.ExpireToken,
                loginResponse.AuthExpire
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(({
                    isSwipeLoading.set(false)
                    isLoading.set(false)
                    if (it.TokenExpire) {
                        logoutStatus(apiInterface, loginResponse.UserId, prefs.androidId ?: "", "0")
                        prefs.loginResponse = gson.toJson(LoginResponseRummy())
                        prefs.loginCompleted = false
                        navigator.logoutUser()
                    }

                    if (it.Status) {
                        data.value = it.Response
                        isVrified.set(it.Response.profileVerified())
                        it.Response.apply {
                            val status = when {
                                BankItem.Verify -> "Bank"
                                PancardItem.Verify -> "PAN"
                                EmailItem.Verify -> "Email"
                                MobileItem.Verify -> "Phone"
                                else -> ""
                            }
                            analyticsHelper.setJsonUserProperty(
                                emptyJson().apply {
                                    put(AnalyticsKey.Properties.VerificationStatus, status)
                                    if (status == "Email" || status == "PAN" || status == "Bank")
                                        put(AnalyticsKey.Properties.Email, loginResponse.Email)
                                }
                            )
                        }
                        navigator.showMessage(it.Message)
                    }else
                        navigator.showError(it.Message)

                }), ({
                    navigator.handleError(it)
                    isSwipeLoading.set(false)
                    isLoading.set(false)
                }))
        )
    }

    fun verifyEmail(emailValue : String) {
        analyticsHelper.fireEvent(
            AnalyticsKey.Names.ButtonClick, bundleOf(
                AnalyticsKey.Keys.ButtonName to AnalyticsKey.Values.VerifyEmail,
                AnalyticsKey.Keys.Email to emailValue,
                AnalyticsKey.Keys.Screen to AnalyticsKey.Screens.Verification,
            )
        )
        if (!connectionDetector.isConnected) {
            myDialog?.noInternetDialog {
                verifyEmail(emailValue)
            }
            isLoading.set(false)
            isSwipeLoading.set(false)
            return
        }
        isLoading.set(true)
        val json = JsonObject()
        json.addProperty("email",emailValue)
        val apiInterface = getApiEndPointObject(prefs.appUrl2?:"")
        compositeDisposable.add(
            apiInterface.sendVerificationEmail(
                loginResponse.UserId.toString(),
                loginResponse.ExpireToken,
                loginResponse.AuthExpire,
                json
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(({
                    isLoading.set(false)
                    if (it.Status) {
                        navigatorAct.fireBranchEvent(loginResponse.UserId)
                        //navigator.showMessage(it.Message)
                        Log.e("loginstep == ", loginStep.toString())
                        email.set(emailValue ?: "")
                        navigatorAct.showVerifyEmailDialog()
                        runTimerVerificationStatus()
                    } else {
                        navigator.showError(it.Message)
                    }
                }), ({
                    isLoading.set(false)
                    navigator.handleError(it)
                }))
        )
    }

    fun runTimerVerificationStatus(){
        countTimer = 0
        timer = Timer()
        timer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                Log.e("runcommnad>>>>>>>>","check email verification")
                checkEmailVerificationStatus()

            }
        }, 100, intervalEmailStatus.toLong())



    }

    fun onEmailVerify(emailValue : String,otpValue : String) {
        analyticsHelper.fireEvent(
            AnalyticsKey.Names.ButtonClick, bundleOf(
                AnalyticsKey.Keys.ButtonName to AnalyticsKey.Values.VerifyOtp,
                AnalyticsKey.Keys.Screen to AnalyticsKey.Screens.Verification,
            )
        )
        isShowPinViewError.set(false)
        if (!connectionDetector.isConnected) {
            myDialog?.noInternetDialog {
                onEmailVerify(emailValue,otpValue)
            }
            isLoading.set(false)
            isSwipeLoading.set(false)
            return
        }
        isLoading.set(true)
        val json = JsonObject()
        json.addProperty("email",emailValue)
        json.addProperty("otp",otpValue)
        val apiInterface = getApiEndPointObject(prefs.appUrl2?:"")
        compositeDisposable.add(
            apiInterface.verifyEmail(
                loginResponse.UserId.toString(),
                loginResponse.ExpireToken,
                loginResponse.AuthExpire,
                json
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(({
                    isLoading.set(false)
                    if (it.Status) {
                        analyticsHelper.fireEvent(
                            AnalyticsKey.Names.EmailVerified, bundleOf(
                                AnalyticsKey.Keys.Type to AnalyticsKey.Values.VerifyOtp,
                                AnalyticsKey.Keys.Screen to AnalyticsKey.Screens.Verification,
                            )
                        )
                        stopVerificationTimer()
                        navigatorAct.dismissVerifyEmailOtpDialog()
                        data.value?.EmailItem?.Verify = true
                        data.value?.EmailItem?.Value = email.get()
                        data.value = data.value
                        navigatorAct.showUploadingSheet(
                            BottomSheetStatusDataModel().apply {
                                title = "Email Verification Successful!"
                                description = it.Message
                                positiveButtonName= "Ok,Got it"
                                btnColorRes = R.color.turtle_Green
                                isSuccess = false
                                allowCross = true
                                animationFileId = R.raw.withdrawal_done_anim
                                showSuccessAnim  = R.raw.success_blast_anim
                            }
                        )


                    } else {
                        isShowPinViewError.set(true)
                        wrongOtpErrorMSg.set(navigator.getStringResource(R.string.otp_you_entered_invalid))
                    }
                }), ({
                    isLoading.set(false)
                    navigator.handleError(it)
                }))
        )
    }

    fun checkEmailVerificationStatus() {
        if(data.value?.isEmailVerify==true){
            stopVerificationTimer()
            return
        }
        if (!connectionDetector.isConnected) {
            myDialog?.noInternetDialog {
                checkEmailVerificationStatus()
            }
            return
        }
        val apiInterface = getApiEndPointObject(prefs.appUrl2?:"")
        compositeDisposable.add(
            apiInterface.verificationStatus(
                loginResponse.UserId.toString(),
                loginResponse.ExpireToken,
                loginResponse.AuthExpire,
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(({
                    if (it.Status) {
                        countTimer++
                        intervalEmailStatus = it.Response.timer * 1000

                        if(countTimer == it.Response.count)
                            stopVerificationTimer()

                        if(it.Response.isIsVerified){
                            analyticsHelper.fireEvent(
                                AnalyticsKey.Names.EmailVerified, bundleOf(
                                    AnalyticsKey.Keys.Type to AnalyticsKey.Values.ViaLink,
                                    AnalyticsKey.Keys.Screen to AnalyticsKey.Screens.Verification,
                                )
                            )
                            stopVerificationTimer()
                            data.value?.EmailItem?.Verify = true
                            data.value?.EmailItem?.Value = email.get()
                            data.value = data.value
                            navigatorAct.dismissVerifyEmailOtpDialog()
                            navigatorAct.showUploadingSheet(
                                BottomSheetStatusDataModel().apply {
                                    title = "Email Verification Successful!"
                                    description = it.Message
                                    positiveButtonName= "Ok,Got it"
                                    btnColorRes = R.color.rummy_maingreen
                                    isSuccess = false
                                    allowCross = true
                                    animationFileId = R.raw.withdrawal_done_anim
                                    showSuccessAnim  = R.raw.success_blast_anim
                                }
                            )


                        }

                    }
                }), ({
                    navigator.handleError(it)
                }))
        )
    }

    fun stopVerificationTimer() {
        timer?.cancel()
        timer = null
    }

    fun updateEmail(emailValue: String) {
        if (!connectionDetector.isConnected) {
            myDialog?.noInternetDialog {
                updateEmail(emailValue)
            }
            isLoading.set(false)
            isSwipeLoading.set(false)
            return
        }
        isLoading.set(true)
        compositeDisposable.add(
            apiInterface.updateEmailAddress(
                loginResponse.UserId.toString(),
                loginResponse.ExpireToken,
                loginResponse.AuthExpire,
                emailValue
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(({
                    isLoading.set(false)
                    if (it.Status) {
                        navigatorAct.emailChangeDone()
                        fetchVerificationData()
                        //navigator.showMessage(it.Message)

                    } else {
                        navigator.showError(it.Message)
                    }
                }), ({
                    navigator.handleError(it)
                }))
        )
    }

    fun deleteBank() {

        if (!connectionDetector.isConnected) {
            myDialog?.noInternetDialog {
                deleteBank()
            }
            isLoading.set(false)
            isSwipeLoading.set(false)
            return
        }
        isLoading.set(true)
        compositeDisposable.add(
            apiInterface.deleteBank(
                loginResponse.UserId.toString(),
                loginResponse.ExpireToken,
                loginResponse.AuthExpire
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(({
                    isLoading.set(false)
                    if (it.Status) {
                        fetchVerificationData()
                    }
                    navigator.showMessage(it.Message)
                }), ({
                    navigator.handleError(it)
                    isLoading.set(false)
                }))
        )
    }

    fun getVerificationItems(addEmail:Boolean = false):ArrayList<ProfileVerificationItem>{
        val list = verificationInfo.value?.let {
            arrayListOf(
                ProfileVerificationItem(
                    MyConstants.VERIFY_ITEM_MOBILE,
                    navigator.getStringResource(R.string.frag_verify_mobile),
                    it.MobileItem.Value?:"",
                    isVerified = it.MobileItem.Verify,
                    selectedColor = selectedColor.get(),
                    icon = R.drawable.ic_mobile_verification,
                    verifyColor = R.color.light_yellow,
                    textColor = R.color.yellow,
                    buttonId = R.id.verifyMobile,
                    isBlocked = it.MobileItem.isBlocked,
                    message = it.MobileItem.Message?:""
                ),
                ProfileVerificationItem(
                    MyConstants.VERIFY_ITEM_PAN,
                    navigator.getStringResource(if(addEmail)R.string.pan else R.string.frag_verify_pan_card),
                    it.PancardItem.Value,
                    isVerified=it.PancardItem.Verify,
                    selectedColor = selectedColor.get(),
                    icon = R.drawable.ic_pan_verification,
                    verifyColor = R.color.light_blue,
                    textColor = R.color.verification_blue,
                    buttonId = R.id.verifyPan,
                    isBlocked = it.PancardItem.isBlocked,
                    message = it.PancardItem.Message?:""
                ),
                ProfileVerificationItem(
                    MyConstants.VERIFY_ITEM_BANK,
                    navigator.getStringResource(if(addEmail)R.string.bank else R.string.bank_account),
                    it.BankItem.Value,isVerified=it.BankItem.Verify,
                    selectedColor = selectedColor.get(),
                    isDeleteAble = true,
                    icon = R.drawable.ic_bank_verification,
                    verifyColor = R.color.light_purple,
                    textColor = R.color.verification_purple,
                    buttonId = R.id.verifyBank,
                    isBlocked = it.BankItem.isBlocked,
                    message = it.BankItem.Message?:""
                ),
                ProfileVerificationItem(
                    MyConstants.VERIFY_ITEM_ADDRESS,
                    when{
                        addEmail -> navigator.getStringResource(R.string.address)
                        it.AddressType==1 ->{navigator.getStringResource(R.string.aadhaar_card)}
                        it.AddressType==2 ->{navigator.getStringResource(R.string.driving_license)}
                        else -> navigator.getStringResource(R.string.address)
                    },
                    it.AddressItem.Value,
                    isVerified = it.AddressItem.Verify,
                    selectedColor = selectedColor.get(),
                    icon = R.drawable.ic_address_verificqation,
                    verifyColor = R.color.light_theme1,
                    textColor = R.color.theme1_regular,
                    buttonId = R.id.verifyAddress,
                    isBlocked = it.AddressItem.isBlocked,
                    message = it.AddressItem.Message?:""
                ),
            )
        }?: kotlin.run {
            arrayListOf()
        }
        if(addEmail){
            list.add(1,
                ProfileVerificationItem(
                    MyConstants.VERIFY_ITEM_EMAIL,
                    navigator.getStringResource(R.string.email),verificationInfo.value?.EmailItem?.Value,
                    isVerified = verificationInfo.value?.EmailItem?.Verify?:false,
                    selectedColor = selectedColor.get(),
                    icon = R.drawable.ic_email_verification,
                    verifyColor = R.color.light_red,
                    textColor = R.color.red,
                    buttonId = R.id.verifyEmail,
                    isBlocked = verificationInfo.value?.EmailItem?.isBlocked?:false,
                    message = verificationInfo.value?.EmailItem?.Message?:""
                )
            )
        }
        list.sortBy { !it.isVerified}
        list.sortBy { TextUtils.isEmpty(it.value) }
        return list
    }

    fun editMobileNumber() {
    }

    fun resendOTP() {

    }

    fun startTimer() {
        otpTimer?.cancel()
        var hms = ""
        otpTimer = object : CountDownTimer(59 * 1000, 1000) {
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
