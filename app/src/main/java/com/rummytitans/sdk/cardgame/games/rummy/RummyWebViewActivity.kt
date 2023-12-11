package com.rummytitans.sdk.cardgame.games.rummy

import com.rummytitans.sdk.cardgame.R
import com.rummytitans.sdk.cardgame.databinding.ActivityGamesWebViewerRummyBinding
import com.rummytitans.sdk.cardgame.models.GamesResponseModel
import com.rummytitans.sdk.cardgame.ui.base.BaseActivity
import com.rummytitans.sdk.cardgame.games.GameEventModel
import com.rummytitans.sdk.cardgame.utils.*
import com.rummytitans.sdk.cardgame.utils.alertDialog.AlertdialogModel
import com.rummytitans.sdk.cardgame.widget.MyDialog
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.webkit.*
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import com.rummytitans.sdk.cardgame.analytics.AnalyticsKey
import com.rummytitans.sdk.cardgame.ui.RummyMainActivity
import com.rummytitans.sdk.cardgame.ui.newlogin.RummyNewLoginActivity
import com.rummytitans.sdk.cardgame.ui.wallet.RummyAddCashActivity
import kotlinx.android.synthetic.main.activity_games_web_viewer_rummy.*
import javax.inject.Inject


class RummyWebViewActivity() : BaseActivity(), RummyNavigator {

    var isStatusBarHidden = false
    lateinit var mBinding: ActivityGamesWebViewerRummyBinding


    private lateinit var mViewModel: RummyViewModel

    companion object {
        val LANDSCAPE = 0
        val PORTRAIT = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        drawOverStatusBar()
        super.onCreate(savedInstanceState)
        this.savedInstanceState = savedInstanceState
        mViewModel =
            ViewModelProvider(this).get(RummyViewModel::class.java)
                .apply {
                    navigator = this@RummyWebViewActivity
                    navigatorAct = this@RummyWebViewActivity
                    myDialog = MyDialog(this@RummyWebViewActivity)
                    intent?.getBooleanExtra(MyConstants.INTENT_PASS_FROM_ALL_GAMES, false)?.let {
                        comeFromAllGames.set(it)
                        if (it) isHeadersAvailable.set(false)
                    }
                }
        mBinding = DataBindingUtil.setContentView(this,R.layout.activity_games_web_viewer_rummy)
        hideStatusBar()
    }

    private var savedInstanceState:Bundle?= null

    override fun launchRummy(mGameModel: GamesResponseModel.GamesModel) {
        mViewModel.apply {
            if (!connectionDetector.isConnected) {
                myDialog?.noInternetDialogExit(
                    retryListener = { launchRummy(mGameModel) },
                    cancelListener = { exitFromGame()})
                return
            }
        }
        val finalUrl = intent.getStringExtra(MyConstants.INTENT_PASS_WEB_URL)?:""
        mBinding.webViewGame.apply {
            loadUrl(finalUrl)

            settings.apply {
                allowContentAccess = true
                allowFileAccess = true
                javaScriptEnabled = true
                loadWithOverviewMode = true
                useWideViewPort = true
                loadsImagesAutomatically = true
                domStorageEnabled = true
                databaseEnabled = true
            }
            addJavascriptInterface(JsObject(), "Android")
        }

        mBinding.webViewGame.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                Log.e("testcode", "url--> $url")
                view.loadUrl(url)
                return false
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                Log.d("savedInstanceState" ,"onPageStarted ")
            }
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                mBinding.webViewGame.visibility = View.VISIBLE
                Log.d("savedInstanceState" ,"onPageFinished ")

                Handler(Looper.getMainLooper()).postDelayed(
                    { ivSplash?.visibility = View.GONE },
                    200
                )
                view?.loadUrl(
                    "javascript:(function() {" +
                            "window.parent.addEventListener ('message', function(event) {" +
                            " Android.receiveMessage(JSON.stringify(event.data));});" +
                            "})()"
                )
            }

            override fun onReceivedError(
                view: WebView?, request: WebResourceRequest?, error: WebResourceError?
            ) {
                val url = request?.url.toString()
                Log.d("Chromium", "onReceivedError--> $url");
                if (url.startsWith("rgmrwebcall", true))
                    if (url.contains("data")) sendDataToWebView(url)
                super.onReceivedError(view, request, error)
            }
        }
    }

    private fun destroyAndRelaunchWebView() {
        mViewModel.apply {
            if (!connectionDetector.isConnected) {
                myDialog?.noInternetDialogExit(
                    retryListener = { destroyAndRelaunchWebView() },
                    cancelListener = { exitFromGame() })
                return
            }
            Handler(mainLooper).post {
                mBinding.webViewGame.reload()
            }
        }
    }

    private var saveRotateDeviceRequest=false


    private fun changeOrientation(target: Int) {
        this.apply {
            saveRotateDeviceRequest=false
            val orientation = resources.configuration.orientation
            if (target == PORTRAIT && orientation == Configuration.ORIENTATION_LANDSCAPE) {
                Log.i("jswrapper", "changeOrientation to PORTRAIT ")
               // Handler(mainLooper).post { showStatusBar() }
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                lastOrientation= "PORTRAIT"
            } else if (target == LANDSCAPE && orientation == Configuration.ORIENTATION_PORTRAIT) {
                Log.i("jswrapper", "changeOrientation to LANDSCAPE")
               // Handler(mainLooper).post { hideStatusBar() }
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                lastOrientation= "LANDSCAPE"
            }
        }
    }

    private fun hideStatusBar() {
        isStatusBarHidden = true
        WindowCompat.getInsetsController(window, window.decorView)?.apply {
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(WindowInsetsCompat.Type.statusBars())
            hide(WindowInsetsCompat.Type.navigationBars())
        }
    }

    private fun showStatusBar() {
        isStatusBarHidden = false
        WindowCompat.getInsetsController(window, window.decorView)?.apply {
            show(WindowInsetsCompat.Type.statusBars())
            show(WindowInsetsCompat.Type.navigationBars())
        }
    }

    private fun sendDataToWebView(data: String) {
        Log.d("Chromium", data);
        val s = "javascript: updateFromNative(\"$data\")"
        Log.d("Full Chromium-->", s)
        mBinding.webViewGame.evaluateJavascript(s, null)
    }

    override fun onResume() {
        super.onResume()
        val uri: Uri? = intent.data
        if (uri != null && (uri.toString().startsWith(MyConstants.AppDeeplink))) {
            val action = uri.getQueryParameter("action")
            Log.i("chromium", "get the deeplink $action")
            if (action == "rummyreload") destroyAndRelaunchWebView()
        }
    }

    var lastOrientation = "PORTRAIT"

    inner class JsObject {
        @JavascriptInterface
        fun receiveMessage(data: String) {
            Log.d("RummyTitan","data "+data)
            if(MyConstants.LOGS_ENABLE)
                mViewModel.uploadEvents( intent.getStringExtra(MyConstants.INTENT_PASS_WEB_URL)?:"",System.currentTimeMillis(),data)

            kotlin.runCatching {
                val eventModel = Gson().fromJson(data, GameEventModel::class.java)
                when(eventModel.redirectionType.lowercase()){
                    "landscape"->{
                        changeOrientation(PORTRAIT)
                    }
                    "portrait"->{
                        changeOrientation(LANDSCAPE)
                    }
                    "exit"->{
                        when(eventModel.redirectionParams.redirectionUrl?.lowercase()){
                            "addmoney" ->{
                                addCash(eventModel.redirectionParams.extraParams?.amount?:
                                MyConstants.DEFAULT_ADD_CASH_AMOUNT.toInt())
                            }
                            "dialog" ->{
                                showDialog(eventModel.redirectionParams.extraParams?.message?:"",true)
                            }
                            null ,"" ->{
                                exitFromGame()
                            }
                            else->{}
                        }
                    }

                    "logout"->{
                        exitDialog("Session Expired! Please login again to continue")
                    }
                    "reload"->{
                        destroyAndRelaunchWebView()
                    }
                }
            }
        }
    }

    private fun addCash(amount:Int){
        startActivityForResult(
            Intent(this, RummyAddCashActivity::class.java)
                .putExtra(MyConstants.INTENT_PASS_AMOUNT, amount.toDouble())
                .putExtra(MyConstants.INTENT_COME_FROM_GAME,
                    intent.getBooleanExtra(MyConstants.INTENT_COME_FROM_GAME,false)),
            MyConstants.REQUEST_CODE_ADD_CASH
        )
    }


    override fun getStringResource(resourseId: Int): String {
        return getString(resourseId)
    }

    override fun onBackPressed() {
        sendDataToWebView("back")
    }

    override fun showDialog(msg: String, isExit: Boolean) {
        val alertModel= AlertdialogModel(
            getString(R.string.app_name_rummy),
            msg,
            positiveText = "OK",
            onPositiveClick = {
                if (isExit){
                    exitFromGame()
                }
            }
        )
        mDialogTimesUp = MyDialog(this).getAlertDialog(alertModel)
        mDialogTimesUp?.show()
    }

    private fun exitFromGame() {
       runOnUiThread {
           changeOrientation(PORTRAIT)
           mBinding.webViewGame.destroy()
       }
        finish()
    }



    fun exitDialog(msg:String="",exitMSg:String="OK") {
        val alertModel= AlertdialogModel(
            getString(R.string.app_name_rummy),
            msg,
            positiveText = exitMSg,
            onPositiveClick = {
                mDialogTimesUp?.dismiss()
                mViewModel.analyticsHelper.fireEvent(AnalyticsKey.Names.LogOut)
                mViewModel.logoutUser()
                val intent = Intent(this@RummyWebViewActivity, RummyNewLoginActivity::class.java)
                intent.flags= Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                exitFromGame()
            },
        )
        mDialogTimesUp = MyDialog(this).getAlertDialog(alertModel)
        mDialogTimesUp?.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        startActivity(Intent(this,RummyMainActivity::class.java).putExtra(MyConstants.CALL_RECENT_MATCH_API,true))
        exitFromGame()
    }
}

