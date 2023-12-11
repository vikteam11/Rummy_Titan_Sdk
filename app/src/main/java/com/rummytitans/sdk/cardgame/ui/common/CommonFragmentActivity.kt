package com.rummytitans.sdk.cardgame.ui.common

import com.rummytitans.sdk.cardgame.R
import com.rummytitans.sdk.cardgame.analytics.AnalyticsHelper
import com.rummytitans.sdk.cardgame.data.SharedPreferenceStorageRummy
import com.rummytitans.sdk.cardgame.databinding.ActivityRummyCommonFragmentBinding
import com.rummytitans.sdk.cardgame.ui.base.BaseActivity
import com.rummytitans.sdk.cardgame.ui.more.FragmentSupport
import com.rummytitans.sdk.cardgame.ui.more.FragmentWebViews
import com.rummytitans.sdk.cardgame.utils.MyConstants
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.PopupWindow
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.appsflyer.AppsFlyerLib
import com.rummytitans.sdk.cardgame.ui.RummyMainActivity
import com.rummytitans.sdk.cardgame.ui.profile.info.FragmentProfileInfo
import com.rummytitans.sdk.cardgame.ui.verify.FragmentVerify
import com.rummytitans.sdk.cardgame.ui.refer.FragmentRefer
import com.rummytitans.sdk.cardgame.ui.settings.SettingsFragment
import com.rummytitans.sdk.cardgame.ui.wallet.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_rummy_common_fragment.*
import javax.inject.Inject

@AndroidEntryPoint
class CommonFragmentActivity : BaseActivity() {

    var popupSportType: PopupWindow? = null
    val RESPONSE_LANG_UPDATE = "RESPONSE_LANG_UPDATE"
    val RESPONSE_THEME_UPDATE = "RESPONSE_THEME_UPDATE"
    val RESPONSE_SETTING_UPDATE = 100


    @Inject
    lateinit var pref: SharedPreferenceStorageRummy

    @Inject
    lateinit var analyticsHelper: AnalyticsHelper


    lateinit var binding: ActivityRummyCommonFragmentBinding
    lateinit var viewModel: CommonViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppsFlyerLib.getInstance().sendPushNotificationData(this)

        viewModel = ViewModelProvider(
            this
        ).get(CommonViewModel::class.java)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_rummy_common_fragment)
        binding.lifecycleOwner = this
        binding.viewmodel = viewModel
        viewModel.navigator = this
        binding.executePendingBindings()

        icBack.setOnClickListener { onBackPressed() }

        setUpFragment(savedInstanceState)

        txtOptionText.setOnClickListener {
            popupSportType?.showAsDropDown(txtOptionText)
        }

        btnOption.setOnClickListener {
            if (currentFragment() is FragmentRefer) {
                (currentFragment() as? FragmentRefer)?.showBottomSheet()
            }
        }

        hideKeyboard()
    }

    fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(container?.windowToken, 0)
    }

    fun updateTheme(colorCode: String) {
        viewModel.selectedColor.set(colorCode)
        val selectedColor = Color.parseColor(colorCode)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = selectedColor
        }
    }

    fun setUpFragment(savedInstanceState: Bundle?) {

        intent?.getStringExtra(MyConstants.INTENT_PASS_COMMON_TYPE).let {

            if (null == it) {
                icDown.visibility = View.GONE
                txtOptionText.visibility = View.GONE
                titile.text = "Help Desk"
                var from = intent?.getStringExtra(MyConstants.INTENT_PASS_FROM) ?: ""
                addFragment(FragmentSupport.newInstance(from))
                return@let
            }

            when (it) {

                "settings" -> {
                    icDown.visibility = View.GONE
                    txtOptionText.visibility = View.GONE
                    titile.text = "Settings"
                    if (savedInstanceState == null) {
                        addFragment(SettingsFragment.newInstance())
                    }
                }

                "support" -> {
                    icDown.visibility = View.GONE
                    txtOptionText.visibility = View.GONE
                    titile.text = "Help Desk"
                    val from = intent?.getStringExtra(MyConstants.INTENT_PASS_FROM) ?: ""
                    val requestDialog =
                        intent?.getStringExtra(MyConstants.INTENT_PASS_COMING_FROM) ?: ""
                    addFragment(FragmentSupport.newInstance(from, requestDialog))
                }

                "editprofile" -> {
                    icDown.visibility = View.GONE
                    txtOptionText.visibility = View.GONE
                    titile.text = "Edit Profile"
                    addFragment(FragmentProfileInfo.newInstance())
                }
                "CashBonus" -> {
                    val title=intent.getStringExtra(MyConstants.INTENT_PASS_WEB_TITLE)?:""
                    topBar.visibility=View.GONE
                    addFragment(FragmentCashBonus.newInstance())
                }

                "withdraw" -> {
                    topBar.visibility = View.GONE
                    icDown.visibility = View.GONE
                    txtOptionText.visibility = View.GONE
                    titile.text = getString(R.string.withdraw_money)
                    addFragment(
                        FragmentWithdraw.newInstance(
                            intent?.getDoubleExtra("currentBalance", 0.0) ?: 0.0,
                            intent?.getStringExtra("widText") ?: "",
                            intent?.getDoubleExtra("totalWinnig", 0.0) ?: 0.0
                        )
                    )
                }

                "unutilized" -> {
                    topBar.visibility = View.GONE
                    icDown.visibility = View.GONE
                    txtOptionText.visibility = View.GONE
                    titile.text = getString(R.string.withdraw_money)
                    addFragment(
                        FragmentUnutilized.newInstance(
                            intent?.getDoubleExtra("currentBalance", 0.0) ?: 0.0,
                            intent?.getStringExtra("widText") ?: "",
                            intent?.getDoubleExtra("totalWinnig", 0.0) ?: 0.0
                        )
                    )
                }

                "wallet" -> {
                    viewModel.isWallet.set(true)
                    icDown.visibility = View.GONE
                    txtOptionText.visibility = View.GONE
                    titile.text = "Wallet"
                    val code = intent?.getStringExtra(MyConstants.INTENT_PASS_OPEN_REDDEM) ?: ""
                    addFragment(FragmentWallet.newInstance(true, code))
                }

                "verify" -> {
                    icDown.visibility = View.GONE
                    txtOptionText.visibility = View.GONE
                    titile.text = "Verify Account"
                    addFragment(FragmentVerify())
                }

                "refer" -> {
                    btnOption.visibility = View.GONE
                    icDown.visibility = View.GONE
                    txtOptionText.visibility = View.GONE
                    titile.text = "Refer & Earn"
                    addFragment(FragmentRefer.newInstance())

                }

                "feedback" -> {
                    icDown.visibility = View.GONE
                    txtOptionText.visibility = View.GONE
                    titile.text = "Feedback"
                    addFragment(FragmentFeedback.newInstance())
                }

                "web" -> {
                    icDown.visibility = View.GONE
                    txtOptionText.visibility = View.GONE
                    titile.text = intent.getStringExtra("title")
                    addFragment(
                        FragmentWebViews.newInstance(
                            intent.getStringExtra("url"),
                            intent.getStringExtra("title")
                        )
                    )
                }

                "recent" -> {
                    topBar.visibility = View.GONE
                    icDown.visibility = View.GONE
                    txtOptionText.visibility = View.GONE
                    titile.text = getString(R.string.frag_recent_transactions)
                    addFragment(
                        FragmentRecentTransactions.newInstance(intent.getIntExtra("tab", 0),intent.getDoubleExtra(
                                "currentBalance",0.0
                    ))
                    )
                }

            }
        }
    }

    fun addFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(fragment_container.id, fragment).commit()
    }

    override fun onBackPressed() {
        val currfrag = supportFragmentManager.findFragmentById(fragment_container.id)
        if (currfrag is SettingsFragment) {
            currfrag.viewModel.let {
                val intent = Intent()
                if (it.isLanguageChange) {
                    intent.putExtra(RESPONSE_LANG_UPDATE, true)
                }
                if (it.isThemeUpdated) {
                    intent.putExtra(RESPONSE_THEME_UPDATE, true)
                }
                setResult(RESPONSE_SETTING_UPDATE, intent)
                finish()
            }
        }
        if (currfrag is FragmentWebViews){
            if(!currfrag.onBackPressed())
                super.onBackPressed()
        }else if(isTaskRoot) {
            startActivity(Intent(this, RummyMainActivity::class.java))
            finish()
        }
        else
            super.onBackPressed()
    }


    fun currentFragment(): Fragment? {
        return supportFragmentManager.findFragmentById(R.id.fragment_container)
    }



    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val currfrag = supportFragmentManager.findFragmentById(fragment_container.id)
        currfrag?.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

}
