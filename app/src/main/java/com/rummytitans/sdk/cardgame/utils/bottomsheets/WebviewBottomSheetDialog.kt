package com.rummytitans.sdk.cardgame.utils.bottomsheets

import com.rummytitans.sdk.cardgame.databinding.BottomsheetDialogWebviewRummyBinding
import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient


class WebViewBottomSheetDialog(context: Context,
                               val color: String,
                               val webUrl:String="",
                               val title:String="") : BaseBottomSheetDialog(context){
    var binding: BottomsheetDialogWebviewRummyBinding =
        BottomsheetDialogWebviewRummyBinding.inflate(LayoutInflater.from(context),null,false)

    init {
        binding.color = color
        binding.title = title
        setContentView(binding.root)
        renderView()
    }

    private fun renderView() {
        binding.imgCross.setOnClickListener {
            dismiss()
        }
        binding.webview.apply {
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
