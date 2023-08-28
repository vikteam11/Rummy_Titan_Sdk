package com.rummytitans.playcashrummyonline.cardgame.ui.verifications


import com.rummytitans.playcashrummyonline.cardgame.BuildConfig
import com.rummytitans.playcashrummyonline.cardgame.R
import com.rummytitans.playcashrummyonline.cardgame.databinding.ActivityRummyAutoAddressVerifyWebviewsBinding
import com.rummytitans.playcashrummyonline.cardgame.ui.base.BaseActivity
import com.rummytitans.playcashrummyonline.cardgame.ui.deeplink.DeepLinkActivity
import com.rummytitans.playcashrummyonline.cardgame.ui.verifications.viewmodels.AddressVerificationViewModel
import com.rummytitans.playcashrummyonline.cardgame.utils.*
import com.rummytitans.playcashrummyonline.cardgame.utils.alertDialog.AlertdialogModel
import com.rummytitans.playcashrummyonline.cardgame.utils.bottomsheets.BottomSheetAlertDialog
import com.rummytitans.playcashrummyonline.cardgame.widget.MyDialog
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import javax.inject.Inject

class WebAutoAddressVerifyActivity : BaseActivity(), AddressVerificationNavigator {


    lateinit var binding: ActivityRummyAutoAddressVerifyWebviewsBinding
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var viewModel: AddressVerificationViewModel

    private var currentUrl : String? = ""
    private val verificationFailedUrl by lazy { "kyc-failed" }
    private val verificationSuccessUrl by lazy { "kyc-success" }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this.viewModelStore, viewModelFactory)
            .get(AddressVerificationViewModel::class.java)
        viewModel.navigatorAct = this
        viewModel.navigator = this
        binding = DataBindingUtil.setContentView(this, R.layout.activity_rummy_auto_address_verify_webviews)
        binding.viewmodel =viewModel
        intent.getBooleanExtra(MyConstants.INTENT_PASS_SHOW_TOOLBAR,false).let {
            if (it)
                binding.constraintLayout.visibility=View.VISIBLE
        }
        intent.getBooleanExtra(MyConstants.INTENT_PASS_SHOW_CROSS,false).let {
            if (it){
                binding.icBack.visibility=View.GONE
                binding.icCross.visibility=View.VISIBLE
                binding.constraintLayout.visibility=View.VISIBLE
            }
        }
        viewModel.myDialog= MyDialog(this)
        viewModel.url = intent.getStringExtra(MyConstants.INTENT_PASS_WEB_URL)?:""
        binding.apply {
            val webTitle = intent.getStringExtra(MyConstants.INTENT_PASS_WEB_TITLE)
            txtTitle.text = webTitle
            icBack.setOnClickListener { onBackPressed() }
            icCross.setOnClickListener {
                webview.destroy()
                finish()
            }
        }
        initWebView()

        changeStatusBarColor(binding.root, R.color.secondary)
        val buttonDrawable = DrawableCompat.wrap(binding.constraintLayout.background)
        DrawableCompat.setTint(buttonDrawable,getColorInt(R.color.secondary) )
        binding.constraintLayout.background = buttonDrawable
    }

    private fun initWebView(reloadUrl:Boolean=false){
        binding.webview.let {
            if (reloadUrl) {
                it.loadUrl(viewModel.url)
                return@let
            }
             it.webViewClient = WebViewClient()
            it.settings.apply {
                loadsImagesAutomatically = true
                javaScriptEnabled = true
                domStorageEnabled = true
                databaseEnabled = true
            }
            it.scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
            it.loadUrl(viewModel.url ?: "")
            it.webViewClient = object : WebViewClient() {

                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                    if (!viewModel.connectionDetector.isConnected){
                        viewModel.myDialog?.retryInternetDialog { initWebView(true) }
                        return true
                    }
                    url?.apply {
                           currentUrl=substring(lastIndexOf("/")+1,length)
                        }
                    return when {
                        url.toString().contains("sportstiger.app.link") -> {
                            sendToExternalBrowser(this@WebAutoAddressVerifyActivity, url.toString())
                            true
                        }
                        url.toString().contains("mailto:") -> {
                            sendEmail()
                            true
                        }
                        url.toString().contains("tel:") -> {
                            val intent = Intent(Intent.ACTION_DIAL)
                            intent.data = url?.toUri()
                            startActivity(intent)
                            true
                        }
                        url.toString().contains("external:") -> {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                            true
                        }
                        url.toString().contains("myteam11deeplink") -> {
                            val status = Uri.parse(url).getQueryParameter("addressVerify")
                            if(status.equals("Failed",true)){
                                binding.webview.destroy()
                                finish()
                            }else{
                                setResult(RESULT_OK)
                                binding.webview.destroy()
                                finish()
                            }
                            true
                        }
                        url.toString().contains(MyConstants.AppDeeplink) -> {
                            openDeeplink(url.toString())
                            true
                        }
                        url.toString().contains(MyConstants.AppDeeplink) -> {
                            openDeeplink(url.toString())
                            true
                        }

                        else -> {
                            // view?.loadUrl(url ?: "")
                            false
                        }
                    }
                }

                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    kotlin.runCatching {
                        binding.loading.visibility = View.VISIBLE
                    }
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    kotlin.runCatching {
                        binding.loading.visibility = View.GONE
                    }
                    if (url.isNullOrEmpty())return
                    if (url.contains("Exited")){

                    }else if(url.contains("Verified")){

                    }
                }
            }
        }
    }

    fun sendEmail() {
        if (!ClickEvent.check(ClickEvent.BUTTON_CLICK)) return
        val deviceDetails =
            "" + "\n\n\n" + "OS Version : " + System.getProperty("os.version") +
                    "\n Device : " + Build.DEVICE +
                    "\n Device Model : " + Build.MODEL +
                    "\n Product : " + Build.PRODUCT +
                    "\n App Version : " + BuildConfig.VERSION_NAME +
                    "\n Version Code : " + BuildConfig.VERSION_CODE +
                    "\n Build Type : " + BuildConfig.BUILD_TYPE
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.type = "text/html"
        intent.putExtra(Intent.EXTRA_EMAIL, getString(R.string.supportEmail))
        intent.putExtra(Intent.EXTRA_SUBJECT, "Support - Android App")
        intent.putExtra(Intent.EXTRA_TEXT, deviceDetails)
        intent.data = Uri.parse("mailto:${getString(R.string.supportEmail)}")
        try {
            startActivity(Intent.createChooser(intent, "Send Email"))
        } catch (e: Exception) {
        }
    }

    fun handleDeepLink(url: String) {
        if (url.contains("?")) {
            val data = url.split("?")
            if (data.isNotEmpty()) {
                startActivity(
                    Intent(this@WebAutoAddressVerifyActivity, DeepLinkActivity::class.java)
                        .putExtra("data", data[1])
                )
            }
        }
    }


    override fun onBackPressed() {
        BottomSheetAlertDialog(
            this, AlertdialogModel(
                getString(R.string.app_name_rummy),
                getString(R.string.back_alert),
                getString(R.string.no),
                getString(R.string.yes),
                onPositiveClick = {
                   performBack()
                }
            ),
            viewModel.selectedColor.get()?:""
        ).show()
    }

    private fun performBack() {
        if(currentUrl.contentEquals(verificationFailedUrl,true)){
            binding.webview.destroy()
            finish()
        }else if(currentUrl.contentEquals(verificationSuccessUrl,true)){
            setResult(RESULT_OK)
            binding.webview.destroy()
            finish()
        }else if (binding.webview.canGoBack())
            binding.webview.goBack()
        else {
            binding.webview.destroy()
            super.onBackPressed()
        }
    }
}
