package com.rummytitans.playcashrummyonline.cardgame.ui.profile.verify.viewmodel

import com.rummytitans.playcashrummyonline.cardgame.R
import com.rummytitans.playcashrummyonline.cardgame.analytics.AnalyticsHelper
import com.rummytitans.playcashrummyonline.cardgame.analytics.AnalyticsKey
import com.rummytitans.playcashrummyonline.cardgame.api.APIInterface
import com.rummytitans.playcashrummyonline.cardgame.data.SharedPreferenceStorage
import com.rummytitans.playcashrummyonline.cardgame.models.LoginResponse
import com.rummytitans.playcashrummyonline.cardgame.models.ProfileVerificationModel
import com.rummytitans.playcashrummyonline.cardgame.ui.BaseViewModel
import com.rummytitans.playcashrummyonline.cardgame.ui.profile.verify.ProfileVerificationItem
import com.rummytitans.playcashrummyonline.cardgame.ui.profile.verify.VerificationNavigator
import com.rummytitans.playcashrummyonline.cardgame.utils.ConnectionDetector
import com.rummytitans.playcashrummyonline.cardgame.utils.MyConstants
import com.rummytitans.playcashrummyonline.cardgame.utils.WebViewUrls
import com.rummytitans.playcashrummyonline.cardgame.utils.locationservices.utils.emptyJson
import com.rummytitans.playcashrummyonline.cardgame.widget.MyDialog
import android.text.TextUtils
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
@HiltViewModel
class VerifyViewModel @Inject constructor(
    val prefs: SharedPreferenceStorage,
    val gson: Gson,
    val apiInterface: APIInterface,
    val connectionDetector: ConnectionDetector,
    val analyticsHelper: AnalyticsHelper
) : BaseViewModel<VerificationNavigator>() {
    val data = MutableLiveData<ProfileVerificationModel>()
    val loginResponse: LoginResponse = gson.fromJson(prefs.loginResponse, LoginResponse::class.java)
    var isLoading = ObservableBoolean(false)
    var myDialog: MyDialog? = null
    var isSwipeLoading = ObservableBoolean(false)


    val verificationInfo: LiveData<ProfileVerificationModel>
        get() = data

    val regularColor = prefs.regularColor
    val safeColor = prefs.safeColor
    val selectedColor = ObservableField(if (prefs.onSafePlay) safeColor else regularColor)
    var isVrified = ObservableBoolean(false)

    fun getHowtoVerifyWebUrls(): String {
        return WebViewUrls.AppDefaultURL + WebViewUrls.SHORT_HowtoVerify
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


        compositeDisposable.add(
            apiInterface.getVerificationInfo(
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
                        prefs.loginResponse = gson.toJson(LoginResponse())
                        prefs.loginCompleted = false
                        navigator.logoutUser()
                    }

                    if (it.Status) {
                        data.value = it.Response
                        isVrified.set(it.Response.EmailVerify and it.Response.MobileVerify and it.Response.PanVerify and it.Response.BankVerify)
                        it.Response.apply {
                            val status = when {
                                BankVerify -> "AccountNumber"
                                PanVerify -> "PAN"
                                EmailVerify -> "Email"
                                MobileVerify -> "Phone"
                                else -> ""
                            }
                            analyticsHelper.setJsonUserProperty(
                                emptyJson().apply {
                                    put(AnalyticsKey.Properties.VerificationStatus, status)
                                    if (status == "Email" || status == "PAN" || status == "AccountNumber")
                                        put(AnalyticsKey.Properties.Email, loginResponse.Email)
                                }
                            )
                        }
                    }
                    navigator.showMessage(it.Message)
                }), ({
                    navigator.handleError(it)
                    isSwipeLoading.set(false)
                    isLoading.set(false)
                }))
        )
    }

    fun verifyEmail() {
        if (TextUtils.isEmpty(verificationInfo.value?.Email)) {
            navigator.showError(R.string.err_invalid_email)
            return
        }
        if (!connectionDetector.isConnected) {
            myDialog?.noInternetDialog {
                verifyEmail()
            }
            isLoading.set(false)
            isSwipeLoading.set(false)
            return
        }
        isLoading.set(true)
        compositeDisposable.add(
            apiInterface.sendVerificationEmail(
                loginResponse.UserId.toString(),
                loginResponse.ExpireToken,
                loginResponse.AuthExpire
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(({
                    isLoading.set(false)
                    if (it.Status) {
                        navigatorAct.fireBranchEvent(loginResponse.UserId)
                        navigator.showMessage(it.Message)
                    } else {
                        navigator.showError(it.Message)
                    }
                }), ({
                    navigator.handleError(it)
                }))
        )
    }

    fun updateEmail(email: String) {
        if (!connectionDetector.isConnected) {
            myDialog?.noInternetDialog {
                updateEmail(email)
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
                email
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(({
                    isLoading.set(false)
                    if (it.Status) {
                        navigatorAct.emailChangeDone()
                        fetchVerificationData()
                        navigator.showMessage(it.Message)
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
        val list =  verificationInfo.value?.let {
            arrayListOf(
                ProfileVerificationItem(
                    MyConstants.VERIFY_ITEM_MOBILE,
                    navigator.getStringResource(R.string.frag_verify_mobile),
                    it.MobileNumber?:"",
                    isVerified = it.MobileVerify,
                    selectedColor = selectedColor.get(),
                    icon = R.drawable.ic_mobile_verification,
                    verifyColor = R.color.light_yellow,
                    textColor = R.color.yellow,
                    buttonId = R.id.verifyMobile
                ),

                ProfileVerificationItem(
                    MyConstants.VERIFY_ITEM_PAN,
                    navigator.getStringResource(if(addEmail)R.string.pan else R.string.frag_verify_pan_card),
                    it.PanCardNumber,
                    isVerified=it.PanVerify,
                    selectedColor = selectedColor.get(),
                    icon = R.drawable.ic_pan_verification,
                    verifyColor = R.color.light_blue,
                    textColor = R.color.verification_blue,
                    buttonId = R.id.verifyPan
                ),

                ProfileVerificationItem(
                    MyConstants.VERIFY_ITEM_BANK,
                    navigator.getStringResource(if(addEmail)R.string.bank else R.string.bank_account),
                    it.AccNo,isVerified=it.BankVerify,
                    selectedColor = selectedColor.get(),
                    isDeleteAble = true,
                    icon = R.drawable.ic_bank_verification,
                    verifyColor = R.color.light_purple,
                    textColor = R.color.verification_purple,
                    buttonId = R.id.verifyBank
                ),

                ProfileVerificationItem(
                    MyConstants.VERIFY_ITEM_ADDRESS,
                    when{
                        addEmail -> "Address"
                        it.AddressType == 1 -> "Aadhaar Card"
                        it.AddressType == 2 -> "Driving License"
                        else -> "Address"
                    },
                    it.AddressNo,
                    isVerified = it.AddressVerified,
                    selectedColor = selectedColor.get(),
                    icon = R.drawable.ic_address_verificqation,
                    verifyColor = R.color.light_theme1,
                    textColor = R.color.theme1_regular,
                    buttonId = R.id.verifyAddress
                ),
            )
        }?: kotlin.run {
            arrayListOf()
        }
        if(addEmail){
            list.add(1,
                ProfileVerificationItem(
                    MyConstants.VERIFY_ITEM_EMAIL,
                    navigator.getStringResource(R.string.email),verificationInfo.value?.Email,
                    isVerified = verificationInfo.value?.EmailVerify?:false,
                    selectedColor = selectedColor.get(),
                    icon = R.drawable.ic_email_verification,
                    verifyColor = R.color.light_red,
                    textColor = R.color.red,
                    buttonId = R.id.verifyEmail
                )

            )
        }

        list.sortBy { !it.isVerified}
        list.sortBy { TextUtils.isEmpty(it.value)  }
        return list
    }

}
