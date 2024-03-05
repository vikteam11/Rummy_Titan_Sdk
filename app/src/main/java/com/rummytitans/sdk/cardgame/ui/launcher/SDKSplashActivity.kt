package com.rummytitans.sdk.cardgame.ui.launcher

import com.rummytitans.sdk.cardgame.analytics.AnalyticsKey
import com.rummytitans.sdk.cardgame.ui.RummyMainActivity

import com.rummytitans.sdk.cardgame.widget.MyDialog
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.appsflyer.AppsFlyerLib
import com.rummytitans.sdk.cardgame.*
//import com.google.firebase.dynamiclinks.ktx.dynamicLinks
//import com.google.firebase.ktx.Firebase
//import com.google.firebase.messaging.FirebaseMessaging
import com.rummytitans.sdk.cardgame.databinding.ActivitySplashSdkBinding
import com.rummytitans.sdk.cardgame.models.LoginResponse
import com.rummytitans.sdk.cardgame.models.VersionModel
import com.rummytitans.sdk.cardgame.ui.base.BaseNavigator
import com.rummytitans.sdk.cardgame.ui.newlogin.RummyNewLoginActivity
import com.rummytitans.sdk.cardgame.utils.*
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.dialog_internet_rummy.*

@AndroidEntryPoint
class SDKSplashActivity : AppCompatActivity(),
    BaseNavigator {
    lateinit var binding: ActivitySplashSdkBinding

    lateinit var viewModel: LaunchViewModel
    private val fromAppUpdateBottomSheet: Int = 1221

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        this.intent = intent
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        window.transparentStatusBar()
        super.onCreate(savedInstanceState)
        AppsFlyerLib.getInstance().sendPushNotificationData(this)
        viewModel = ViewModelProvider(this).get(LaunchViewModel::class.java)
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.activity_splash_sdk,null,false)
        setContentView(binding.root)
        fetchAdvertisingId()
        viewModel.navigator = this
        viewModel.myDialog= MyDialog(this)

        if (viewModel.prefs.firstOpen) {
            viewModel.prefs.firstOpen = false
        }

        viewModel.versionResp.observe(this) {
            redirectUser()
            viewModel.prefs.let { pref ->
                pref.splashImageUrl = it.SplashImage
            }
        }

        if (TextUtils.isEmpty(viewModel.prefs.androidId)) {
            Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)?.let {
                viewModel.prefs.androidId = it
            }
        }

        viewModel.failedReason.observe(this, Observer {
            if (it.message == "timeout") {
                MyDialog(this).noInternetDialog { apiCall() }.txtCancel.setOnClickListener { finish() }
            } else showError(R.string.something_went_wrong_restart)
        })

        viewModel.analyticsHelper.fireEvent(AnalyticsKey.Names.AppOpened, bundleOf())

        viewModel.prefs.onSafePlay = true
        viewModel.analyticsHelper.fireEvent(
            AnalyticsKey.Names.ScreenLoadDone, bundleOf(
                AnalyticsKey.Keys.Screen to AnalyticsKey.Screens.SPLASH
            )
        )
    }

    override fun onResume() {
        super.onResume()
        apiCall()
    }
    private fun fetchAdvertisingId() {

           val d= Observable.fromCallable {
               AdvertisingIdClient.getAdvertisingIdInfo(this)?.id
           }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
               .doOnError {
                   it.printStackTrace()
               }
               .subscribe( {
                   if (it==null) return@subscribe
                   viewModel.prefs.advertisingId = it
               },{
                   it.printStackTrace()
               })

    }
    private fun apiCall() {
        viewModel.fetchVersion()
    }
    private fun redirectUser(){
        val loginModel: LoginResponse? = viewModel.gson.fromJson(
            viewModel.prefs.loginResponse,
            LoginResponse::class.java
        )
        viewModel.analyticsHelper.setUserID(loginModel?.UserId.toString())

        val i = Intent(this, RummyMainActivity::class.java)
        if (!TextUtils.isEmpty(viewModel.prefs.appsFlyerDeepLink)) {
            i.putExtra("comingForGame", true)
            i.putExtra("deepLink", intent.getStringExtra("deepLink"))
            viewModel.prefs.appsFlyerDeepLink = ""
        }
        startActivity(i)
        viewModel.prefs.isOldUser = true
        finish()
    }

    override fun goBack() {
        finishAffinity()
    }

    override fun handleError(throwable: Throwable?) {}

    override fun showError(message: String?) {}

    override fun showError(message: Int?) {}

    override fun showMessage(message: String?) {}

    override fun getStringResource(resourseId: Int) = getString(resourseId)

    override fun logoutUser() {
        showError(R.string.err_session_expired)
        finishAffinity()
        startActivity(Intent(this, RummyNewLoginActivity::class.java))
    }

    override fun onBackPressed() {
        viewModel.versionResp.value?.let {
            redirectUser()
        }
        super.onBackPressed()
    }
}
