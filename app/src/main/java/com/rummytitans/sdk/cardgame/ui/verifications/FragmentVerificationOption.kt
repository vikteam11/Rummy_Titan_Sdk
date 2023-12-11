package com.rummytitans.sdk.cardgame.ui.verifications

import com.rummytitans.sdk.cardgame.R
import com.rummytitans.sdk.cardgame.analytics.AnalyticsKey
import com.rummytitans.sdk.cardgame.ui.verifications.adapter.AadhaarVerifyNoteAdapter

import com.rummytitans.sdk.cardgame.models.AddressKycContentModel
import com.rummytitans.sdk.cardgame.ui.base.BaseActivity
import com.rummytitans.sdk.cardgame.ui.base.BaseFragment
import com.rummytitans.sdk.cardgame.ui.verifications.viewmodels.AddressVerificationViewModel
import com.rummytitans.sdk.cardgame.utils.MyConstants
import com.rummytitans.sdk.cardgame.utils.setOnClickListenerDebounce
import com.rummytitans.sdk.cardgame.widget.MyDialog
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.URLUtil
import androidx.lifecycle.ViewModelProvider
import com.rummytitans.sdk.cardgame.databinding.FragmentVerificationOptionRummyBinding
import com.rummytitans.sdk.cardgame.models.VerificationOptionModel
import com.rummytitans.sdk.cardgame.ui.verifications.adapter.VerificationOptionAdapter
import com.rummytitans.sdk.cardgame.ui.verifications.adapter.VerificationOptionNavigator
import com.rummytitans.sdk.cardgame.utils.bottomsheets.LottieBottomSheetDialog
import com.rummytitans.sdk.cardgame.utils.bottomsheets.listeners.BottomSheetStatusListener
import com.rummytitans.sdk.cardgame.utils.bottomsheets.models.BottomSheetStatusDataModel
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import java.util.*
import javax.inject.Inject

class FragmentVerificationOption : BaseFragment(), AddressVerificationNavigator,VerificationOptionNavigator,BottomSheetStatusListener {

    private val REQUEST_WEBVIEW = 1001

    lateinit var viewModel: AddressVerificationViewModel

    lateinit var binding: FragmentVerificationOptionRummyBinding

    var uploadingDialog: LottieBottomSheetDialog?= null

    private var verificationOptionAdapter:VerificationOptionAdapter ?= null

    companion object{
        fun newInstance(kycNotes: AddressKycContentModel): FragmentVerificationOption {
            val fragment = FragmentVerificationOption()
            val args = Bundle()
            args.putSerializable(MyConstants.INTENT_PASS_KYS_NOTES,kycNotes)
            fragment.arguments = args
            return fragment
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel =
            ViewModelProvider(
                this
            ).get(AddressVerificationViewModel::class.java)

        viewModel.navigatorAct = this
        (activity as? BaseActivity)?.let {
            viewModel.navigator = it
            viewModel.myDialog = MyDialog(it)
        }
        arguments?.getSerializable(MyConstants.INTENT_PASS_KYS_NOTES)?.let {
            viewModel.updateKycData(it as AddressKycContentModel)
            /*IsInstantAadharAllow is not active then always show other Options */
            viewModel.isOtherOptionHide.set(viewModel.kycNotes.value?.IsInstantAadharAllow==true)
        }
        binding =
            FragmentVerificationOptionRummyBinding.inflate(localInflater ?: inflater, container, false)
                .apply {
                    lifecycleOwner = this@FragmentVerificationOption
                    viewmodel = this@FragmentVerificationOption.viewModel
                }
        viewModel.colorTextArray = listOf("Note:")
        viewModel.colorArray = listOf(R.color.text_color8,R.color.text_color2)
        viewModel.fontArray = listOf(R.font.rubik_medium,R.font.rubik_regular)
        binding.executePendingBindings()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.selectedDocument.set(3)
        initListener()
        binding.rvNotes.adapter = AadhaarVerifyNoteAdapter(viewModel.kycNotes.value?.ManualNotes?:arrayListOf(),
            showCheckBox = true)
        verificationOptionAdapter = VerificationOptionAdapter(viewModel.getVerifyOptions(),this)
        binding.verifyOptionList.adapter = verificationOptionAdapter
    }

    private fun initListener() {

        binding.edtAadhaar.doAfterTextChanged {
            if (it.toString().isNotEmpty()) {
                binding.edtAadhaar.error = null
                verificationOptionAdapter?.unselectAllItem()
                viewModel.setButtonEnabledOrDisabled()
                val formattedText = it.toString()
                    .replace(" ", "")
                    .chunked(4)
                    .joinToString(" ")
                if (formattedText != it.toString()) {
                    binding.edtAadhaar.setText(formattedText)
                    binding.edtAadhaar.setSelection(binding.edtAadhaar.length())
                }
            }
        }

        binding.btExpand.setOnClickListenerDebounce {
            expandView()
        }

        binding.btContinue.setOnClickListenerDebounce {
            if(viewModel.isButtonEnabled.get()){
                verificationOptionAdapter?.options?.singleOrNull{it.isSelect}?.let { docOption->
                    val eventValue= when(docOption.documentType){
                        0->{
                            viewModel.initAadhaar()
                            AnalyticsKey.Values.VerifyAadhaarViaOTP
                        }else->{
                            (activity as? RummySDKAddressVerificationActivity? )?.addFragment(
                                FragmentManualAddressVerification.newInstance(viewModel.kycNotes.value,docOption.documentType)
                            )

                            if(docOption.documentType==1)
                                AnalyticsKey.Values.UploadAadhaar
                            else
                                AnalyticsKey.Values.UploadDrivingLicense
                        }
                    }
                    viewModel.analyticsHelper.fireEvent(AnalyticsKey.Names.ButtonClick ,
                        bundleOf(AnalyticsKey.Keys.ButtonName to eventValue,
                            AnalyticsKey.Keys.Screen to AnalyticsKey.Screens.AddressVerification)
                    )
                }?: kotlin.run {
                    viewModel.verifyWithAadhaarNumber()
                }
            }
        }
    }

    override fun onVerificationSuccess(){
        viewModel.analyticsHelper.fireEvent(AnalyticsKey.Names.ButtonClick ,
            bundleOf(AnalyticsKey.Keys.ButtonName to AnalyticsKey.Values.GoBack,
                AnalyticsKey.Keys.Screen to AnalyticsKey.Screens.AddressVerification)
        )
        requireActivity().setResult(Activity.RESULT_OK)
        requireActivity().finish()
    }

    override fun onVerificationFailed() {
        viewModel.analyticsHelper.fireEvent(AnalyticsKey.Names.ButtonClick ,
            bundleOf(AnalyticsKey.Keys.ButtonName to AnalyticsKey.Values.TryAgain,
                AnalyticsKey.Keys.Screen to AnalyticsKey.Screens.AddressVerification)
        )
        binding.edtAadhaar.setText("")
        viewModel.isButtonEnabled.set(false)
    }

    override fun onAadhaarInitialized(url: String) {
        if (TextUtils.isEmpty(url) || !URLUtil.isValidUrl(url)) return
        startActivityForResult(Intent(activity, WebAutoAddressVerifyActivity::class.java)
            .putExtra(MyConstants.INTENT_PASS_WEB_URL, url)
            .putExtra(MyConstants.INTENT_PASS_WEB_TITLE,getString(R.string.aadhaar_verification) )
            .putExtra(MyConstants.INTENT_PASS_SHOW_TOOLBAR, true)
            ,REQUEST_WEBVIEW)
    }

    override fun showUploadingSheet(statusDataModel: BottomSheetStatusDataModel) {
        if (uploadingDialog == null) {
            uploadingDialog = LottieBottomSheetDialog(requireContext(), statusDataModel, this)
        } else {
            uploadingDialog?.updateData(statusDataModel)
        }
        uploadingDialog?.show()
    }

    override fun hideKeyboard() {
        super.hideKeyboard()
        (activity as? BaseActivity)?.hideKeyboardView()
    }

    override fun onResume() {
        super.onResume()
        (activity as? RummySDKAddressVerificationActivity)?.setTitle(getString(R.string.verifyAddress))
        if (::viewModel.isInitialized){
            val anyOtpSelected =  verificationOptionAdapter?.options?.any { it.isSelect }?:false
            if(anyOtpSelected){
                viewModel.isButtonEnabled.set(true)
            }else{
                viewModel.setButtonEnabledOrDisabled()
            }
        }

    }

    private fun expandView() {
        viewModel.analyticsHelper.fireEvent(
            AnalyticsKey.Names.ButtonClick ,
            bundleOf(
                AnalyticsKey.Keys.Screen to AnalyticsKey.Screens.AddressVerification,
                AnalyticsKey.Keys.ButtonName to if (viewModel.isOtherOptionHide.get())
                    AnalyticsKey.Values.ViewOtherMethods
                else AnalyticsKey.Values.HideMethods)
        )
        viewModel.isOtherOptionHide.set(!viewModel.isOtherOptionHide.get())
        if(viewModel.isOtherOptionHide.get()){
            verificationOptionAdapter?.unselectAllItem()
            viewModel.isButtonEnabled.set(viewModel.aadhaarNumber.isNotEmpty())
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REQUEST_WEBVIEW && resultCode == Activity.RESULT_OK){
            requireActivity().setResult(Activity.RESULT_OK)
            requireActivity().finish()
        }
    }

    override fun onSelectVerificationOption(model: VerificationOptionModel) {
        hideKeyboard()
        binding.edtAadhaar.setText("")
        viewModel.isButtonEnabled.set(true)
    }
}

