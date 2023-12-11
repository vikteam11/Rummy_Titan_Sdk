package com.rummytitans.sdk.cardgame.ui.verifications

import com.rummytitans.sdk.cardgame.R
import com.rummytitans.sdk.cardgame.data.SharedPreferenceStorageRummy
import com.rummytitans.sdk.cardgame.databinding.ActivityRummyVerificationBinding
import com.rummytitans.sdk.cardgame.ui.base.BaseActivity
import com.rummytitans.sdk.cardgame.ui.home.MainNavigationFragment
import com.rummytitans.sdk.cardgame.utils.inTransaction
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.activity_rummy_verification.*
import javax.inject.Inject


class VerificationActivity : BaseActivity() {

    @Inject
    lateinit var prefs: SharedPreferenceStorageRummy
    val FRAGMENT_ID = R.id.fragment_container
    lateinit var binding: ActivityRummyVerificationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_rummy_verification)
        icBack.setOnClickListener { onBackPressed() }

        intent.getStringExtra("for").let {
            if (it == "pan") {
                replaceFragment(FragmentPanVerification.newInstance())
                txtTitle.text = getString(R.string.act_verification_pan_card_verification)
            } else {
                replaceFragment(FragmentBankVerification.newInstance())
                txtTitle.text = getString(R.string.act_verification_bank_verification)
            }
        }
    }

    private fun <F> replaceFragment(fragment: F) where F : Fragment, F : MainNavigationFragment {
        supportFragmentManager.inTransaction { replace(FRAGMENT_ID, fragment) }
    }

}
