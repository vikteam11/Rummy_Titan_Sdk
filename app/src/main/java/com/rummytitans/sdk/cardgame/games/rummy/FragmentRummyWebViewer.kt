package com.rummytitans.sdk.cardgame.games.rummy

import com.rummytitans.sdk.cardgame.BuildConfig
import com.rummytitans.sdk.cardgame.R
import com.rummytitans.sdk.cardgame.analytics.AnalyticsKey
import com.rummytitans.sdk.cardgame.databinding.ActivityGamesWebViewerRummyBinding
import com.rummytitans.sdk.cardgame.models.GamesResponseModel
import com.rummytitans.sdk.cardgame.models.LoginResponse
import com.rummytitans.sdk.cardgame.ui.base.BaseFragment
import com.rummytitans.sdk.cardgame.ui.newlogin.RummyNewLoginActivity
import com.rummytitans.sdk.cardgame.utils.*
import com.rummytitans.sdk.cardgame.utils.alertDialog.ALERT_DIALOG_NEGETIVE
import com.rummytitans.sdk.cardgame.utils.alertDialog.AlertdialogModel
import com.rummytitans.sdk.cardgame.widget.MyDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.*
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModelProvider
import com.rummytitans.sdk.cardgame.ui.RummyMainActivity
import com.rummytitans.playcashrummyonline.cardgame.ui.RummyMainActivity
import com.rummytitans.sdk.cardgame.ui.base.BaseNavigator
import kotlinx.android.synthetic.main.activity_games_web_viewer_rummy.*
import org.json.JSONObject


class FragmentRummyWebViewer() : BaseFragment(),
    BaseNavigator, RummyNavigator {

    var isStatusBarHidden = false
    lateinit var mBinding: ActivityGamesWebViewerRummyBinding

    private lateinit var mViewModel: RummyViewModel

    companion object {
        val LANDSCAPE = 0
        val PORTRAIT = 1
        fun getInstance() = FragmentRummyWebViewer()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        with(requireActivity()) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        mViewModel =
            ViewModelProvider(this).get(RummyViewModel::class.java)
                .apply {
                    navigator = this@FragmentRummyWebViewer
                    navigatorAct = this@FragmentRummyWebViewer
                    myDialog = MyDialog(requireActivity())
                    arguments?.apply {
                        getBoolean(MyConstants.INTENT_PASS_FROM_ALL_GAMES, false).let {
                            comeFromAllGames.set(it)
                            if (it) isHeadersAvailable.set(false)
                        }
                    }
                }
        mBinding = ActivityGamesWebViewerRummyBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    private var savedInstanceState:Bundle?= null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.savedInstanceState = savedInstanceState
    }

    override fun launchRummy(mGameModel: GamesResponseModel.GamesModel) {
        mViewModel.apply {
            if (!connectionDetector.isConnected) {
                myDialog?.noInternetDialogExit(
                    retryListener = { launchRummy(mGameModel) },
                    cancelListener = { requireActivity().finish() })
                return
            }
        }
        val json = JSONObject()
        var finalUrl = ""
        mViewModel.let { viewModel ->
            json.put("user_id", viewModel.loginModel.UserId)
            json.put("auth_expire", viewModel.loginModel.AuthExpire)//"c321f3242c34234c234c23")
            json.put("expire_token", viewModel.loginModel.ExpireToken)
            json.put("app_version", BuildConfig.VERSION_CODE)
            json.put("unique_id", viewModel.prefs.androidId)
            json.put("app_type",  if(BuildConfig.isPlayStoreApk==1) 3 else 2)
            json.put("lobbyID", "rummy")
            json.put("State", viewModel.loginModel.gameState)
            json.put("DeviceType", "Android")
            json.put("DeviceID", viewModel.prefs.androidId)
            json.put("IPAddress", getIpAddress())
            json.put("PINCode", viewModel.prefs.PinCode)
            json.put("KYCStatus", viewModel.prefs.KycStatus)
            finalUrl = mGameModel.gameUrl+"info="+json.toString().toBase64()
        }

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
                return if (url.contains(MyConstants.AppDeeplink+":")) {
                    println("DEEP link --> $url")
                    when {
                        url.contains("rummyReset") -> sendDataToWebView(url)
                        url.contains("screen=main") -> requireActivity().finish()
                        url.contains("screen=logout") -> {
                            startActivity(Intent(requireActivity(),RummyNewLoginActivity::class.java))
                            requireActivity().finishAffinity()
                        }
                        url.contains("screen=rummyreload") -> destroyAndRelaunchWebView()
                        else -> requireActivity().openDeeplink(url,true)
                    }
                    true
                } else {
                    view.loadUrl(url)
                    false
                }
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
               /* myDialog?.noInternetDialogExit(
                    retryListener = { destroyAndRelaunchWebView() },
                    cancelListener = { requireActivity().finish() })*/
                return
            }
            mBinding.webViewGame.reload()
        }
    }

    private var saveRotateDeviceRequest=false

    override fun performSavedActions() {
        if (saveRotateDeviceRequest)
            changeOrientation(Configuration.ORIENTATION_LANDSCAPE)
    }

    private fun changeOrientation(target: Int) {
        requireActivity().apply {
            val allowRotate=(this as? RummyMainActivity)?.let {
                it.mCurrentFragment is FragmentRummyWebViewer
            }?:false
            if (allowRotate){
                saveRotateDeviceRequest=false
                val orientation = resources.configuration.orientation
                if (target == PORTRAIT && orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    Log.i("jswrapper", "changeOrientation to PORTRAIT ")
                    Handler(mainLooper).post { showStatusBar() }
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    lastOrientation= "PORTRAIT"
                } else if (target == LANDSCAPE && orientation == Configuration.ORIENTATION_PORTRAIT) {
                    Log.i("jswrapper", "changeOrientation to LANDSCAPE")
                    Handler(mainLooper).post { hideStatusBar() }
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    lastOrientation= "LANDSCAPE"
                }
                Handler(Looper.getMainLooper()).postDelayed({
                    (activity as RummyMainActivity).animateView(target == Configuration.ORIENTATION_PORTRAIT)
                },  100)
            }else
                saveRotateDeviceRequest=true
        }
    }

    private fun hideStatusBar() {
        isStatusBarHidden = true
        requireActivity().apply {
            WindowCompat.getInsetsController(window, window.decorView)?.apply {
                systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                hide(WindowInsetsCompat.Type.statusBars())
                hide(WindowInsetsCompat.Type.navigationBars())
            }
        }
    }

    private fun showStatusBar() {
        isStatusBarHidden = false
        requireActivity().apply {
            WindowCompat.getInsetsController(window, window.decorView)?.apply {
                show(WindowInsetsCompat.Type.statusBars())
                show(WindowInsetsCompat.Type.navigationBars())
            }
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
        val uri: Uri? = requireActivity().intent.data
        if (uri != null && uri.toString().startsWith("myteam11deeplink")) {
            val action = uri.getQueryParameter("action")
            Log.i("chromium", "get the deeplink $action")
            if (action == "rummyreload") destroyAndRelaunchWebView()
        }

    }

    var lastOrientation = "PORTRAIT"

    inner class JsObject {
        @JavascriptInterface
        fun receiveMessage(data: String) {
            if (data.equals("logout",true)){
                exitDialog("Session Expired! Please Login Again.")
            }else{
                Log.e("testcode", "ori $data ${mBinding.hashCode()}")

                Log.i("JsObject", "new postMessage data=$data")
                if (data == "portrait") changeOrientation(PORTRAIT) else
                    if (data == "landscape")
                        changeOrientation(LANDSCAPE)
            }
        }
    }


    var mDialogTimesUp: Dialog?=null


    override fun showDialog(msg: String, isExit: Boolean) {
        exitDialog(msg, if (isExit)"Exit" else "Okay",isExit = true)
    }

    fun exitDialog(msg:String="",exitMSg:String="Okay",isExit: Boolean=false) {
        if (TextUtils.isEmpty(msg))return
        activity?.let {
            if((it as? RummyMainActivity)?.mCurrentFragment !is FragmentRummyWebViewer)
                return
            val alertModel= AlertdialogModel(
                getString(R.string.app_name_rummy),
                msg,
                positiveText = exitMSg,
                onPositiveClick = {
                    if (isExit){
                        requireActivity().finishAffinity()
                    }else{
                        mDialogTimesUp?.dismiss()
                        mViewModel.apply {
                            analyticsHelper.fireEvent(AnalyticsKey.Names.LogOut)
                            prefs.apply {
                                userName = ""
                                userTeamName = ""
                                avtarId = -1
                                sportSelected = 1
                                introductionCompleted=false
                                loginCompleted = false
                                loginResponse = gson.toJson(LoginResponse())
                            }
                        }
                        startActivity(Intent(requireActivity(),RummyNewLoginActivity::class.java))
                        requireActivity().finishAffinity()
                    }
                },
                imgRes = R.drawable.ic_cross_uncheck,
                isDefaultImgPositive = ALERT_DIALOG_NEGETIVE
            )
            mDialogTimesUp = MyDialog(it).getAlertDialog(alertModel)
            mDialogTimesUp?.show()
        }
    }

    fun performBack():Boolean{
        sendDataToWebView("back")
        return true
    }
    override fun goBack() {
    }

    override fun handleError(throwable: Throwable?) {
    }

    override fun showError(message: String?) {
    }

    override fun showError(message: Int?) {
    }

    override fun showMessage(message: String?) {
    }

    override fun logoutUser() {
    }

    override fun getStringResource(resourseId: Int): String {
        return getString(resourseId)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("InitRummy", "onDestroy $this")//bb46ebb

    }
}

interface RummyNavigator {
    fun launchRummy(gameModel: GamesResponseModel.GamesModel)
    fun showDialog(msg:String,isExit:Boolean)
}


