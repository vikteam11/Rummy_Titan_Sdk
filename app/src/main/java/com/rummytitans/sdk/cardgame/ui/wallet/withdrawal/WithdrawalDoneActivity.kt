package com.rummytitans.sdk.cardgame.ui.wallet.withdrawal

import com.rummytitans.sdk.cardgame.R
import com.rummytitans.sdk.cardgame.databinding.ActivityRummyWithdrawDoneBinding
import com.rummytitans.sdk.cardgame.models.WithdrawalSuccessCustomModel
import com.rummytitans.sdk.cardgame.ui.base.BaseActivity
import com.rummytitans.sdk.cardgame.utils.MyConstants
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.WindowCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableInt
import com.rummytitans.sdk.cardgame.analytics.AnalyticsHelper
import com.rummytitans.sdk.cardgame.analytics.AnalyticsKey
import com.rummytitans.sdk.cardgame.ui.common.CommonFragmentActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class WithdrawalDoneActivity : BaseActivity(){

    lateinit var binding: ActivityRummyWithdrawDoneBinding
    private var withdrawalStatusView  = ObservableInt(1)
    private var withdrawalModel : WithdrawalSuccessCustomModel? =null
    @Inject
    lateinit var analyticsHelper: AnalyticsHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_rummy_withdraw_done)

        window?.statusBarColor =
            ContextCompat.getColor(this, R.color.black)
        window?.let {
            WindowCompat.getInsetsController(it, binding.root)?.isAppearanceLightStatusBars =
                true
        }

        withdrawalModel = intent.getSerializableExtra(MyConstants.INTENT_PASS_WITHDRAWAL) as WithdrawalSuccessCustomModel
        binding.lifecycleOwner = this
        binding.model = withdrawalModel
        binding.withdrawalStatusView = withdrawalStatusView

        startAnimation()
        initListener()
    }

    private fun startAnimation(){
        if (withdrawalModel?.withdrawalStatus == true) {
            try {
                startWithdrawalAnimation(binding.animProgress)
                val resID = resources.getIdentifier("withdrawal_done", "raw", this?.packageName)
                val mediaPlayer: MediaPlayer = MediaPlayer.create(this, resID)
                mediaPlayer.start()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            startWithdrawalAnimation(binding.animFailed)
            val v = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v?.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                v?.vibrate(500)
            }
        }
    }

    private fun startWithdrawalAnimation(viewToAnimate: View){
        Thread{
            Thread.sleep(2000)
            runOnUiThread {
                viewToAnimate.animate().y(binding.ivSupport.bottom.toFloat())
                    .start()
            }
            Thread.sleep(500)
            withdrawalStatusView.set(2)
        }.start()
    }

    fun initListener(){
        binding.txTryAgain.setOnClickListener { onBackPressed() }
        binding.TvTrack.setOnClickListener {
            analyticsHelper.fireEvent(
                AnalyticsKey.Names.ButtonClick, bundleOf(
                    AnalyticsKey.Keys.ButtonName to AnalyticsKey.Values.TrackWitdrawalRequest,
                    AnalyticsKey.Keys.Screen to AnalyticsKey.Screens.Withdraw,
                )
            )
            startActivity(Intent(this, WithdrawDetailActivity::class.java)
                .putExtra(MyConstants.INTENT_PASS_TRANSACTION_ID,
                    withdrawalModel?.TnxId ?: ""))
            finish()
        }
        binding.ivSupport.setOnClickListener {
            analyticsHelper.fireEvent(
                AnalyticsKey.Names.ScreenLoadDone, bundleOf(
                    AnalyticsKey.Keys.ButtonName to AnalyticsKey.Values.Helpdesk,
                    AnalyticsKey.Keys.Screen to AnalyticsKey.Screens.Withdraw,
                )
            )
            startActivity(
                Intent(this, CommonFragmentActivity::class.java)
                    .putExtra(MyConstants.INTENT_PASS_COMMON_TYPE, "support")
                    .putExtra("FROM", "Wallet")
            )
        }
        binding.tvBackToWallet.setOnClickListener {
            analyticsHelper.fireEvent(
                AnalyticsKey.Names.ButtonClick, bundleOf(
                    AnalyticsKey.Keys.ButtonName to AnalyticsKey.Values.BackToWallet,
                    AnalyticsKey.Keys.Screen to AnalyticsKey.Screens.Withdraw,
                )
            )
            val intent = Intent()
            intent.putExtra("wallet",true)
            setResult(RESULT_OK,intent)
            finish()
        }

    }
    override fun onBackPressed() {
        super.onBackPressed()
    }
}