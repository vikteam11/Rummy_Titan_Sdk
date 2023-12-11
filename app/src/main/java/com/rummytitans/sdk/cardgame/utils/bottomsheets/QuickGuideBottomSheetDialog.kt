package com.rummytitans.sdk.cardgame.utils.bottomsheets

import com.rummytitans.sdk.cardgame.databinding.BottomsheetDialogQuickGuideRummyBinding
import com.rummytitans.sdk.cardgame.utils.WebViewUrls
import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient


class QuickGuideBottomSheetDialog(context: Context,
                                  val color: String,
                                  val language:String="en") : BaseBottomSheetDialog(context){
    lateinit var binding: BottomsheetDialogQuickGuideRummyBinding

    init {
        binding = BottomsheetDialogQuickGuideRummyBinding.inflate(LayoutInflater.from(context),null,false)
        binding.color = color
        setContentView(binding.root)

        renderView()
    }

    private fun renderView() {
        binding.imgCross.setOnClickListener {
            dismiss()
        }
        binding.webview.apply {
            val webUrl = WebViewUrls.AppDefaultURL + "/web-quick-guide.html"
            webViewClient = WebViewClient()
            settings.apply {
                loadsImagesAutomatically = true
                javaScriptEnabled = true
            }
            scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
            loadUrl(webUrl ?: "")
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                    view?.loadUrl(url?:"")
                    return false
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
                }
            }
        }
    }
}
