package com.rummytitans.sdk.cardgame.ui.refer

import com.rummytitans.sdk.cardgame.R
import com.rummytitans.sdk.cardgame.databinding.ActivityRummyReferEarnBinding
import com.rummytitans.sdk.cardgame.ui.base.BaseActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.rummytitans.sdk.cardgame.utils.MyConstants

class ReferEarnActivity : BaseActivity() {
    private lateinit var binding: ActivityRummyReferEarnBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_rummy_refer_earn)
        binding.icBack.setOnClickListener { onBackPressed() }
        supportFragmentManager.beginTransaction().replace(binding.fragmentContainer.id, FragmentRefer.newInstance(
            false,
            intent.getStringExtra(MyConstants.INTENT_PASS_REFER_CODE)?:""
        )).commit()
    }

}
