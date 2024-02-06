package com.rummytitans.sdk.cardgame.ui.newlogin

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.appsflyer.AppsFlyerLib
import com.google.android.gms.auth.api.credentials.Credential
import com.google.android.gms.auth.api.credentials.Credentials
import com.google.android.gms.auth.api.credentials.HintRequest
import com.rummytitans.sdk.cardgame.AdvertisingIdClient
import com.rummytitans.sdk.cardgame.R
import com.rummytitans.sdk.cardgame.analytics.AnalyticsKey
import com.rummytitans.sdk.cardgame.databinding.ActivityNewLoginRummyBinding
import com.rummytitans.sdk.cardgame.ui.RummyMainActivity
import com.rummytitans.sdk.cardgame.ui.base.BaseActivity
import com.rummytitans.sdk.cardgame.utils.*
import com.rummytitans.sdk.cardgame.widget.MyDialog
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener
import java.util.*
import javax.inject.Inject


class RummyNewLoginActivity : BaseActivity(), NewLoginNavigator {


    lateinit var binding: ActivityNewLoginRummyBinding
    lateinit var mViewModel: NewLoginViewModel

    private val REQUEST_CODE_LOGIN = 111

    private var referCodeDialog: Dialog? = null
    private var fairPlayVoilationDialog: Dialog? = null


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        transparentStatusBar()
        super.onCreate(savedInstanceState)
        mViewModel = ViewModelProvider(this).get(NewLoginViewModel::class.java)
        mViewModel.navigator = this
        mViewModel.navigatorAct = this
        mViewModel.myParentDialog = MyDialog(this)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_new_login_rummy)
        binding.viewModel = mViewModel
        mViewModel.prefs.loginResponse = ""
        mViewModel.emailTextArray = ArrayList<String>().apply {
            add(getString(R.string.enter_your_email_address_and_password_to_continue_your_fantasy_journey_with_myteam11))
            add(getString(R.string.app_name_rummy))
        }

        mViewModel.phoneTextArray = ArrayList<String>().apply {
            add(getString(R.string.enter_your_phone_number_to_continue_your_fantasy_journey_with_myteam11))
            add(getString(R.string.app_name_rummy))
        }
        mViewModel.colorArray = ArrayList<Int>().apply {
            add(R.color.cool_grey)
            add(R.color.gunmetal)
        }
        mViewModel.clikableTextArray.add(getString(R.string.term_condition))
        mViewModel.clikableTextArray.add(getString(R.string.privacy_policy))
        //mViewModel.clikableTextArray.add(getString(R.string.responsible_gaming_text))
        mViewModel.plainText.set(getString(R.string.privacypolicy_term_login_title))

        mViewModel.prefs.apply {
            if (!TextUtils.isEmpty(referCode)) {
                mViewModel.referCode.set(true)
                mViewModel.userReferCode.set(referCode)
            }
        }

        requestForUserPhoneNumber()
        startSMSListener()
        setTextWatchers()
        fetchAdvertisingId()

        binding.scrollView.postDelayed( { binding.scrollView.fullScroll(View.FOCUS_DOWN) },700)
        KeyboardVisibilityEvent.setEventListener(this, object : KeyboardVisibilityEventListener {
            override fun onVisibilityChanged(isOpen: Boolean) {
                if (isOpen) binding.scrollView.post { binding.scrollView.fullScroll(View.FOCUS_DOWN) }
            }
        })

        binding.scrollView.setOnTouchListener { v, event ->
            when (event?.action) {
                MotionEvent.ACTION_DOWN ->{
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    if (imm.isAcceptingText) {
                        Log.e("keyboard>>","Software Keyboard was shown")
                    } else {
                        Log.e("keyboard>>","Software Keyboard was not shown")
                    }
                }
            }

            v?.onTouchEvent(event) ?: true
        }
            mViewModel.analyticsHelper.fireEvent(
            AnalyticsKey.Names.ScreenLoadDone, bundleOf(
                AnalyticsKey.Keys.Screen to AnalyticsKey.Screens.LOGIN
            )
        )
    }

    private fun fetchAdvertisingId() {

        mViewModel.appsFlyerId = AppsFlyerLib.getInstance().getAppsFlyerUID(this)

        val d = Observable.fromCallable {
            AdvertisingIdClient.getAdvertisingIdInfo(this)?.id
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .doOnError {
                it.printStackTrace()
            }
            .subscribe( {
                if (it==null) return@subscribe
                mViewModel.prefs.advertisingId=it
                println("Advertising id is ---> $it")
            },{ println("Failed Advertising id is ---> ${it.message}") })

    }

    private fun setTextWatchers() {
        binding.apply {
            includeMobileNumber.editMobileNumber.addTextChangedListener {
                if (it.toString().isNotEmpty()) includeMobileNumber.inputPhone.error = null
                mViewModel.validForMobile.set(validMobile(it.toString()))
            }
            includeOTPVerify.otpView.addTextChangedListener {
                if (it.toString().isNotEmpty()) mViewModel.wrongOtp.set(false)
                mViewModel.validForPinview.set(it.toString().length>5)
                val otp = it.toString()
                if (otp.length == 6) mViewModel.verifyOTP(otp)
            }
        }
    }

    override fun showReferCodeDialog() {
        mViewModel.analyticsHelper.fireEvent(
            AnalyticsKey.Names.ButtonClick, bundleOf(
                AnalyticsKey.Keys.ButtonName to AnalyticsKey.Values.ReferCode,
                AnalyticsKey.Keys.Screen to AnalyticsKey.Screens.LOGIN
            )
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MyConstants.PHONE_PICKER_REQUEST) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                val credential = data.getParcelableExtra<Credential>(Credential.EXTRA_KEY)
                var mPhone = credential?.id ?: ""
                if (mPhone.length >= 10) {
                    mPhone = mPhone.substring(mPhone.length - 10)
                    onNumberReceived(mPhone, MyConstants.MOBILE_STATUS_SELECT)
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                onNumberReceived("", MyConstants.MOBILE_STATUS_CANCELED)
            } else {
                onNumberReceived("", MyConstants.MOBILE_STATUS_NONE)
            }
        } else if (requestCode == REQUEST_CODE_LOGIN) {
            if (resultCode == Activity.RESULT_OK) {
                setResult(Activity.RESULT_OK)
                finish()
            }
        }
    }

     fun onNumberReceived(number: String, status: String) {
        when (status) {
            MyConstants.MOBILE_STATUS_SELECT -> {
                binding.includeMobileNumber.editMobileNumber.isFocusableInTouchMode = true
                binding.includeMobileNumber.editMobileNumber.requestFocus()
                binding.includeMobileNumber.editMobileNumber.setText(number)
                binding.includeMobileNumber.editMobileNumber.setSelection(number.length)
            }
            MyConstants.MOBILE_STATUS_CANCELED -> {
                binding.includeMobileNumber.editMobileNumber.isFocusableInTouchMode = true
                binding.includeMobileNumber.editMobileNumber.requestFocus()
            }
            MyConstants.MOBILE_STATUS_NONE -> {
                binding.includeMobileNumber.editMobileNumber.isFocusableInTouchMode = true
                binding.includeMobileNumber.editMobileNumber.requestFocus()
            }
            else -> {
                binding.includeMobileNumber.editMobileNumber.isFocusableInTouchMode = true
                binding.includeMobileNumber.editMobileNumber.requestFocus()
            }
        }
    }

    private fun verifyUserConfirm():Boolean{
        return if (!mViewModel.confirmByUser.get()) {
            binding.checkBoxConfirm.startAnimation(AnimationUtils.loadAnimation(this,R.anim.shake))
            binding.txtTerms.startAnimation(AnimationUtils.loadAnimation(this,R.anim.shake))
            false
        }else true
    }


    fun submit(v: View) {
        if (mViewModel.loginStep.get() == mViewModel.MOBILE_NUMBER) {
            val number = binding.includeMobileNumber.editMobileNumber.text.toString()
            if (validMobile(number)) {
                mViewModel.mobileNumber.set(number)
                mViewModel.loginOrRegisterByMobile()
            } else {
                mobileError(R.string.err_invalid_mobile_number)
            }
        } else if (mViewModel.loginStep.get() == mViewModel.OTP) {
            val otp = binding.includeOTPVerify.otpView.text.toString()
            if (otp.length == 6) mViewModel.verifyOTP(otp)
            else {
                mViewModel.apply {
                    wrongOtpErrorMSg.set(
                        if (otp.isEmpty())
                            getString(R.string.please_enter_verification_code)
                        else
                            getString(R.string.you_have_entered_wrong_verification_code)
                    )
                    wrongOtp.set(true)
                }
            }
        }
    }


    override fun onBackPressed() {
        when (mViewModel.OTP) {
            mViewModel.loginStep.get() -> {
                mViewModel.loginStep.set(mViewModel.MOBILE_NUMBER)
                mViewModel.finishTimer()
            }
            else -> super.onBackPressed()
        }
    }

    private fun startSMSListener() {

    }

    private fun requestForUserPhoneNumber() {
        binding.includeMobileNumber.editMobileNumber.isFocusableInTouchMode = false
        binding.includeMobileNumber.editMobileNumber.setOnClickListener {
            if (!binding.includeMobileNumber.editMobileNumber.isFocusableInTouchMode) {
               kotlin.runCatching {
                   val hintRequest = HintRequest.Builder()
                       .setPhoneNumberIdentifierSupported(true).build()
                   val credentialsClient = Credentials.getClient(this)
                   val intent = credentialsClient.getHintPickerIntent(hintRequest)
                   startIntentSenderForResult(
                       intent.intentSender, MyConstants.PHONE_PICKER_REQUEST,
                       null, 0, 0, 0
                   )
               }
            }
        }
    }
    override fun loginSuccess() {
        if (intent.hasExtra(MyConstants.INTENT_PASS_COMING_FROM)) {
            setResult(Activity.RESULT_OK)
            finish()
        } else {
            finishAffinity()
            mViewModel.prefs.referUrl = ""
            val intent = Intent(this, RummyMainActivity::class.java)
            if (!TextUtils.isEmpty(mViewModel.prefs.appsFlyerDeepLink)) {
                intent.putExtra("comingForGame", true)
                mViewModel.prefs.appsFlyerDeepLink = ""
            }
            startActivity(intent)
        }
    }

    override fun mobileError(error: Int) {
        binding.includeMobileNumber.inputPhone.error = getString(error)
    }

    override fun resetErrors() {
        hideKeyboard()
        binding.includeMobileNumber.apply { inputPhone.error = null }
    }

    override fun dismissFairplayVoilationDialog() {
        fairPlayVoilationDialog?.dismiss()
    }

    override fun showFairplayVoilationDialog(fairPlayMessage: String) {
      /*  fairPlayVoilationDialog = MyDialog(this).getMyDialog(R.layout.dialog_change_language)
        fairPlayVoilationDialog?.show()
        fairPlayVoilationDialog?.txtMessage?.text = fairPlayMessage
        fairPlayVoilationDialog?.textView29?.text = "ATTENTION"
        fairPlayVoilationDialog?.btnContinue?.setOnClickListener {
            mViewModel.loginResponseCheckup()
        }*/
    }

    override fun onRequestFocusOtp() {
        binding.includeOTPVerify.otpView.requestFocus(View.FOCUS_UP)
        binding.includeOTPVerify.otpView.setText("")
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        kotlin.runCatching {
            binding.root.postDelayed({
                val otpViewWidth = binding.includeOTPVerify.otpView.width
                binding.includeOTPVerify.otpView.itemWidth = otpViewWidth/7
            }, 50)
        }
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.conatainer.windowToken, 0)
    }
}

interface NewLoginNavigator {
    fun loginSuccess()
    fun showReferCodeDialog()
    fun resetErrors()
    fun forgotPassword() {}
    fun mobileError(error: Int)
    fun dismissFairplayVoilationDialog()
    fun showFairplayVoilationDialog(fairPlayMessage: String)
    fun onRequestFocusOtp()
}
