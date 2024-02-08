package com.rummytitans.sdk.cardgame.ui.deeplink

import com.rummytitans.sdk.cardgame.R
import com.rummytitans.sdk.cardgame.databinding.ActivityDeepLinkRummyBinding
import com.rummytitans.sdk.cardgame.models.MatchModel

import com.rummytitans.sdk.cardgame.ui.RummyMainActivity
import com.rummytitans.sdk.cardgame.ui.base.BaseActivity
import com.rummytitans.sdk.cardgame.utils.*
import com.rummytitans.sdk.cardgame.widget.MyDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.appsflyer.AppsFlyerLib
import com.rummytitans.sdk.cardgame.RummyTitanSDK
import com.rummytitans.sdk.cardgame.ui.WebViewActivity
import com.rummytitans.sdk.cardgame.ui.common.CommonFragmentActivity
import com.rummytitans.sdk.cardgame.ui.games.tickets.GamesTicketActivity
import com.rummytitans.sdk.cardgame.ui.newlogin.RummyNewLoginActivity
import com.rummytitans.sdk.cardgame.ui.wallet.RummyAddCashActivity
import com.rummytitans.sdk.cardgame.ui.wallet.withdrawal.WithdrawDetailActivity
import dagger.hilt.android.AndroidEntryPoint
//import com.google.firebase.dynamiclinks.ktx.dynamicLinks
//import com.google.firebase.ktx.Firebase

@AndroidEntryPoint
class DeepLinkActivityRummy : BaseActivity(), DeepLinkNavigator {


    lateinit var viewModel: DeepLinkRummyViewModel
    lateinit var binding: ActivityDeepLinkRummyBinding

    var comingFor = 0
    val MATCH = 1
    val CONTEST = 2
    val CREATE_TEAM = 3
    val SPORTS_CHANGE = 4
    val APP_TYPE_CHANGE = 5

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(DeepLinkRummyViewModel::class.java)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_deep_link_rummy)
        binding.lifecycleOwner = this
        AppsFlyerLib.getInstance().sendPushNotificationData(this)
        viewModel.apply {
            navigator = this@DeepLinkActivityRummy
            navigatorAct = this@DeepLinkActivityRummy
            myDialog = MyDialog(this@DeepLinkActivityRummy)
            binding.viewModel = this
            checkForceUpdate()
        }

        viewModel.isForceUpdate.observe(this, Observer {model->
            if (model.ForceUpdate) {
                if (model.playStoreApkUpdateFrom==model.UPDATE_FROM_APP_STORE)
                    sendToPlayStore(this,packageName)
                else{
                    // todo updateApp
                }
                finish()
            } else {
                handleIntent(intent)
            }
        })

        if (intent.hasExtra("data")) {
            val data = intent.getStringExtra("data")
            redirection(getKeyValueOfQuery(data))
        }

        binding.executePendingBindings()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        // On Android 12, Raise notification clicked event when Activity is already running in activity backstack
        if(intent?.getBooleanExtra("isFromClever",false) == true) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                deepLinkOnNewIntent(intent.extras)
            }
        }
    }

    private fun fetchDataFromFirebaseDynamicLink(){
       /* Firebase.dynamicLinks
            .getDynamicLink(intent)
            .addOnSuccessListener(this) { pendingDynamicLinkData ->
                Log.e("inAppTest", "getDynamicLink:addOnSuccessListener "+pendingDynamicLinkData?.link)
                val link =  pendingDynamicLinkData?.link?.toString()?:""
                var splitText = ""
                splitText = if(link.contains(MyConstants.AppDeeplink)){
                    MyConstants.AppDeeplink
                }else{
                    MyConstants.AppDeeplink_
                }
                link.split("$splitText://?").
                elementAtOrNull(1)?.let {
                    kotlin.runCatching {
                        val key= it.split("=")[0]
                        val value= it.split("=")[1]
                        redirection(mapOf(Pair(key,value)))
                    }
                }
            }
            .addOnFailureListener(this) {  Log.e("inAppTest", "getDynamicLink:onFailure $it") }*/
    }

    fun handleIntent(deepIntent: Intent?) {
        if (!viewModel.prefs.loginCompleted) {
            showError(R.string.err_login_account_to_continue)
            startActivity(Intent(this, RummyNewLoginActivity::class.java))
            finishAffinity()
            return
        }
        if (Intent.ACTION_VIEW == deepIntent?.action && null != deepIntent.data) {
            val data = deepIntent.data?.query ?: deepIntent.data?.lastPathSegment ?: ""
            redirection(getKeyValueOfQuery(data))
        }

        if (intent.hasExtra("deepLink")) {
            val deeplinkUrl = intent.getStringExtra("deepLink") ?: ""
            if (deeplinkUrl.contains("?")) {
                val params = deeplinkUrl.split("?")[1]
                redirection(getKeyValueOfQuery(params))
            }
        }
        if (intent.hasExtra("fireBaseDeeplink")) {
            val inAppDeeplink = intent.getStringExtra("fireBaseDeeplink") ?: ""
            inAppDeeplink.split("//?")
                .elementAtOrNull(1)
                ?.split("=")
                ?.let {
                    redirection( mapOf(Pair(it[0],it[1])) )
                }
        }
        fetchDataFromFirebaseDynamicLink()
    }

    //matchId=12345&staId=1232
    private fun getKeyValueOfQuery(query: String?): Map<String, String> {
        val map = HashMap<String, String>()
        val pairs = query?.split("&")
        pairs?.forEach { pair ->
            if (pair.contains("=")) {
                val split = pair.split("=")
                if (split.size == 2) map[split[0]] = split[1]
                else if (split.size == 1) map[split[0]] = ""
            }else{
                if (viewModel.prefs.loginCompleted) {
                   goToHome()
                }else{
                    RummyTitanSDK.rummyCallback?.logoutUser()
                }
            }
        }
        return map
    }

    fun redirection(map: Map<String, String>) {
        map.forEach {
            if (it.key == "action") {
                val action = map[it.key] ?: ""
                viewModel.getDeeplinkUrl(action)
            }
           else if (it.key=="withdrawDetail"){
                startActivity(
                    Intent(this, WithdrawDetailActivity::class.java)
                        .putExtra(MyConstants.INTENT_PASS_TRANSACTION_ID, it.value)
                )
                finish()
            }else if (it.key == "web") {
                sendToInternalBrowser(this, it.value)
                finish()
            } else if (it.key == "external") {
                sendToExternalBrowser(this, it.value)
                finish()
            }
            /*else if (it.key == "youtube") {
                sendToYoutubeVideo(it.value)
                finish()
            }*/
             else if (it.key == "game") {
                 goToHome()
            } else if (it.key == "addcashamount" || it.key == "addcash") {
                startActivity(
                    Intent(this, RummyAddCashActivity::class.java)
                        .putExtra(MyConstants.INTENT_PASS_AMOUNT, it.value.toDouble())
                        .putExtra(MyConstants.INTENT_COME_FROM_GAME,
                            intent.getBooleanExtra(MyConstants.INTENT_COME_FROM_GAME,false))
                )
                finish()
            }else if(it.key == "addressVerification") {
                val rejectMsg = if (TextUtils.isEmpty(it.value))
                    ""
                else
                    it.value
                launchAddressVerificationScreen(rejectMsg,true)
                finish()
            }else if (it.key == "screen") {
                when (it.value) {
                    "main" -> {
                        goToHome()
                    }

                    "games" -> {
                        goToHome()
                    }
                    "gamesTicket" -> {
                        startActivity(Intent(this, GamesTicketActivity::class.java))
                        finish()
                    }
                    "profile" -> {
                       // startActivity(Intent(this, ProfileActivity::class.java))
                        RummyTitanSDK.rummyCallback?.openProfile()
                        RummyTitanSDK.rummyCallback?.sdkFinish()
                        finish()
                    }
                    "addcash" -> {
                        startActivity(Intent(this, RummyAddCashActivity::class.java)
                            .putExtra(MyConstants.INTENT_COME_FROM_GAME,
                                intent.getBooleanExtra(MyConstants.INTENT_COME_FROM_GAME,false))
                        )
                        finish()
                    }

                    "wallet" -> {
                        goToHome(tabName = "wallet")
                    }
                    "refer","Refer"->{
                        goToHome(tabName = "refer")
                    }
                    "rakeback"->{
                        goToHome(tabName = "rakeback")
                    }
                    "depositBonusHistory" -> {
                        startActivity(Intent(this, CommonFragmentActivity::class.java)
                            .putExtra(MyConstants.INTENT_PASS_COMMON_TYPE, "CashBonus")
                            .putExtra(MyConstants.INTENT_PASS_WEB_TITLE, "Deposit Bonus")
                        )
                        finish()
                    }
                    "gameBonusHistory" -> {
                        startActivity(Intent(this, CommonFragmentActivity::class.java)
                            .putExtra(MyConstants.INTENT_PASS_COMMON_TYPE, "CashBonus")
                            .putExtra(MyConstants.INTENT_PASS_WEB_TITLE, getString(R.string.game_bonus))
                        )
                        finish()
                    }
                    "conversionBonusHistory" -> {
                        startActivity(Intent(this, CommonFragmentActivity::class.java)
                            .putExtra(MyConstants.INTENT_PASS_COMMON_TYPE, "CashBonus")
                            .putExtra(MyConstants.INTENT_PASS_WEB_TITLE, "Conversion Bonus")
                        )
                        finish()
                    }
                    else -> {
                       goToHome()
                    }
                }
                return@forEach
            }
            else  {
            goToHome()
          }
        }
    }



    override fun sendToContestActivity(matchModel: MatchModel?) {

    }

    override fun finishAllAndCallMainActivity() {
        goToHome()
    }

    override fun finishActivity() {
        Handler(mainLooper).postDelayed({
            if (isTaskRoot)
                finishAllAndCallMainActivity()
            else
                finish()
        },2000)
    }
    private fun goToHome(tabName:String="",deeplinkStr:String?=""){
        val intent =  Intent(this, RummyMainActivity::class.java)

        if(!TextUtils.isEmpty(tabName)){
            intent.putExtra(MyConstants.INTENT_PASS_SELECT_TAB,tabName)
        }
        deeplinkStr?.let { deeplink-> }
        startActivity(intent)
        finish()
    }

    override fun openWebView(title: String, url: String) {
        startActivity(
            Intent(this, WebViewActivity::class.java)
                .putExtra(MyConstants.INTENT_PASS_WEB_URL, url)
                .putExtra(MyConstants.INTENT_PASS_WEB_TITLE, title)
        )
        finish()
    }
}

interface DeepLinkNavigator {
    fun sendToContestActivity(matchModel: MatchModel?){}
    fun finishAllAndCallMainActivity(){}
    fun finishActivity(){}
    fun deepLinkOnNewIntent(bundle: Bundle?){}
    fun openWebView(title: String, url: String){}
}



