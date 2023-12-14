package com.rummytitans.sdk.cardgame.ui.wallet.withdrawal

import com.rummytitans.sdk.cardgame.R
import com.rummytitans.sdk.cardgame.databinding.ActivityRummyWithdrawDetailBinding
import com.rummytitans.sdk.cardgame.ui.base.BaseActivity
import com.rummytitans.sdk.cardgame.ui.common.CommonFragmentActivity
import com.rummytitans.sdk.cardgame.ui.wallet.withdrawal.adapter.WithdrawProgressAdapter
import com.rummytitans.sdk.cardgame.utils.MyConstants
import com.rummytitans.sdk.cardgame.utils.alertDialog.AlertdialogModel
import com.rummytitans.sdk.cardgame.utils.bottomsheets.BottomSheetAlertDialog
import com.rummytitans.sdk.cardgame.widget.MyDialog
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.rummytitans.sdk.cardgame.analytics.AnalyticsKey
import com.rummytitans.playcashrummyonline.cardgame.ui.RummyMainActivity
import com.rummytitans.sdk.cardgame.ui.wallet.withdrawal.viewmodel.WithdrawDetailViewModel
import javax.inject.Inject


class WithdrawDetailActivity : BaseActivity(), WithdrawalDetailNavigator {

    lateinit var mViewModel: WithdrawDetailViewModel
    lateinit var binding: ActivityRummyWithdrawDetailBinding
    var cancelWithdrawalDialog: BottomSheetAlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewModel = ViewModelProvider(this).get(WithdrawDetailViewModel::class.java)
        mViewModel.myDialog = MyDialog(this)
        mViewModel.navigator = this
        mViewModel.navigatorAct=this
        binding = DataBindingUtil.setContentView(this, R.layout.activity_rummy_withdraw_detail)
        binding.lifecycleOwner = this
        binding.viewModel = mViewModel
        intent?.getStringExtra(MyConstants.INTENT_PASS_TRANSACTION_ID)?.let {
            mViewModel.mTransactionID = it
            mViewModel.getWithdrawalDetailAsync()
        }

        mViewModel.withdrawalDetail.observe(this, {
            binding.rvProgress.adapter = WithdrawProgressAdapter(it.statusDetails)
        })

        binding.ivSupport.setOnClickListener {
            startActivity(
                Intent(this, CommonFragmentActivity::class.java)
                    .putExtra(MyConstants.INTENT_PASS_COMMON_TYPE, "support")
                    .putExtra("FROM", "Wallet")
            )
        }
    }

   override fun onWithdrawalCancel() {
       if (cancelWithdrawalDialog?.isShowing==true)
           return

       mViewModel.analyticsHelper.fireEvent(
           AnalyticsKey.Names.ButtonClick, bundleOf(
               AnalyticsKey.Keys.ButtonName to AnalyticsKey.Values.CancelWithdrawalRequest,
               AnalyticsKey.Keys.Screen to AnalyticsKey.Screens.Withdraw
           )
       )
       cancelWithdrawalDialog = BottomSheetAlertDialog(
           this, AlertdialogModel(
               getString(R.string.cancel_request),
               getString(R.string.are_you_surecancel_withdrawal_request),
               getString(R.string.no),
               getString(R.string.yes),
               imgRes = R.drawable.ic_withdrawal_cancel,
               onPositiveClick = {
                   mViewModel.cancelWithdrawalRequest()
               }
           ),
       )
       cancelWithdrawalDialog?.show()
    }

    override fun onWithdrawalCancelSuccessful() {
        setResult(Activity.RESULT_OK)
    }

    override fun onDestroy() {
        cancelWithdrawalDialog?.dismiss()
        super.onDestroy()
    }

    override fun onBackPressed() {
        when {
            isTaskRoot -> {
                startActivity(Intent(this, RummyMainActivity::class.java))
                finish()
            }
            else -> super.onBackPressed()
        }
    }
}

interface WithdrawalDetailNavigator {
    fun onWithdrawalCancel()
    fun onWithdrawalCancelSuccessful()
}