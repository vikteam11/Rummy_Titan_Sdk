package com.rummytitans.playcashrummyonline.cardgame.ui.more

import com.rummytitans.playcashrummyonline.cardgame.BuildConfig
import com.rummytitans.playcashrummyonline.cardgame.MainApplication.analyticsHelper
import com.rummytitans.playcashrummyonline.cardgame.R
import com.rummytitans.playcashrummyonline.cardgame.analytics.AnalyticsKey
import com.rummytitans.playcashrummyonline.cardgame.data.SharedPreferenceStorage
import com.rummytitans.playcashrummyonline.cardgame.databinding.FragmentMoreRummyBinding
import com.rummytitans.playcashrummyonline.cardgame.ui.base.BaseFragment
import com.rummytitans.playcashrummyonline.cardgame.ui.base.BaseNavigator
import com.rummytitans.playcashrummyonline.cardgame.ui.common.CommonFragmentActivity
import com.rummytitans.playcashrummyonline.cardgame.ui.home.MainNavigationFragment
import com.rummytitans.playcashrummyonline.cardgame.utils.MyConstants
import com.rummytitans.playcashrummyonline.cardgame.utils.WebViewUrls
import com.rummytitans.playcashrummyonline.cardgame.utils.sendToCloseAbleInternalBrowser
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import com.rummytitans.playcashrummyonline.cardgame.ui.RummyMainActivity
import com.rummytitans.playcashrummyonline.cardgame.ui.WebChatActivity
import com.rummytitans.playcashrummyonline.cardgame.ui.newlogin.RummyNewLoginActivity
import javax.inject.Inject


class FragmentMore : BaseFragment(), MainNavigationFragment, MoreNavigator,
    BaseNavigator {

    lateinit var binding: FragmentMoreRummyBinding

    @Inject
    lateinit var prefs: SharedPreferenceStorage
    lateinit var viewModel: MoreViewModel

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        setLanguage()
        setTheme(inflater)
        viewModel =
            ViewModelProvider(this).get(MoreViewModel::class.java)
        binding = FragmentMoreRummyBinding.inflate((localInflater?:inflater), container, false).apply {
            lifecycleOwner = this@FragmentMore
            viewmodel = this@FragmentMore.viewModel
            viewModel.navigator = this@FragmentMore
            viewModel.navigatorAct = this@FragmentMore
        }
        binding.executePendingBindings()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.txtMyteam11Blog.setOnClickListener {
            val urlString = WebViewUrls.MYTEAM11_BLOG
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlString))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }

        val currentVersionCode=BuildConfig.VERSION_NAME
        with(viewModel.versionResponse){
            if(IsAppUpdate){
                kotlin.runCatching {
                    "New Version : $newAppVersionCode".also { binding.tvUpdateDesc.text = it }
                }.onFailure {
                    binding.tvUpdateDesc.text= getString(R.string.new_version_available)
                }
            }else{
                binding.tvUpdateDesc.text= getString(R.string.app_up_to_date)
            }
        }

        (activity?.getString(R.string.version)+" : $currentVersionCode").also { binding.tvUpdateTitle.text = it }

        val isNotificationApiCalled=(activity as? RummyMainActivity)?.isNotificationApiCalled?:false
        viewModel.let {frgViewModel->
            if (!isNotificationApiCalled){
                frgViewModel.getNotificationKey()
            }else{
                frgViewModel.newNotificationAvailable.set(frgViewModel.prefs.latestNotificationAvailable)
            }
        }
        viewModel.analyticsHelper.fireEvent(
            AnalyticsKey.Names.ScreenLoadDone, bundleOf(
                AnalyticsKey.Keys.Screen to AnalyticsKey.Values.More,
            )
        )
    }


    override fun logoutUser() {
        showError(R.string.err_session_expired)
        activity?.finishAffinity()
        startActivity(Intent(activity, RummyNewLoginActivity::class.java))
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

    override fun getStringResource(resourseId: Int) = getString(resourseId)


    override fun onWebClick(url: String, titleId: Int) {
        chkype(titleId)
        sendToCloseAbleInternalBrowser(requireActivity(), url,activity?.getString(titleId)?:"")
    }

    override fun onItemClick(position: Int) {

        var buttonName = ""

        when (position) {
            1 -> {
                buttonName = AnalyticsKey.Values.ReferAndEarn
                startActivity(
                    Intent(requireActivity(), CommonFragmentActivity::class.java)
                        .putExtra(MyConstants.INTENT_PASS_COMMON_TYPE, "refer")
                )
            }
            2 -> {
                buttonName = AnalyticsKey.Values.Feedback
                startActivity(
                    Intent(requireActivity(), CommonFragmentActivity::class.java)
                        .putExtra(MyConstants.INTENT_PASS_COMMON_TYPE, "feedback")
                )
            }

            3 -> {
                buttonName = AnalyticsKey.Values.Support
                startActivity(
                    Intent(requireActivity(), CommonFragmentActivity::class.java)
                        .putExtra(MyConstants.INTENT_PASS_COMMON_TYPE, "support")
                        .putExtra("FROM", "Home")
                )
            }


            4 -> {
                buttonName = AnalyticsKey.Values.LiveChat
                startActivity(Intent(activity, WebChatActivity::class.java)
                    .putExtra(MyConstants.INTENT_PASS_FROM,"more")
                )
            }

            5 -> {
                buttonName = AnalyticsKey.Values.UpdateApp
                (activity as? RummyMainActivity )?.onAppUpdate()
            }
        }

        analyticsHelper.fireEvent(
            AnalyticsKey.Names.ButtonClick, bundleOf(
                AnalyticsKey.Keys.ButtonName to buttonName,
                AnalyticsKey.Keys.Screen to AnalyticsKey.Screens.More,
            )
        )
        analyticsHelper.addTrigger(AnalyticsKey.Screens.More,buttonName)
    }


    fun chkype(int: Int) {
        val list = listOf(
            R.string.about_us, R.string.how_to_play, R.string.tutorial_videos,
            R.string.faq_s, R.string.fantasy_point_system
        )
        if (!list.contains(int)) return

        val eventName = when (int) {
            R.string.about_us -> AnalyticsKey.Values.AboutUs
            R.string.how_to_play -> AnalyticsKey.Values.HowToPlay
            R.string.tutorial_videos -> AnalyticsKey.Values.TutorialVideos
            R.string.faq_s -> AnalyticsKey.Values.FAQ
            R.string.fantasy_point_system -> AnalyticsKey.Values.FantasyPointSystem
            else -> ""
        }
        viewModel.analyticsHelper.fireEvent(
            AnalyticsKey.Names.ButtonClick, bundleOf(
                AnalyticsKey.Keys.ButtonName to eventName,
                AnalyticsKey.Keys.Screen to AnalyticsKey.Values.More
            )
        )

    }

    override fun notificationApiCalled() {
        (activity as? RummyMainActivity)?.isNotificationApiCalled=true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
         if (requestCode==1221){
             viewModel.getNotificationKey()
         }
    }


}