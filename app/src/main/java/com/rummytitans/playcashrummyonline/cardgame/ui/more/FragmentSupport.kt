package com.rummytitans.playcashrummyonline.cardgame.ui.more

import com.rummytitans.playcashrummyonline.cardgame.BuildConfig
import com.rummytitans.playcashrummyonline.cardgame.R
import com.rummytitans.playcashrummyonline.cardgame.analytics.AnalyticsKey
import com.rummytitans.playcashrummyonline.cardgame.databinding.FragmentHelpDeskRummyBinding
import com.rummytitans.playcashrummyonline.cardgame.ui.WebChatActivity
import com.rummytitans.playcashrummyonline.cardgame.ui.WebViewActivity
import com.rummytitans.playcashrummyonline.cardgame.ui.base.BaseFragment
import com.rummytitans.playcashrummyonline.cardgame.ui.base.BaseNavigator
import com.rummytitans.playcashrummyonline.cardgame.ui.home.MainNavigationFragment
import com.rummytitans.playcashrummyonline.cardgame.ui.more.module.SupportViewModel
import com.rummytitans.playcashrummyonline.cardgame.ui.newlogin.RummyNewLoginActivity
import com.rummytitans.playcashrummyonline.cardgame.utils.*
import com.rummytitans.playcashrummyonline.cardgame.widget.MyDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import javax.inject.Inject

class FragmentSupport : BaseFragment(), MainNavigationFragment, SupportClick,
    BaseNavigator {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    lateinit var binding: FragmentHelpDeskRummyBinding
    lateinit var viewModel: SupportViewModel
    var array = ArrayList<SupportModel>()
    var comeFrom = ""
    val darkStatus:Boolean by lazy {
        arguments?.getBoolean(MyConstants.INTENT_PASS_DARK_STATUS,false)?:false
    }

    enum class FollowType {
        FACEBOOK, TWITTER, YOUTUBE, INSTAGRAM, LINKEDIN
    }

    companion object {
        fun newInstance(from: String, requstFromHomeDialogs: String = ""): FragmentSupport {
            val bundle = Bundle()
            bundle.putString(MyConstants.INTENT_PASS_FROM, from)
            bundle.putBoolean(MyConstants.INTENT_PASS_DARK_STATUS, from.equals("Address Verify",true))
            if (requstFromHomeDialogs != "")
                bundle.putString(MyConstants.INTENT_PASS_COMING_FROM, MyConstants.HOME_DIALOGS)
            val f = FragmentSupport()
            f.arguments = bundle
            return f
        }
    }

    override fun onResume() {
        super.onResume()
        kotlin.runCatching {
            activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        setLanguage()
        setTheme(inflater)
        viewModel = ViewModelProvider(
            this
        ).get(SupportViewModel::class.java)
        binding = FragmentHelpDeskRummyBinding.inflate(
            localInflater ?: localInflater ?: inflater, container, false
        ).apply {
            lifecycleOwner = this@FragmentSupport
            viewmodel = this@FragmentSupport.viewModel
            viewmodel?.navigatorAct = this@FragmentSupport
        }
        activity?.let{ viewModel.myParentDialog = MyDialog(it) }
        viewModel.navigator = this@FragmentSupport
        viewModel.getHelpDesk()
        binding.executePendingBindings()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setOnClickListener(null)
        comeFrom = arguments?.getString(MyConstants.INTENT_PASS_FROM) ?: ""
        arguments?.getString(MyConstants.INTENT_PASS_COMING_FROM)?.let {
            if (it.equals(MyConstants.HOME_DIALOGS)) onChatClick()
        }
        viewModel.supportResponse.observe(viewLifecycleOwner, { array.clear() })

        viewModel.analyticsHelper.fireEvent(
            AnalyticsKey.Names.ScreenLoadDone, bundleOf(
                AnalyticsKey.Keys.Screen to AnalyticsKey.Screens.HelpDesk
            )
        )
    }

    override fun onChatClick() {
        if (!ClickEvent.check(ClickEvent.BUTTON_CLICK)) return

        viewModel.analyticsHelper.fireEvent(
            AnalyticsKey.Names.ButtonClick, bundleOf(
                AnalyticsKey.Keys.ButtonName to AnalyticsKey.Values.ChatWithUs,
                AnalyticsKey.Keys.Screen to AnalyticsKey.Screens.HelpDesk
            )
        )
        startActivity(Intent(activity, WebChatActivity::class.java)
            .putExtra(MyConstants.INTENT_PASS_FROM,comeFrom)
            .putExtra(MyConstants.INTENT_PASS_DARK_STATUS,darkStatus)
        )
    }

    override fun getStringResource(resourseId: Int) = getString(resourseId)

    override fun onFAQClick() {
        if (!ClickEvent.check(ClickEvent.BUTTON_CLICK)) return
        viewModel.analyticsHelper.fireEvent(
            AnalyticsKey.Names.ButtonClick, bundleOf(
                AnalyticsKey.Keys.ButtonName to AnalyticsKey.Values.FAQ,
                AnalyticsKey.Keys.Screen to AnalyticsKey.Screens.HelpDesk
            )
        )
        startActivity(
            Intent(requireActivity(), WebViewActivity::class.java)
                .putExtra(MyConstants.INTENT_PASS_WEB_URL, WebViewUrls.FAQ)
                .putExtra(MyConstants.INTENT_PASS_WEB_TITLE, getString(R.string.faq_s))
                .putExtra(MyConstants.INTENT_PASS_FROM,comeFrom)
                .putExtra(MyConstants.INTENT_PASS_DARK_STATUS,darkStatus)

        )
    }

    override fun onCallClick() {
        if (!ClickEvent.check(ClickEvent.BUTTON_CLICK)) return
        val supportPhone = viewModel.supportResponse.value?.Mobile ?:"911414579900"
        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse("tel:+$supportPhone")
        startActivity(intent)
    }

    override fun onEmailClick() {
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


        viewModel.analyticsHelper.fireEvent(
            AnalyticsKey.Names.ButtonClick, bundleOf(
                AnalyticsKey.Keys.ButtonName to AnalyticsKey.Values.EmailUs,
                AnalyticsKey.Keys.Screen to AnalyticsKey.Screens.HelpDesk
            )
        )

        val supportEmail = viewModel.supportResponse.value?.Email ?: getString(R.string.supportEmail)
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.type = "text/html"
        intent.putExtra(Intent.EXTRA_EMAIL, supportEmail)
        intent.putExtra(Intent.EXTRA_SUBJECT, "Support - Android App")
        intent.putExtra(
            Intent.EXTRA_TEXT,
            deviceDetails + "\nEmail: ${viewModel.loginResponse.Email}\nMobile: ${viewModel.loginResponse.Mobile}"
        )
        intent.data = Uri.parse("mailto:$supportEmail")
        kotlin.runCatching {
            startActivity(Intent.createChooser(intent, "Send Email"))
        }.onFailure { print(it.message) }
    }


    override fun goBack() {
        activity?.onBackPressed()
    }

    override fun handleError(throwable: Throwable?) {
        println(throwable?.message.toString())
        throwable?.message?.let { showErrorMessageView(it) }
    }

    override fun showError(message: String?) {
        message?.let { showErrorMessageView(it) }
    }

    override fun showError(message: Int?) {
        message?.let { showMessageView(getString(it)) }
    }

    override fun showMessage(message: String?) {
        message?.let { showMessageView(it) }
    }

    override fun logoutUser() {
        showError(R.string.err_session_expired)
        activity?.finishAffinity()
        startActivity(Intent(activity, RummyNewLoginActivity::class.java))
    }

    fun fireEvent(s: String) {
        viewModel.analyticsHelper.fireEvent(
            AnalyticsKey.Names.ButtonClick, bundleOf(
                AnalyticsKey.Keys.ButtonName to s,
                AnalyticsKey.Keys.Screen to AnalyticsKey.Screens.HelpDesk
            )
        )
    }

    override fun onFollowClick(type: FollowType) {
        when (type) {
            FollowType.FACEBOOK -> {
                try {
                    val facebookUrl = "fb://page/105975840982646"
                    val facebookIntent = Intent(Intent.ACTION_VIEW)
                    facebookIntent.data = Uri.parse(facebookUrl)
                    startActivity(facebookIntent)
                } catch (e: Exception) {
                    val url = WebViewUrls.FB
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
                fireEvent(AnalyticsKey.Values.Facebook)
            }
            FollowType.TWITTER -> {
                val urlString = WebViewUrls.TWITTER
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlString))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                fireEvent(AnalyticsKey.Values.Twitter)
            }
            FollowType.YOUTUBE -> {
                val urlString = WebViewUrls.YOUTUBE
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlString))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                fireEvent(AnalyticsKey.Values.YouTube)
            }
            FollowType.INSTAGRAM -> {
                val urlString = WebViewUrls.INSTA
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlString))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                fireEvent(AnalyticsKey.Values.Instagram)
            }
            FollowType.LINKEDIN -> {
                val urlString = WebViewUrls.INSTA
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlString))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                fireEvent(AnalyticsKey.Values.LinkedIn)
            }
        }
    }
}

interface SupportClick {
    fun onChatClick()
    fun onCallClick()
    fun onEmailClick()
    fun onFAQClick()
    fun onFollowClick(type: FragmentSupport.FollowType)
}