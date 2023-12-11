package com.rummytitans.sdk.cardgame.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.rummytitans.sdk.cardgame.R
import com.rummytitans.sdk.cardgame.ui.launcher.SDKSplashActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LaunchActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launch)
        startActivity(Intent(this, SDKSplashActivity::class.java))
    }
}