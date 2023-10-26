package com.rummytitans.playcashrummyonline.cardgame.ui.refer

import com.rummytitans.playcashrummyonline.cardgame.R
import com.rummytitans.playcashrummyonline.cardgame.ui.base.BaseActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_refer_rummy.*

class RummySDKReferActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_refer_rummy)
        icBack.setOnClickListener { onBackPressed() }
        txtOptionText.setOnClickListener { (supportFragmentManager.fragments[0] as FragmentRefer).showBottomSheet() }
        supportFragmentManager.beginTransaction().replace(fragment_container.id, FragmentRefer()).commit()
    }

}
