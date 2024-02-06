package com.rummytitans.sdk.cardgame.ui.verify

import android.app.Activity
import com.rummytitans.sdk.cardgame.R
import com.rummytitans.sdk.cardgame.databinding.FragmentVerifyRummyBinding
import com.rummytitans.sdk.cardgame.ui.base.BaseFragment
import com.rummytitans.sdk.cardgame.ui.verify.viewmodel.VerifyViewModel
import com.rummytitans.sdk.cardgame.ui.verifications.VerificationActivity
import com.rummytitans.sdk.cardgame.utils.*
import com.rummytitans.sdk.cardgame.widget.MyDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import com.rummytitans.sdk.cardgame.databinding.BottomsheetDialogEmailVerifyRummyBinding
import com.rummytitans.sdk.cardgame.ui.base.BaseNavigator
import com.rummytitans.sdk.cardgame.ui.common.CommonFragmentActivity
import com.rummytitans.sdk.cardgame.ui.newlogin.RummyNewLoginActivity
import com.rummytitans.sdk.cardgame.ui.profile.verify.ProfileVerificationItem
import com.rummytitans.sdk.cardgame.ui.verify.adapter.ProfileVerifyItemAdapter
import com.rummytitans.sdk.cardgame.utils.bottomsheets.BottomSheetDialogBinding
import com.rummytitans.sdk.cardgame.utils.bottomsheets.LottieBottomSheetDialog
import com.rummytitans.sdk.cardgame.utils.bottomsheets.listeners.BottomSheetStatusListener
import com.rummytitans.sdk.cardgame.utils.bottomsheets.models.BottomSheetStatusDataModel
import kotlinx.android.synthetic.main.dialog_edit_email_rummy.*
import kotlinx.android.synthetic.main.dialog_for_delete_bank_rummy.*
import kotlinx.android.synthetic.main.fragment_verify_rummy.*
import javax.inject.Inject

class FragmentVerify : BaseFragment(), BaseNavigator, VerificationNavigator,
    BottomSheetStatusListener {
    var dialogChangeEmail: Dialog? = null
    lateinit var viewModel: VerifyViewModel
    lateinit var binding: FragmentVerifyRummyBinding
    var uploadingDialog: LottieBottomSheetDialog?= null
    var setEmail = true
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        setTheme(inflater)
        setLanguage()
        viewModel = ViewModelProvider(this)
            .get(VerifyViewModel::class.java)
        binding = FragmentVerifyRummyBinding.inflate(localInflater ?: inflater, container, false).apply {
            lifecycleOwner = this@FragmentVerify
            viewmodel = this@FragmentVerify.viewModel
            viewModel.navigator = this@FragmentVerify
            viewModel.navigatorAct = this@FragmentVerify
            viewModel.myDialog = MyDialog(requireActivity())
        }
        dialogChangeEmail = viewModel.myDialog?.getMyDialog(R.layout.dialog_edit_email_rummy)
        dialogChangeEmail?.btnContinue?.setBackgroundColor(Color.parseColor(viewModel.selectedColor.get()))

        dialogChangeEmail?.apply {
            val btnContinue = findViewById<TextView>(R.id.btnContinue)
            val btnCancle = findViewById<TextView>(R.id.btnCancel)
            btnContinue?.setOnClickListener {
                val email = editEmail.text.toString().trim()
                if (!validEmail(email)) showError(R.string.err_invalid_email)
                else viewModel.updateEmail(email)
            }
            btnCancle?.setOnClickListener { dialogChangeEmail?.dismiss() }
        }
        binding.btnChangeEmail.setOnClickListener { dialogChangeEmail?.show() }

        binding.btnHowToVerify.setOnClickListener {
            if (ClickEvent.check(ClickEvent.BUTTON_CLICK)) {
                //sendToInternalBrowser(requireActivity(), getPlatformBasedWebViewUrl(viewModel.prefs.seletedLanguage + WebViewUrls.SHORT_HowtoVerify))
                startActivity(
                    Intent(activity, CommonFragmentActivity::class.java)
                        .putExtra(MyConstants.INTENT_PASS_COMMON_TYPE, "support")
                        .putExtra("FROM", "Home")
                )
            }
        }

        binding.btnVerifyEmail.setOnClickListener{
            val email = binding.txtEmail.text.toString().trim()

            if(email.isEmpty()){
                showError(R.string.err_enter_email)
            }else if (!validEmail(email))
                showError(R.string.err_invalid_email)
            else{
                //txtEmail.clearFocus()
                hideKeyboard(binding.txtEmail,requireActivity())
                viewModel.verifyEmail(email)
            }
        }

        binding.executePendingBindings()

        return binding.root
    }




    override fun emailChangeDone() {
        dialogChangeEmail?.dismiss()
    }

    fun showBankDeletePopup() {
        activity?.let {
            val dialog = MyDialog(it).getMyDialog(R.layout.dialog_for_delete_bank_rummy)
            dialog.show()
            dialog.view22.setBackgroundColor(Color.parseColor(viewModel.selectedColor.get()))
            dialog.txtNo.setOnClickListener {
                if (dialog.isShowing) dialog.dismiss()
            }
            dialog.txtYes.setOnClickListener {
                if (dialog.isShowing) dialog.dismiss()
                viewModel.deleteBank()
            }
        }
    }

    override fun fireBranchEvent(userId: Int) {
        activity?.let {
            viewModel.analyticsHelper.fireAttributesEvent(
                AnalyticsEventsKeys.EMAIL_VARIFY_REQUESTED, userId.toString()
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.isLoading.set(true)
        viewModel.fetchVerificationData()

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.isLoading.set(false)
            viewModel.isSwipeLoading.set(true)
            setEmail = false
            viewModel.fetchVerificationData()
        }
        observerVerificationItem()
    }

    private fun observerVerificationItem() {
        viewModel.verificationInfo.observe(viewLifecycleOwner){
            if(setEmail){
                setEmail = false
                binding.txtEmail.setText(it.Email?:"")
            }
            if (!it.EmailVerify) {
                binding.txtEmail.isFocusableInTouchMode=true
                binding.txtEmail.requestFocus()
            }

            binding.rvVerification.adapter = ProfileVerifyItemAdapter(viewModel.getVerificationItems(),
                listener = this)
        }
    }

    override fun onDeleteVerificationItem(item: ProfileVerificationItem) {
        if(item.isDeleteAble){
            showBankDeletePopup()
        }
    }

    override fun onClickWarning(view: View, item: ProfileVerificationItem) {
        showToolTip(activity, view, "Under Process")
    }

    override fun onVerificationItemClick(item: ProfileVerificationItem) {
        when(item.type){
            MyConstants.VERIFY_ITEM_PAN->{
                if (viewModel.data.value?.EmailVerify == true) {
                    startActivityForResult(
                        Intent(activity, VerificationActivity::class.java).putExtra("for", "pan"),
                        101
                    )
                } else showError(activity?.getString(R.string.please_verify_your_email_first))
            }

            MyConstants.VERIFY_ITEM_BANK->{
                if (viewModel.data.value?.EmailVerify == true) {
                    if (viewModel.data.value?.PanVerify == true) {
                        startActivityForResult(
                            Intent(activity, VerificationActivity::class.java)
                                .putExtra("for", "bank"), 101
                        )
                    } else {
                        showError(activity?.getString(R.string.please_verify_your_pan_first))
                    }
                } else showError(activity?.getString(R.string.please_verify_email_pan_to_verify_bank))
            }

            MyConstants.VERIFY_ITEM_ADDRESS->{
                launchAddressVerificationScreen("")
            }
        }
    }

    override fun goBack() {
        activity?.onBackPressed()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        viewModel.isLoading.set(true)
        viewModel.fetchVerificationData()
    }

    override fun showUploadingSheet(statusDataModel: BottomSheetStatusDataModel) {
        if (uploadingDialog == null) {
            uploadingDialog = LottieBottomSheetDialog(requireContext(), statusDataModel, this)
        } else {
            uploadingDialog?.updateData(statusDataModel)
        }
        uploadingDialog?.binding?.btnSubmitDone?.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(),statusDataModel.btnColorRes))
        uploadingDialog?.show()
    }
    var dialog: BottomSheetDialogBinding<BottomsheetDialogEmailVerifyRummyBinding>?= null
    override fun showVerifyEmailDialog() {
        if(dialog == null){
            dialog = BottomSheetDialogBinding(requireContext(),R.layout.bottomsheet_dialog_email_verify_rummy)
        }

        dialog?.binding?.apply {
            verifyModel = viewModel
            onEditClick = {
                dialog?.dismiss()
                binding.txtEmail.apply {
                    requestFocus()
                    showSoftKeyboard(this)
                }
            }
            onResndOtpClick = {

            }
            imgCross.setOnClickListener{
                hideKeyboard(root,requireActivity())
                viewModel.stopVerificationTimer()
                dialog?.dismiss()
            }

            btnSubmit.setOnClickListenerDebounce{
                val otp = includeOTPVerify.otpView.text.toString()
                if(TextUtils.isEmpty(otp)){
                    viewModel.wrongOtpErrorMSg.set(getString(R.string.err_enter_otp))
                    viewModel.isShowPinViewError.set(true)
                    return@setOnClickListenerDebounce
                }
                if(otp.length < 6){
                    viewModel.wrongOtpErrorMSg.set(getString(R.string.otp_you_entered_invalid))
                    viewModel.isShowPinViewError.set(true)
                    return@setOnClickListenerDebounce
                }
                viewModel.onEmailVerify(viewModel.email.get().toString(),includeOTPVerify.otpView.text.toString())
            }
            includeOTPVerify.otpView.text = null
            viewModel.isShowPinViewError.set(false)
            includeOTPVerify.otpView.addTextChangedListener{
                if(it.toString().length == 6) {
                    viewModel.isValidEmailVerify.set(true)
                    hideKeyboard(includeOTPVerify.otpView,requireActivity())
                    btnSubmit.performClick()
                }else
                    viewModel.isValidEmailVerify.set(false)
            }

        }
        dialog?.behavior?.isDraggable = false
        dialog?.setCanceledOnTouchOutside(false)
        dialog?.show()

    }

    fun showSoftKeyboard(view: View) {
        view.postDelayed({
            val imm: InputMethodManager =
                requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(view,0)
        },200)
    }

    override fun dismissVerifyEmailOtpDialog() {
        dialog?.dismiss()
    }

    fun hideKeyboard(view: View,context: Activity) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    override fun handleError(throwable: Throwable?) {
        binding.swipeRefresh.isRefreshing = false
    }

    override fun showMessage(message: String?) {
        binding.swipeRefresh.isRefreshing = false
        if (TextUtils.isEmpty(message)) return
        showMessageView(message ?: "")
    }

    override fun showError(message: String?) {
        binding.swipeRefresh.isRefreshing = false
        if (TextUtils.isEmpty(message)) return
        showErrorMessageView(message ?: "")
    }

    override fun showError(message: Int) {
        binding.swipeRefresh.isRefreshing = false
        if (message == 0) return
        showErrorMessageView(getString(message))
    }

    override fun logoutUser() {
        showError(R.string.err_session_expired)
        activity?.finishAffinity()
        startActivity(Intent(activity, RummyNewLoginActivity::class.java))
    }


    override fun onPause() {
        super.onPause()
        viewModel.stopVerificationTimer()
    }



    override fun onResume() {
        super.onResume()
        if(dialog?.isShowing == true) {
            viewModel.runTimerVerificationStatus()
        }
    }

    override fun getStringResource(resourseId: Int) = getString(resourseId)
}


interface VerificationNavigator {
    fun emailChangeDone()
    fun onVerificationItemClick(item: ProfileVerificationItem){}
    fun onDeleteVerificationItem(item: ProfileVerificationItem){}
    fun onClickWarning(view:View,item: ProfileVerificationItem){}
    fun fireBranchEvent(userId: Int)
    fun showUploadingSheet(statusDataModel: BottomSheetStatusDataModel){}
    fun showVerifyEmailDialog(){}
    fun dismissVerifyEmailOtpDialog(){}
}