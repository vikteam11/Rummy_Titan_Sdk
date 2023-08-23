package com.rummytitans.playcashrummyonline.cardgame.ui

import com.rummytitans.playcashrummyonline.cardgame.BuildConfig
import com.rummytitans.playcashrummyonline.cardgame.R
import com.rummytitans.playcashrummyonline.cardgame.data.SharedPreferenceStorage
import com.rummytitans.playcashrummyonline.cardgame.databinding.ActivityRummyWebchatBinding
import com.rummytitans.playcashrummyonline.cardgame.models.LoginResponse
import com.rummytitans.playcashrummyonline.cardgame.utils.*
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.gson.Gson

class WebChatActivity : AppCompatActivity() {
    lateinit var binding:ActivityRummyWebchatBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        val prefs = SharedPreferenceStorage(this)
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this,R.layout.activity_rummy_webchat)

        binding.icBack.setOnClickListener { onBackPressed() }

        val loginResponse = SharedPreferenceStorage(this).let {
            Gson().fromJson(it.loginResponse, LoginResponse::class.java)
        }

        val params = try {
            val name = loginResponse.Name
            val mobile = loginResponse.Mobile
            val expire = loginResponse.ExpireToken
            val email = loginResponse.Email
            if (!TextUtils.isEmpty(expire)) "name=$name&mobile=$mobile&code=$expire&email=$email"
            else ""
        } catch (e: Exception) {
            ""
        }

        binding.webview.scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
        binding.webview.settings.javaScriptEnabled = true
        binding.webview.settings.domStorageEnabled = true
        binding.webview.settings.loadWithOverviewMode = true
        binding.webview.settings.loadsImagesAutomatically = true
        binding.webview.settings.cacheMode = WebSettings.LOAD_DEFAULT


        binding.webview.loadUrl("https://www.myteam11.com/haptik.html?$params")

        binding.webview.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                return if (url.toString().contains("mailto:")) {
                    sendEmail()
                    true
                } else if (url.toString().contains("myteam11deeplink:")) {
                    openDeeplink(url.toString())
                    true
                } else {
                    view?.loadUrl(url ?: "")
                    false
                }
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                binding.loading.visibility = View.GONE
            }
        }
    }


    fun sendEmail() {
        if (!ClickEvent.check(ClickEvent.BUTTON_CLICK)) return
        val deviceDetails =
            "" + "\n\n\n" + "OS Version : " + System.getProperty("os.version") +
                    "\n OS Build Version : " + android.os.Build.VERSION.SDK +
                    "\n Device : " + android.os.Build.DEVICE +
                    "\n Device Model : " + android.os.Build.MODEL +
                    "\n Product : " + android.os.Build.PRODUCT +
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

    override fun onBackPressed() {
        if (binding.webview.canGoBack()) binding.webview.goBack()
        else super.onBackPressed()
    }
}