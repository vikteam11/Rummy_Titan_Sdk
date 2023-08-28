package com.rummytitans.playcashrummyonline.cardgame.ui.deeplink

import com.rummytitans.playcashrummyonline.cardgame.R
import com.rummytitans.playcashrummyonline.cardgame.databinding.ActivityDeepLinkRummyBinding
import com.rummytitans.playcashrummyonline.cardgame.models.MatchModel
import com.rummytitans.playcashrummyonline.cardgame.ui.RummyMainActivity
import com.rummytitans.playcashrummyonline.cardgame.ui.base.BaseActivity
import com.rummytitans.playcashrummyonline.cardgame.utils.*
import com.rummytitans.playcashrummyonline.cardgame.widget.MyDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.appsflyer.AppsFlyerLib
import com.rummytitans.playcashrummyonline.cardgame.ui.newlogin.RummyNewLoginActivity
import com.rummytitans.playcashrummyonline.cardgame.ui.wallet.RummyAddCashActivity
import dagger.hilt.android.AndroidEntryPoint
//import com.google.firebase.dynamiclinks.ktx.dynamicLinks
//import com.google.firebase.ktx.Firebase
import javax.inject.Inject

class DeepLinkActivity : BaseActivity(), DeepLinkNavigator {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    lateinit var viewModel: DeepLinkViewModel
    lateinit var binding: ActivityDeepLinkRummyBinding

    var comingFor = 0
    val MATCH = 1
    val CONTEST = 2
    val CREATE_TEAM = 3
    val SPORTS_CHANGE = 4
    val APP_TYPE_CHANGE = 5

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(DeepLinkViewModel::class.java)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_deep_link_rummy)
        binding.lifecycleOwner = this
        AppsFlyerLib.getInstance().sendPushNotificationData(this)
        viewModel.apply {
            navigator = this@DeepLinkActivity
            navigatorAct = this@DeepLinkActivity
            myDialog = MyDialog(this@DeepLinkActivity)
            binding.viewModel = this
            checkForceUpdate()
        }


        if (intent.hasExtra("data")) {
            val data = intent.getStringExtra("data")
            redirection(getKeyValueOfQuery(data))
        }

        binding.executePendingBindings()
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
                    startActivity(Intent(this, RummyMainActivity::class.java))
                    finishAffinity()
                }else{
                    startActivity(Intent(this, RummyNewLoginActivity::class.java))
                    finishAffinity()
                }
            }
        }
        return map
    }

    fun redirection(map: Map<String, String>) {
        map.forEach {
            if (it.key=="withdrawDetail"){
               /* startActivity(
                    Intent(this, WithdrawDetailActivity::class.java)
                        .putExtra(MyConstants.INTENT_PASS_TRANSACTION_ID, it.value)
                )
                finish()*/
            }else if (it.key == "matchId") {
                viewModel.getMatchDetails(it.value)
                comingFor = MATCH
            } else if (it.key == "contestId") {
                viewModel.getInviteCodeDetails(it.value)
                comingFor = CONTEST
            } else if (it.key == "createTeam") {
                viewModel.getMatchDetails(it.value)
                comingFor = CREATE_TEAM
            } else if (it.key == "sports") {
                viewModel.changeSportsType(it.value.toInt())
                comingFor = SPORTS_CHANGE
            } else if (it.key == "sportswithapptype") {
                val value1 = it.value.split("-")
                val finalValue=if (!TextUtils.isEmpty(value1[0]) && TextUtils.isDigitsOnly(value1[0]))
                    value1[0].toInt()
                else   1
                viewModel.changeSportsType(finalValue)
                comingFor = SPORTS_CHANGE
            }else if (it.key == "web") {
                //sendToInternalBrowser(this, it.value)
                finish()
            } else if (it.key == "external") {
                sendToExternalBrowser(this, it.value)
                finish()
            }
            /*else if (it.key == "youtube") {
                sendToYoutubeVideo(it.value)
                finish()
            }*/
            else if (it.key == "poker") {
                startActivity(
                    Intent(this, RummyMainActivity::class.java)
                        .putExtra(MyConstants.INTENT_POKER_DATA, it.value)
                        .putExtra("comingForGame", true)
                )
                finish()
            } else if (it.key == "game") {
                finishAffinity()
                startActivity(
                    Intent(this, RummyMainActivity::class.java)
                        .putExtra(MyConstants.INTENT_GAME_DATA, it.value)
                        .putExtra("comingForGame", true)
                )
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
               // launchAddressVerificationScreen(rejectMsg,true)
                finish()
            }else if (it.key == "screen") {
                when (it.value) {
                    "main" -> {
                        finishAffinity()
                        startActivity(Intent(this, RummyMainActivity::class.java))
                    }

                    "games" -> {
                        startActivity(
                            Intent(this, RummyMainActivity::class.java)
                                .putExtra("comingForGame", true)
                        )
                        finish()
                    }
                   /* "gamesTicket" -> {
                        startActivity(Intent(this, GamesTicketActivity::class.java))
                        finish()
                    }
                    "notification" -> {
                        startActivity(Intent(this, NotificationActivity::class.java))
                        finish()
                    }
                    "profile" -> {
                        startActivity(Intent(this, ProfileActivity::class.java))
                        finish()
                    }
                    "addcash" -> {
                        startActivity(Intent(this, AddCashActivity::class.java)
                            .putExtra(MyConstants.INTENT_COME_FROM_GAME,
                                intent.getBooleanExtra(MyConstants.INTENT_COME_FROM_GAME,false))
                        )
                        finish()
                    }
                    "scratch" -> {
                        startActivity(Intent(this, ScrachCardActivity::class.java))
                        finish()
                    }
                    "offers" -> {
                        startActivity(Intent(this, OffersActivity::class.java))
                        finish()
                    }
                    "coupon" -> {
                        startActivity(Intent(this, CouponsListActivity::class.java))
                        finish()
                    }
                    "chat" -> {
                        startActivity(Intent(this, WebChatActivity::class.java))
                        finish()
                    }*/
                    "wallet" -> {
                        startActivity(Intent(this, RummyMainActivity::class.java)
                            .putExtra(MyConstants.INTENT_PASS_SELECT_TAB,"wallet"))
                        finish()
                    }
                    "refer","Refer"->{
                        startActivity(Intent(this, RummyMainActivity::class.java)
                            .putExtra(MyConstants.INTENT_PASS_SELECT_TAB,"refer"))
                        finish()
                    }
                    "rakeback"->{
                        startActivity(Intent(this, RummyMainActivity::class.java)
                            .putExtra(MyConstants.INTENT_PASS_SELECT_TAB,"rakeback"))
                        finish()
                    }
                    else -> {
                       /* startActivity(
                            Intent(this, CommonFragmentActivity::class.java)
                                .putExtra(MyConstants.INTENT_PASS_COMMON_TYPE, it.value)
                        )
                        finish()*/
                    }
                }
                return@forEach
            }
        }
    }



    override fun sendToContestActivity(matchModel: MatchModel?) {

    }

    override fun finishAllAndCallMainActivity() {
        finishAffinity()
        startActivity(Intent(this, RummyMainActivity::class.java))
    }

    override fun finishActivity() {
        Handler(mainLooper).postDelayed({
            if (isTaskRoot)
                finishAllAndCallMainActivity()
            else
                finish()
        },2000)
    }
}

interface DeepLinkNavigator {
    fun sendToContestActivity(matchModel: MatchModel?)
    fun finishAllAndCallMainActivity()
    fun finishActivity()
}