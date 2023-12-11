package com.rummytitans.sdk.cardgame.ui.profile.updatePhone

import com.rummytitans.sdk.cardgame.R
import com.rummytitans.sdk.cardgame.ui.base.BaseActivity
import com.rummytitans.sdk.cardgame.widget.MyDialog
import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.WindowManager
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.rummytitans.sdk.cardgame.databinding.ActivityUpdatePhoneRummyBinding
import javax.inject.Inject

class UpdatePhoneActivity : BaseActivity(),UpdatePhoneNavigator {


    private lateinit var mBinding: ActivityUpdatePhoneRummyBinding
    private lateinit var mViewModel: UpdatePhoneViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewModel = ViewModelProvider(this)[UpdatePhoneViewModel::class.java]
        mBinding = DataBindingUtil.setContentView<ActivityUpdatePhoneRummyBinding>(this, R.layout.activity_update_phone_rummy).apply{
            lifecycleOwner = this@UpdatePhoneActivity
            viewModel = mViewModel
        }
        mViewModel.myParentDialog= MyDialog(this)
        mViewModel.navigatorAct=this
        mViewModel.navigator=this
        setEditTextWatchers()
    }

    private fun setEditTextWatchers(){

        mBinding.editMobileNumber.addTextChangedListener {
            mBinding.inputPhone.error=null
            mViewModel.mobileNumber.set(it.toString())
           // mViewModel.onMobileSubmit()
        }
        mBinding.includeVerificationCode.otpView.addTextChangedListener {
            mViewModel.showError.set(false)
            mViewModel.otpText.set(it.toString())
            mViewModel.onOTPSubmit()
        }
    }

     override fun onRequestFocusOtp() {
         mBinding.includeVerificationCode.apply {
             otpView.requestFocus(View.FOCUS_UP)
             otpView.setText("")
             window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
         }
    }

    override fun showMobileError(msg: Int) {
        mBinding.inputPhone.error=getString(msg)
    }

    override fun onSuccessUpdate() {
        Handler().postDelayed({
            setResult(Activity.RESULT_OK)
            finish()
        },1000)
    }

    override fun onBackPressed() {
        if (mViewModel.updateStep.get()==UpdatePhoneViewModel.VERIFY_OTP){
            mViewModel.updateStep.set(UpdatePhoneViewModel.UPDATE_REQUEST_OTP)
            mViewModel.finishTimer()
        }else
        super.onBackPressed()
    }

}
interface UpdatePhoneNavigator{
    fun onRequestFocusOtp()
    fun showMobileError(msg:Int)
    fun onSuccessUpdate()
}