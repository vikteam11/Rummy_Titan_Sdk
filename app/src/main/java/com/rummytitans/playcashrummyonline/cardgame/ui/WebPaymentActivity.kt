package com.rummytitans.playcashrummyonline.cardgame.ui

import com.rummytitans.playcashrummyonline.cardgame.R
import com.rummytitans.playcashrummyonline.cardgame.data.SharedPreferenceStorage
import com.rummytitans.playcashrummyonline.cardgame.databinding.FragmentWebviewsRummyBinding
import com.rummytitans.playcashrummyonline.cardgame.utils.MyConstants
import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.databinding.DataBindingUtil
import com.rummytitans.playcashrummyonline.cardgame.utils.changeStatusBarColor
import kotlinx.android.synthetic.main.fragment_webviews_rummy.*

class WebPaymentActivity : AppCompatActivity() {
    lateinit var binding: FragmentWebviewsRummyBinding
    val successRegex = "https://web.myteam11.com/Home/AndroidSucess".toRegex()
    val failedRegex = "https://web.myteam11.com/Home/AndroidFailed".toRegex()

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        val prefs = SharedPreferenceStorage(applicationContext)
        setTheme(R.style.RummyAppTheme)
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.fragment_webviews_rummy)
        intent.getBooleanExtra(MyConstants.INTENT_PASS_SHOW_TOOLBAR,true).let {
            binding.toolBar.visibility=if (it) {
                changeStatusBarColor(window.decorView,R.color.rummy_statusBarColor)
                View.VISIBLE
            }else {
                window?.statusBarColor =
                    ContextCompat.getColor(this, R.color.white)
                window?.let {
                    WindowCompat.getInsetsController(it, binding.root)?.isAppearanceLightStatusBars =
                        true
                }
                View.GONE
            }
        }
        val webUrl = intent.getStringExtra(MyConstants.INTENT_PASS_WEB_URL)
        val webTitle = intent.getStringExtra(MyConstants.INTENT_PASS_WEB_TITLE)

        binding.icBack.setOnClickListener { onBackPressed() }

        binding.txtTitle.text = webTitle

        binding.webview.apply {
            settings.loadsImagesAutomatically = true
            settings.javaScriptEnabled = true
            scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
            loadUrl(webUrl ?: "")

            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, url: String): Boolean {
                    if (url.matches(successRegex)) {
                        setResult(Activity.RESULT_OK)
                        finish()
                    } else if (url.matches(failedRegex)) {
                        setResult(Activity.RESULT_CANCELED)
                        finish()
                    }
                    return false
                }

                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    if (loading != null)
                        loading.visibility = View.VISIBLE
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    if (loading != null)
                        loading.visibility = View.GONE
                }
            }
        }
    }

    override fun onBackPressed() {
        if (intent.getBooleanExtra(MyConstants.INTENT_PASS_FROM_PAYMENTS,true))return

        if (binding.webview.canGoBack()) binding.webview.goBack()
        else {
            setResult(Activity.RESULT_CANCELED)
            super.onBackPressed()
        }
    }
}
