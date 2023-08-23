package com.rummytitans.playcashrummyonline.cardgame.ui

import com.rummytitans.playcashrummyonline.cardgame.R
import com.rummytitans.playcashrummyonline.cardgame.analytics.AnalyticsHelper
import com.rummytitans.playcashrummyonline.cardgame.analytics.AnalyticsKey
import com.rummytitans.playcashrummyonline.cardgame.data.SharedPreferenceStorage
import com.rummytitans.playcashrummyonline.cardgame.databinding.ActivityHomeRummyBinding
import com.rummytitans.playcashrummyonline.cardgame.databinding.NotificationBadgeRummyBinding
import com.rummytitans.playcashrummyonline.cardgame.ui.base.BaseActivity
import com.rummytitans.playcashrummyonline.cardgame.ui.base.BaseFragment
import com.rummytitans.playcashrummyonline.cardgame.ui.deeplink.DeepLinkActivity
import com.rummytitans.playcashrummyonline.cardgame.ui.home.FragmentHome
import com.rummytitans.playcashrummyonline.cardgame.utils.*
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.PopupWindow
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomnavigation.BottomNavigationView
//import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.rummytitans.playcashrummyonline.cardgame.games.rummy.RummyWebViewActivity
import com.rummytitans.playcashrummyonline.cardgame.ui.more.FragmentMore
import com.rummytitans.playcashrummyonline.cardgame.ui.rakeback.RakeBackFragment
import com.rummytitans.playcashrummyonline.cardgame.ui.refer.FragmentShare
import com.rummytitans.playcashrummyonline.cardgame.ui.refer.ReferEarnActivity
import com.rummytitans.playcashrummyonline.cardgame.ui.wallet.FragmentWallet
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_home_rummy.*
import javax.inject.Inject

@AndroidEntryPoint
class RummyMainActivity : BaseActivity(), BottomNavigationView.OnNavigationItemSelectedListener,
   ActiveGameNavigator {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    lateinit var viewModel: MainViewModel

    lateinit var mCurrentFragment: BaseFragment

    @Inject
    lateinit var prefs: SharedPreferenceStorage

    @Inject
    lateinit var analyticsHelper: AnalyticsHelper

    @Inject
    lateinit var gson: Gson

    lateinit var binding: ActivityHomeRummyBinding
    lateinit var badgeBinding: NotificationBadgeRummyBinding
    var isNotificationApiCalled = false
    var pokerData = ""
    var gameID = ""
    private var referCode:String=""
    companion object {
        val FRAGMENT_ID = R.id.fragment_container
        const val REFER_CLICK = 1
        const val COUPON_CLICK = 2
        const val RANK_CLICK = 3
        const val SPORTSTIGER_CLICK = 4
        const val SUPPORT_CLICK = 5
        const val POLL_CLICK = 6
        const val FEEDBACK_CLICK = 7
        const val CHAT_CLICK = 9
        const val SETTING_CLICK = 10
        const val FAV_TEAM = 12
        const val TAG="MainActivity"

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkAndSetLanguage(this)
        //FirebaseMessaging.getInstance().subscribeToTopic("global")
        pokerData = intent?.getStringExtra(MyConstants.INTENT_POKER_DATA) ?: ""
        gameID = intent?.getStringExtra(MyConstants.INTENT_GAME_DATA) ?: ""
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        viewModel.navigatorAct = this
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home_rummy)
        binding.viewModel = viewModel

       binding.navigation.setOnNavigationItemSelectedListener(this)

        initFragments()

        //findViewById<View>(R.id.navigation_home).performClick()
        replaceFragment(FragmentHome())
        if (intent.hasExtra("deepLink"))
            startDeepLinkActivity(intent.getStringExtra("deepLink"))
        analyticsHelper.fireEvent(
            AnalyticsKey.Names.GameScreenLaunched
        )
        viewModel.displayHome.set(true)
       // binding.navigation.menu.findItem(R.id.navigation_rakeback)?.isVisible = false

        initClicks()
        disableTooltipFromNavigation()
        viewModel.checkForActiveMatch()

    }

    private fun disableTooltipFromNavigation() {
       /* binding.navigation.menu.forEach {
                //TooltipCompat.setTooltipText(binding.navigation.findViewById(it.itemId), null)
                binding.navigation.findViewById<View>(it.itemId)?.setOnLongClickListener{true}
        }*/
    }



    override fun onResume() {
        super.onResume()
        if(viewModel.displayHome.get()){
            viewModel.getWalletDetail()
        }

       /* if(viewModel.userAvtar.get() != prefs.avtarId){
            viewModel.fetchProfileData()
        }*/
    }


    private fun initClicks() {
        binding.ivRefers.setOnClickListenerDebounce {
            startActivity(
                Intent(this, ReferEarnActivity::class.java)
                    .putExtra(MyConstants.INTENT_PASS_REFER_CODE,referCode)
            )
        }
        binding.viewBgWallet.setOnClickListenerDebounce {
            viewModel.analyticsHelper.fireEvent(
                AnalyticsKey.Names.ButtonClick, bundleOf(
                    AnalyticsKey.Keys.ButtonName to AnalyticsKey.Values.Wallet ,
                    AnalyticsKey.Keys.Screen to AnalyticsKey.Values.Home
                )
            )

        }


    }
    fun redirectToTab(id:Int){
       // binding.navigation.selectedItemId = id
    }

    private fun initFragments() {
        replaceFragment(FragmentHome())
    }

    fun setNavigationItem(id :Int){
        //binding.navigation.menu.findItem(id)?.isChecked = true
    }
    fun animateView(showView: Boolean) {
        Log.i("JsObject", "hidestate $showView")
        binding.bottomView.visibility = if (showView) View.VISIBLE else View.GONE
    }

    private fun startDeepLinkActivity(deepLink: String?) {
        startActivity(
            Intent(this, DeepLinkActivity::class.java)
                .putExtra("deepLink", deepLink ?: "")
        )
    }

    fun replaceFragment(fragment:BaseFragment){
        viewModel.displayHome.set(fragment is FragmentHome)
        showSupportIcon(fragment is FragmentWallet || fragment is RakeBackFragment || fragment is FragmentShare)
        showReferIcon(false)
        mCurrentFragment = fragment
        supportFragmentManager.inTransaction {
            mCurrentFragment = fragment
            replace(FRAGMENT_ID, fragment)
        }
        //show badges when user move different tab
        /*if(fragment !is FragmentShare && viewModel.walletBalance.value?.showReferBadge()==true){
            binding.inBadgeRefer.root.visibility = View.VISIBLE
        }
        if(fragment !is FragmentWallet && viewModel.walletBalance.value?.showWalletBadge()==true ){
            binding.inBadgeWallet.root.visibility = View.VISIBLE
        }
        if(viewModel.displayHome.get()){
            viewModel.getWalletDetail()
        }*/
        if(viewModel.displayHome.get()){
            viewModel.getWalletDetail()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        for (i in 0 until supportFragmentManager.backStackEntryCount) {
            supportFragmentManager.popBackStack()
        }

        hideKeyboard()
        var tabName = ""
        when (item.itemId) {
            R.id.navigation_home -> {
                if (::mCurrentFragment.isInitialized) {
                    if (mCurrentFragment is FragmentHome)
                        return false
                }
                tabName = AnalyticsKey.Values.Home
                replaceFragment(FragmentHome())
            }
            R.id.navigation_wallet -> {
                if (mCurrentFragment is FragmentWallet)
                    return false
                tabName = AnalyticsKey.Values.Wallet
                binding.inBadgeWallet.root.visibility = View.GONE
                replaceFragment(FragmentWallet.newInstance(false))
            }
            R.id.navigation_rakeback -> {
                if (mCurrentFragment is RakeBackFragment)
                    return false
                tabName = AnalyticsKey.Values.RakeBack
                replaceFragment(RakeBackFragment())
            }
            R.id.navigation_refer -> {
                if (mCurrentFragment is FragmentShare)
                    return false
                tabName = AnalyticsKey.Values.Refer
                binding.inBadgeRefer.root.visibility = View.GONE
                replaceFragment(FragmentShare.newInstance(null,true))
            }
            R.id.navigation_more -> {
                if (mCurrentFragment is FragmentMore)
                    return false
                tabName = AnalyticsKey.Values.More
                replaceFragment(FragmentMore())
            }

        }
        analyticsHelper.addTrigger(AnalyticsKey.Keys.Screen,tabName)
        analyticsHelper.fireEvent(
            AnalyticsKey.Names.ButtonClick, bundleOf(
                AnalyticsKey.Keys.TabName to tabName,
                AnalyticsKey.Keys.Screen to AnalyticsKey.Screens.HOME
            )
        )
        return true
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(container?.windowToken, 0)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent?.getBooleanExtra(MyConstants.CALL_RECENT_MATCH_API,false)==true)
            viewModel.checkForActiveMatch()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        mCurrentFragment.onActivityResult(requestCode,resultCode,data)

    }

    //inCase of share fragment open in main activity
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val currentFragment = supportFragmentManager.findFragmentById(fragment_container.id)
        currentFragment?.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private var doubleBackToExitPressedOnce = false

    override fun onBackPressed() {
        if (mCurrentFragment is FragmentHome){
            if (doubleBackToExitPressedOnce) {
                finish()
                return
            }
            this.doubleBackToExitPressedOnce = true
            Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show()
            Handler(mainLooper).postDelayed({ doubleBackToExitPressedOnce = false }, 1500)
        }else{
            //binding.navigation.selectedItemId=R.id.navigation_home
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        popupWindow?.dismiss()
    }
    private var popupWindow:PopupWindow?= null
    fun setUpPopupWindow(msg:String?,tabId:Int,badgeBinding: NotificationBadgeRummyBinding) {

        /*binding.navigation.findViewById<BottomNavigationItemView>(tabId)?.let {tabView->
            badgeBinding.root.visibility = View.VISIBLE
            badgeBinding.root.viewTreeObserver.addOnGlobalLayoutListener {
                val xCor:Int = (tabView.x +(tabView.width/2)).toInt()
                val finalPosX:Int = xCor.minus(( badgeBinding.root.width/2))
                badgeBinding.txtTitle.text = msg
                badgeBinding.root.x = finalPosX.toFloat()
            }
        }*/
    }


    fun onAppUpdate(){
        //redirectToAppUpdateScreen(viewModel.versionResp,TAG)
    }

    fun showReferIcon(show:Boolean) {
        binding.ivRefers.visibility = if(show) View.VISIBLE else View.GONE
    }
    fun showSupportIcon(show:Boolean) {
        binding.ivSupport.visibility = if(show) View.VISIBLE else View.GONE
    }

    /*Called from child fragments to check user Address verified or not*/
    fun isAddressVerified() = viewModel.isAddressVerified

    fun setReferCode(referCode:String){
        this.referCode = referCode
    }

    override fun onFindActiveGame(webUrl: String) {
        startActivity(
            Intent(this, RummyWebViewActivity::class.java)
                .putExtra(MyConstants.INTENT_PASS_WEB_URL, webUrl)
        )
        overridePendingTransition(0,0)
    }
}