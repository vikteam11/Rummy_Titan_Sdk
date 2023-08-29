package com.rummytitans.playcashrummyonline.cardgame.ui

import com.rummytitans.playcashrummyonline.cardgame.BuildConfig
import com.rummytitans.playcashrummyonline.cardgame.R
import com.rummytitans.playcashrummyonline.cardgame.data.SharedPreferenceStorage
import com.rummytitans.playcashrummyonline.cardgame.databinding.FragmentWebviewsRummyBinding
import com.rummytitans.playcashrummyonline.cardgame.ui.deeplink.DeepLinkActivityRummy
import com.rummytitans.playcashrummyonline.cardgame.utils.*
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import com.rummytitans.playcashrummyonline.cardgame.RummyTitanSDK
import kotlinx.android.synthetic.main.fragment_webviews_rummy.*

class WebViewActivity : AppCompatActivity() {


    lateinit var binding: FragmentWebviewsRummyBinding


    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        val prefs = SharedPreferenceStorage(this)
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.fragment_webviews_rummy)
        intent.getBooleanExtra(MyConstants.INTENT_PASS_SHOW_TOOLBAR,false).let {
            if (it)
                binding.toolBar.visibility=View.VISIBLE
        }
        intent.getBooleanExtra(MyConstants.INTENT_PASS_SHOW_CROSS,false).let {
            if (it){
                binding.icBack.visibility=View.GONE
                binding.icCross.visibility=View.VISIBLE
                binding.toolBar.visibility=View.VISIBLE
            }
        }
        binding.apply {
            val webTitle = intent.getStringExtra(MyConstants.INTENT_PASS_WEB_TITLE)
            txtTitle.text = webTitle
            icBack.setOnClickListener { onBackPressed() }
            icCross.setOnClickListener {
                webview.destroy()
                finish()
            }
            webview.apply {
                setBackgroundResource(0)
                setBackgroundColor(0)
                val webUrl = intent.getStringExtra(MyConstants.INTENT_PASS_WEB_URL)
                webViewClient = WebViewClient()
                settings.loadsImagesAutomatically = true
                settings.javaScriptEnabled = true
                scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
                loadUrl(webUrl ?: "")
                webViewClient = object : WebViewClient() {

                    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                        return if (url.toString().contains("sportstiger.app.link")) {
                            sendToExternalBrowser(this@WebViewActivity, url.toString())
                            true
                        }else if (url.toString().contains("mailto:")) {
                            sendEmail()
                            true
                        } else if (url.toString().contains("tel:")) {
                            val intent = Intent(Intent.ACTION_DIAL)
                            intent.data = url?.toUri()
                            startActivity(intent)
                            true
                        } else if (url.toString().contains("external:")) {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                            true
                        } else if (url.toString().isMyTeamDeeplink()) {
                            RummyTitanSDK.rummyCallback?.openDeeplink(url)
                            true
                        } else if (url.toString().contains("${MyConstants.AppDeeplink}:")) {
                            openDeeplink(url.toString())
                            true
                        } else {
                            // view?.loadUrl(url ?: "")
                            false
                        }
                    }

                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        super.onPageStarted(view, url, favicon)
                        kotlin.runCatching {
                            loading.visibility = View.VISIBLE
                        }
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                       kotlin.runCatching {
                           loading.visibility = View.GONE
                       }
                    }
                }
                setOnLongClickListener { true }
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
                    Intent(this@WebViewActivity, DeepLinkActivityRummy::class.java)
                        .putExtra("data", data[1])
                )
            }
        }
    }

    override fun onBackPressed() {
        if (binding.webview.canGoBack()) binding.webview.goBack() else super.onBackPressed()
    }
}
