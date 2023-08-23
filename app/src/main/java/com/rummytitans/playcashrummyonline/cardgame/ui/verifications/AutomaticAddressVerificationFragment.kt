package com.rummytitans.playcashrummyonline.cardgame.ui.verifications

import com.rummytitans.playcashrummyonline.cardgame.R
import com.rummytitans.playcashrummyonline.cardgame.databinding.FragmentAddressVerifyWithAadhaarRummyBinding
import com.rummytitans.playcashrummyonline.cardgame.ui.verifications.adapter.AadhaarVerifyNoteAdapter

import com.rummytitans.playcashrummyonline.cardgame.models.AddressKycContentModel
import com.rummytitans.playcashrummyonline.cardgame.ui.base.BaseActivity
import com.rummytitans.playcashrummyonline.cardgame.ui.base.BaseFragment
import com.rummytitans.playcashrummyonline.cardgame.ui.verifications.viewmodels.AddressVerificationViewModel
import com.rummytitans.playcashrummyonline.cardgame.utils.MyConstants
import com.rummytitans.playcashrummyonline.cardgame.utils.setOnClickListenerDebounce
import com.rummytitans.playcashrummyonline.cardgame.widget.MyDialog
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.URLUtil
import androidx.lifecycle.ViewModelProvider
import java.util.*
import javax.inject.Inject

class AutomaticAddressVerificationFragment : BaseFragment(), AddressVerificationNavigator {

    private val REQUEST_WEBVIEW = 1001
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var viewModel: AddressVerificationViewModel

    lateinit var binding: FragmentAddressVerifyWithAadhaarRummyBinding

    companion object{
        fun newInstance(kycNotes: AddressKycContentModel): AutomaticAddressVerificationFragment {
            val fragment = AutomaticAddressVerificationFragment()
            val args = Bundle()
            args.putSerializable(MyConstants.INTENT_PASS_KYS_NOTES,kycNotes)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onAadhaarInitialized(url: String) {
        if (TextUtils.isEmpty(url) || !URLUtil.isValidUrl(url)) return
        startActivityForResult(Intent(activity, WebAutoAddressVerifyActivity::class.java)
            .putExtra(MyConstants.INTENT_PASS_WEB_URL, url)
            .putExtra(MyConstants.INTENT_PASS_WEB_TITLE,getString(R.string.aadhaar_verification) )
            .putExtra(MyConstants.INTENT_PASS_SHOW_TOOLBAR, true)
        ,REQUEST_WEBVIEW)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel =
            ViewModelProvider(
                this.viewModelStore,
                viewModelFactory
            ).get(AddressVerificationViewModel::class.java)

        viewModel.navigatorAct = this
        (activity as? BaseActivity)?.let {
            viewModel.navigator = it
            viewModel.myDialog = MyDialog(it)
        }
        arguments?.getSerializable(MyConstants.INTENT_PASS_KYS_NOTES)?.let {
            viewModel.updateKycData(it as AddressKycContentModel)
        }
        binding =
            FragmentAddressVerifyWithAadhaarRummyBinding.inflate(localInflater ?: inflater, container, false)
                .apply {
                    lifecycleOwner = this@AutomaticAddressVerificationFragment
                    viewmodel = this@AutomaticAddressVerificationFragment.viewModel
                }
        binding.executePendingBindings()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvNotes.adapter = AadhaarVerifyNoteAdapter(viewModel.kycNotes.value?.Notes?: arrayListOf())
        initClicks()
    }

    override fun onResume() {
        super.onResume()
        (activity as? AddressVerificationActivity)?.setTitle(getString(R.string.verifyAddress))
    }

    private fun initClicks() {
        binding.txtComplteKyc.setOnClickListenerDebounce {
            (activity as? AddressVerificationActivity? )?.addFragment(
                FragmentManualAddressVerification.newInstance(viewModel.kycNotes.value,1)
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REQUEST_WEBVIEW && resultCode == Activity.RESULT_OK){
            requireActivity().setResult(Activity.RESULT_OK)
            requireActivity().finish()
        }
    }

}

