package com.rummytitans.playcashrummyonline.cardgame.ui.more

import com.rummytitans.playcashrummyonline.cardgame.BuildConfig
import com.rummytitans.playcashrummyonline.cardgame.data.SharedPreferenceStorage
import com.rummytitans.playcashrummyonline.cardgame.databinding.FragmentWebviewsRummyBinding
import com.rummytitans.playcashrummyonline.cardgame.ui.base.BaseFragment
import com.rummytitans.playcashrummyonline.cardgame.ui.home.MainNavigationFragment
import com.rummytitans.playcashrummyonline.cardgame.utils.ClickEvent
import com.rummytitans.playcashrummyonline.cardgame.utils.ConnectionDetector
import com.rummytitans.playcashrummyonline.cardgame.utils.openDeeplink
import com.rummytitans.playcashrummyonline.cardgame.widget.MyDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.net.toUri
import kotlinx.android.synthetic.main.fragment_webviews_rummy.*
import javax.inject.Inject


class FragmentWebViews : BaseFragment(), MainNavigationFragment {

    lateinit var binding: FragmentWebviewsRummyBinding
    var connectionDetector: ConnectionDetector?=null
    var myDialog:MyDialog?=null
    var url = ""
    var title = ""


    @Inject
    lateinit var prefs: SharedPreferenceStorage


    companion object {
        fun newInstance(url: String?, title: String?): FragmentWebViews {
            val frag = FragmentWebViews()
            val bundle = Bundle()
            bundle.putString("url", url)
            bundle.putString("title", title)
            frag.arguments = bundle
            return frag
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setTheme(inflater)
        binding = FragmentWebviewsRummyBinding.inflate(
            localInflater ?: localInflater ?: inflater,
            container,
            false
        ).apply {
            lifecycleOwner = this@FragmentWebViews
        }
        binding.executePendingBindings()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.let {act->
            connectionDetector=ConnectionDetector(act)
            myDialog=MyDialog(act)
        }
        icBack.setOnClickListener {
            it.isEnabled = false
            activity?.onBackPressed()
        }
        url = arguments?.getString("url") ?: ""
        title = arguments?.getString("title") ?: ""
        txtTitle.text = title
        loadUrl()
    }

    fun loadUrl(){
        webview.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                return if (url.toString().contains("mailto:")) {
                    val mail = url?.replace("mailto:", "")
                    sendEmail(mail ?: "")
                    true
                } else if (url.toString().contains("tel:")) {
                    val intent = Intent(Intent.ACTION_DIAL)
                    intent.data = url?.toUri()
                    startActivity(intent)
                    true
                } else if (url.toString().contains("myteam11deeplink:")) {
                    requireContext().openDeeplink(url ?: "")
                    true
                } else {
                    view?.loadUrl(url ?: "")
                    true
                }
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
        webview.clearCache(true)
        webview.clearHistory()
        webview.settings.loadsImagesAutomatically = true
        webview.settings.javaScriptEnabled = true
        webview.clearCache(true)
        webview.clearFormData()
        webview.clearHistory()
        webview.scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
        if (connectionDetector?.isConnected==false) {
            myDialog?.noInternetDialogExit({
                loadUrl()
            },{
                activity?.finish()
            })
            return
        }
        webview.loadUrl(url)


        val regularColor = prefs.regularColor
        val safeColor = prefs.safeColor
        val selectedColor = if (prefs.onSafePlay) safeColor else regularColor


        val buttonDrawable = DrawableCompat.wrap(toolBar.background)
        DrawableCompat.setTint(buttonDrawable, Color.parseColor(selectedColor))
        toolBar.background = buttonDrawable
    }

    fun sendEmail(mail: String) {
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
        intent.putExtra(Intent.EXTRA_EMAIL, mail)
        intent.putExtra(Intent.EXTRA_SUBJECT, "Support - Android App")
        intent.putExtra(Intent.EXTRA_TEXT, deviceDetails)
        intent.data = Uri.parse("mailto:$mail")
        try {
            startActivity(Intent.createChooser(intent, "Send Email"))
        } catch (e: Exception) {
        }
    }

    override fun onBackPressed(): Boolean {
        return if (webview.canGoBack()) {
            webview.goBack()
            true
        } else
            false
    }
}