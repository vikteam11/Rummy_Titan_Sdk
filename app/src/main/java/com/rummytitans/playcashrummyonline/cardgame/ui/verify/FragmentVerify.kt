package com.rummytitans.playcashrummyonline.cardgame.ui.profile.verify

import com.rummytitans.playcashrummyonline.cardgame.R
import com.rummytitans.playcashrummyonline.cardgame.databinding.FragmentVerifyRummyBinding
import com.rummytitans.playcashrummyonline.cardgame.ui.base.BaseFragment
import com.rummytitans.playcashrummyonline.cardgame.ui.base.BaseNavigator
import com.rummytitans.playcashrummyonline.cardgame.ui.profile.verify.viewmodel.VerifyViewModel
import com.rummytitans.playcashrummyonline.cardgame.ui.verifications.VerificationActivity
import com.rummytitans.playcashrummyonline.cardgame.utils.*
import com.rummytitans.playcashrummyonline.cardgame.widget.MyDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import com.rummytitans.playcashrummyonline.cardgame.ui.newlogin.RummyNewLoginActivity
import com.rummytitans.playcashrummyonline.cardgame.ui.verify.adapter.ProfileVerifyItemAdapter
import kotlinx.android.synthetic.main.dialog_edit_email_rummy.*
import kotlinx.android.synthetic.main.dialog_for_delete_bank_rummy.*
import kotlinx.android.synthetic.main.fragment_verify_rummy.*
import javax.inject.Inject

class FragmentVerify : BaseFragment(),
    BaseNavigator, VerificationNavigator {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    var dialogChangeEmail: Dialog? = null
    lateinit var viewModel: VerifyViewModel
    lateinit var binding: FragmentVerifyRummyBinding

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
                sendToInternalBrowser(requireActivity(), viewModel.getHowtoVerifyWebUrls(),
                getString(R.string.how_to_verify))
            }
        }

        binding.executePendingBindings()
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        activity?.window
                ?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
            val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }

    override fun emailChangeDone() {
        dialogChangeEmail?.dismiss()
    }

    fun showBankDeletePopup() {
        activity?.let {
            val dialog = MyDialog(it).getMyDialog(R.layout.dialog_for_delete_bank_rummy)
            dialog.show()
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

        swipeRefresh.setOnRefreshListener {
            viewModel.isLoading.set(false)
            viewModel.isSwipeLoading.set(true)
            viewModel.fetchVerificationData()
        }
        observerVerificationItem()
    }
    private fun observerVerificationItem() {
        viewModel.verificationInfo.observe(viewLifecycleOwner){
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

    override fun handleError(throwable: Throwable?) {
        swipeRefresh.isRefreshing = false
    }

    override fun showMessage(message: String?) {
        swipeRefresh.isRefreshing = false
        if (TextUtils.isEmpty(message)) return
        showMessageView(message ?: "")
    }

    override fun showError(message: String?) {
        swipeRefresh.isRefreshing = false
        if (TextUtils.isEmpty(message)) return
        showErrorMessageView(message ?: "")
    }

    override fun showError(message: Int) {
        swipeRefresh.isRefreshing = false
        if (message == 0) return
        showErrorMessageView(getString(message))
    }

    override fun logoutUser() {
        showError(R.string.err_session_expired)
        activity?.finishAffinity()
        startActivity(Intent(activity, RummyNewLoginActivity::class.java))
    }

    override fun getStringResource(resourseId: Int) = getString(resourseId)
}

interface VerificationNavigator {
    fun onVerificationItemClick(item: ProfileVerificationItem){}
    fun onDeleteVerificationItem(item: ProfileVerificationItem){}
    fun onClickWarning(view:View,item: ProfileVerificationItem){}
    fun emailChangeDone()
    fun fireBranchEvent(userId: Int)
}