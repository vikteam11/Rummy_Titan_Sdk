package com.rummytitans.playcashrummyonline.cardgame.ui.verifications

import com.rummytitans.playcashrummyonline.cardgame.R
import com.rummytitans.playcashrummyonline.cardgame.analytics.AnalyticsKey
import com.rummytitans.playcashrummyonline.cardgame.databinding.ActivityRummyAddressVerificationBinding
import com.rummytitans.playcashrummyonline.cardgame.databinding.DialogAlertPopupRummyBinding
import com.rummytitans.playcashrummyonline.cardgame.ui.base.BaseActivity
import com.rummytitans.playcashrummyonline.cardgame.ui.common.CommonFragmentActivity
import com.rummytitans.playcashrummyonline.cardgame.ui.verifications.viewmodels.AddressVerificationViewModel
import com.rummytitans.playcashrummyonline.cardgame.utils.MyConstants
import com.rummytitans.playcashrummyonline.cardgame.utils.alertDialog.AlertdialogModel
import com.rummytitans.playcashrummyonline.cardgame.utils.bottomsheets.models.BottomSheetStatusDataModel
import com.rummytitans.playcashrummyonline.cardgame.utils.inTransaction
import com.rummytitans.playcashrummyonline.cardgame.utils.setOnClickListenerDebounce
import com.rummytitans.playcashrummyonline.cardgame.widget.MyDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider

interface AddressVerificationNavigator{
    fun hideKeyboard(){}
    fun showChooseUploadSheet(uploadType:Int){}
    fun showUploadingSheet(dataModel: BottomSheetStatusDataModel){}
    fun onAadhaarInitialized(url:String){}
}

class RummySDKAddressVerificationActivity : BaseActivity(), AddressVerificationNavigator {

   // @Inject
    //lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var viewModel: AddressVerificationViewModel

    lateinit var binding: ActivityRummyAddressVerificationBinding

    var isFreshUser=false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)
            .get(AddressVerificationViewModel::class.java)
        viewModel.navigatorAct = this
        viewModel.navigator = this
        viewModel.myDialog = MyDialog(this)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_rummy_address_verification)
        binding.viewmodel = viewModel
        binding.lifecycleOwner = this

        intent.getBooleanExtra("FROM_SPLASH",true).apply {
            if (!this) binding.icBack.visibility=View.VISIBLE
            isFreshUser=this
        }
        intent.getStringExtra(MyConstants.INTENT_PASS_VERIFICATION_REJECT_MSG)?.apply {
            viewModel.warningMessage.set(this)
        }

        viewModel.analyticsHelper.fireEvent(
            AnalyticsKey.Names.ScreenLoadDone,bundleOf(
            AnalyticsKey.Keys.Screen to AnalyticsKey.Screens.AddressVerification
        ))

        viewModel.fetchAddressVerificationContent()

        binding.icSupport.setOnClickListenerDebounce {
            viewModel.analyticsHelper.fireEvent(AnalyticsKey.Names.ButtonClick, bundleOf(
                AnalyticsKey.Keys.ButtonName to AnalyticsKey.Values.Support,
                AnalyticsKey.Keys.Screen to AnalyticsKey.Screens.AddressVerification
            ))
            startActivity(
                Intent(this, CommonFragmentActivity::class.java)
                    .putExtra(MyConstants.INTENT_PASS_COMMON_TYPE, "support")
                    .putExtra(MyConstants.INTENT_PASS_FROM, "Address Verify")
                    .putExtra(MyConstants.INTENT_PASS_DARK_STATUS, true)
            )
        }
        observerKycData()
    }

    fun setTitle(title:String){
        viewModel.toolbarTitle.set(title)
    }

    private fun observerKycData() {
        viewModel.kycNotes.observe(this){
            replaceFragment(FragmentVerificationOption.newInstance(it))
            if (!viewModel.warningMessage.get().isNullOrEmpty()) showAlertPopup()
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.inTransaction {
            replace(binding.fragmentContainer.id,fragment )
        }
    }

    fun addFragment(fragment: Fragment) {
        supportFragmentManager.inTransaction {
            replace(binding.fragmentContainer.id,fragment ).addToBackStack(null)
        }
    }

    private fun showAlertPopup() {
        viewModel.myDialog?.getAlertPopup<DialogAlertPopupRummyBinding>(
            AlertdialogModel(
                title = getString(R.string.verification_failed),
                description = viewModel.warningMessage.get()?:"",
                positiveText = getString(R.string.try_again),
                showClose = true,
                imgRes = R.drawable.ic_failed
            ),R.layout.dialog_alert_popup_rummy
        )?.show()
    }

    override fun onBackPressed() {
        /*val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (currentFragment is ManualAddressVerificationFragment){
            setTitle(getString(R.string.verifyAddress))
        }*/
        super.onBackPressed()
    }

}

