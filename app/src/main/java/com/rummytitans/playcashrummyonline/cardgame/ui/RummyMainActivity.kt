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
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.PopupWindow
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.forEach
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.google.android.material.bottomnavigation.BottomNavigationView
//import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.rummytitans.playcashrummyonline.cardgame.games.rummy.RummyWebViewActivity
import com.rummytitans.playcashrummyonline.cardgame.models.WalletInfoModel
import com.rummytitans.playcashrummyonline.cardgame.ui.common.CommonFragmentActivity
import com.rummytitans.playcashrummyonline.cardgame.ui.more.FragmentMore
import com.rummytitans.playcashrummyonline.cardgame.ui.rakeback.RakeBackFragment
import com.rummytitans.playcashrummyonline.cardgame.ui.refer.FragmentShare
import com.rummytitans.playcashrummyonline.cardgame.ui.refer.ReferEarnActivity
import com.rummytitans.playcashrummyonline.cardgame.ui.wallet.AddCashActivity
import com.rummytitans.playcashrummyonline.cardgame.ui.wallet.FragmentWallet
import com.rummytitans.playcashrummyonline.cardgame.ui.wallet.adapter.WalletBonusAdapter
import com.rummytitans.playcashrummyonline.cardgame.utils.alertDialog.AlertdialogModel
import com.rummytitans.playcashrummyonline.cardgame.utils.bottomsheets.BottomSheetAlertDialog
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
    private var doubleBackToExitPressedOnce = false

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
        viewModel.navigator = this
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home_rummy)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        binding.navigation.setOnNavigationItemSelectedListener(this)
        observeWalletData()
        initFragments()

        //findViewById<View>(R.id.navigation_home).performClick()
        replaceFragment(FragmentHome())
        if (intent.hasExtra("deepLink"))
            startDeepLinkActivity(intent.getStringExtra("deepLink"))
        analyticsHelper.fireEvent(
            AnalyticsKey.Names.GameScreenLaunched
        )
        viewModel.displayHome.set(true)

        initClicks()
        disableTooltipFromNavigation()
        viewModel.checkForActiveMatch()

    }

    private fun disableTooltipFromNavigation() {
        binding.navigation.menu.forEach{
            //TooltipCompat.setTooltipText(binding.navigation.findViewById(it.itemId), null)
            binding.navigation.findViewById<View>(it.itemId)?.setOnLongClickListener{true}
        }
    }
    override fun onResume() {
        super.onResume()
        if(viewModel.displayHome.get()){
            viewModel.getWalletDetail()
        }
        //fetch profile when comes form rummyTitans
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
            loadWalletData()
        }
        binding.viewBgMiniWallet.setOnClickListener {
            hideMiniWallet()
        }
        binding.bottomSheetMiniWallet.btnTopAddCash.setOnClickListenerDebounce {
            sendToAddCash()
        }
    }

    private fun loadWalletData(){
        if(!viewModel.isMiniWalletOpen.get()){
            displayMiniWallet()
            viewModel.walletInfo.value?.let {walletInfo->
                initPieChart(walletInfo)
                initWalletBonusList(walletInfo)
            }?: kotlin.run {
                viewModel.fetchWalletData()
            }
        }else{
            hideMiniWallet()
        }
    }

    private fun displayMiniWallet() {
        if(viewModel.isMiniWalletOpen.get()){
            return
        }
        val anim = AnimationUtils.loadAnimation(this,R.anim.bottom_sheet_slide_down)
        binding.layoutMiniWallet.startAnimation(anim)
        viewModel.isMiniWalletOpen.set(true)
    }

    private fun hideMiniWallet(){
        if(!viewModel.isMiniWalletOpen.get()){
            return
        }
        val anim = AnimationUtils.loadAnimation(this,R.anim.bottom_sheet_slide_up)
        binding.layoutMiniWallet.startAnimation(anim)
        binding.root.postDelayed({
            viewModel.isMiniWalletOpen.set(false)
        },500)
    }
    private fun initPieChart(walletInfoData: WalletInfoModel) {
        viewModel.isGraphVisible.set(true)
        val graphValues = ArrayList<PieEntry>()
        val colorArrayList = ArrayList<Int>()

        graphValues.add(
            PieEntry(
                getPercentValue(
                    walletInfoData.Balance.TotalAmount,
                    walletInfoData.Balance.Winning
                ), "Type1"
            )
        )
        graphValues.add(
            PieEntry(
                getPercentValue(
                    walletInfoData.Balance.TotalAmount,
                    walletInfoData.Balance.Unutilized
                ), "Type2"
            )
        )
        graphValues.add(
            PieEntry(
                getPercentValue(
                    walletInfoData.Balance.TotalAmount,
                    walletInfoData.Balance.Bonus
                ), "Type3"
            )
        )
        graphValues.add(
            PieEntry(
                getPercentValue(
                    walletInfoData.Balance.TotalAmount,
                    walletInfoData.Balance.DailyBonus
                ), "Type4"
            )
        )
        graphValues.add(
            PieEntry(
                getPercentValue(
                    walletInfoData.Balance.TotalAmount,
                    walletInfoData.Balance.SignUpBonus
                ), "Type5"
            )
        )
        val (c1,c2,c3,c4,c5) = //if(MyConstants.CURRENT_APP_TYPE==8)
           // arrayOf("#0684f2","#feaf17","#fe3265","#03cbff","#9d7aff")
        //else{
            arrayOf("#3ecb87","#e79556","#877aef","#03cbff","#9d7aff")
        //}
        colorArrayList.add(Color.parseColor(c1))
        colorArrayList.add(Color.parseColor(c2))
        colorArrayList.add(Color.parseColor(c3))
        colorArrayList.add(Color.parseColor(c4))
        colorArrayList.add(Color.parseColor(c5))

        val dataSet = PieDataSet(graphValues, "")
        dataSet.sliceSpace = 0f
        val data = PieData(dataSet)
        binding.bottomSheetMiniWallet.apply {
            val l = piechart.legend
            l.verticalAlignment = Legend.LegendVerticalAlignment.TOP
            l.horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
            l.orientation = Legend.LegendOrientation.VERTICAL
            l.setDrawInside(false)
            l.xEntrySpace = 7f
            l.yEntrySpace = 0f
            l.textSize = 0f
            l.isEnabled = false

            piechart.isDrawHoleEnabled = true
            piechart.transparentCircleRadius = 0f
            piechart.holeRadius = 85f
            dataSet.setColors(*colorArrayList.toIntArray())
            data.setValueTextSize(1f)
            data.setValueTextColor(Color.TRANSPARENT)
            piechart.animateXY(1400, 1400)
            piechart.setEntryLabelColor(Color.TRANSPARENT)
            piechart.highlightValues(null)
            data.setValueFormatter(PercentFormatter())
            piechart.data = data
            piechart.description.isEnabled = false
            //if(MyConstants.CURRENT_APP_TYPE != 1){
                piechart.setHoleColor(ContextCompat.getColor(this@RummyMainActivity,R.color.text_color6))
            //}
        }

    }

    private fun getPercentValue(total: Double, amount: Double) = (amount * 100 / total).toFloat()

    private fun initWalletBonusList(walletInfo: WalletInfoModel) {
        binding.bottomSheetMiniWallet.rvBones.apply {
           // val appColor = Color.parseColor(viewModel.prefs.appPrimaryColor)
            adapter = WalletBonusAdapter(walletInfo.Balance.BonusList,) {
                startActivity(
                    Intent(this@RummyMainActivity, CommonFragmentActivity::class.java)
                        .putExtra(MyConstants.INTENT_PASS_COMMON_TYPE, "CashBonus")
                        .putExtra(MyConstants.INTENT_PASS_WEB_TITLE,getString(R.string.credit_bonus) )
                )
            }
        }
    }

    fun redirectToTab(id:Int){
        binding.navigation.selectedItemId = id
    }

    private fun initFragments() {
        replaceFragment(FragmentHome())
    }

    fun setNavigationItem(id :Int){
        binding.navigation.menu.findItem(id)?.isChecked = true
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
        hideMiniWallet()
        viewModel.displayHome.set(fragment is FragmentHome)
        showSupportIcon(fragment is FragmentWallet || fragment is RakeBackFragment || fragment is FragmentShare)
        showReferIcon(false)
        mCurrentFragment = fragment
        supportFragmentManager.inTransaction {
            mCurrentFragment = fragment
            replace(FRAGMENT_ID, fragment)
        }
        //show badges when user move different tab
        if(fragment !is FragmentShare && viewModel.walletBalance.value?.showReferBadge()==true){
            binding.inBadgeRefer.root.visibility = View.VISIBLE
        }
        if(fragment !is FragmentWallet && viewModel.walletBalance.value?.showWalletBadge()==true ){
            binding.inBadgeWallet.root.visibility = View.VISIBLE
        }
        if(viewModel.displayHome.get()){
            viewModel.getWalletDetail()
        }
    }

    private fun observeWalletData(){
        viewModel.walletBalance.observe(this){
            if(it.showWalletBadge()){
                setUpPopupWindow(it.WalletTabMesage?:"",R.id.navigation_wallet,binding.inBadgeWallet)
            }else{
                binding.inBadgeWallet.root.visibility = View.GONE
            }

            if(it.showReferBadge()){
                setUpPopupWindow(it.ReferTabMesage?:"",R.id.navigation_refer,binding.inBadgeRefer)
            }else{
                binding.inBadgeRefer.root.visibility = View.GONE
            }
        }

        viewModel.walletInfo.observe(this){walletInfo->
            initPieChart(walletInfo)
            initWalletBonusList(walletInfo)
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
        if ( requestCode==MyConstants.REQUEST_CODE_ADD_CASH)
            viewModel.fetchWalletData()

    }

    //inCase of share fragment open in main activity
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val currentFragment = supportFragmentManager.findFragmentById(fragment_container.id)
        currentFragment?.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
    override fun onBackPressed() {
        if (mCurrentFragment is FragmentHome){
            if(viewModel.isMiniWalletOpen.get()){
                hideMiniWallet()
            }else{
                performBack()
            }
        }else{
            binding.navigation.selectedItemId=R.id.navigation_home
        }
    }

    private fun performBack() {
        BottomSheetAlertDialog(
            this, AlertdialogModel(
                if(viewModel.gameTicket.get() > 0) "Free Tickets still available" else "",
                getString(R.string.back_alert),
                getString(R.string.go_back),
                "Play",
                onNegativeClick = {
                   // if(isTaskRoot){
                     //   startActivity(Intent(this, MainActivity::class.java))
                    //}
                    finish()
                },
            ),
            viewModel.selectedColor.get()?:""
        ).show()
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

    override fun sendToAddCash() {
        startActivityForResult(
            Intent(this, AddCashActivity::class.java),
            MyConstants.REQUEST_CODE_ADD_CASH)
    }
}