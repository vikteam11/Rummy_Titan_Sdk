package com.rummytitans.playcashrummyonline.cardgame.ui.settings

import com.rummytitans.playcashrummyonline.cardgame.R
import com.rummytitans.playcashrummyonline.cardgame.analytics.AnalyticsKey
import com.rummytitans.playcashrummyonline.cardgame.databinding.AlertLanguageChangeRummyBinding
import com.rummytitans.playcashrummyonline.cardgame.databinding.DialogAlertNotificationRummyBinding
import com.rummytitans.playcashrummyonline.cardgame.databinding.FragmentMySettingsRummyBinding
import com.rummytitans.playcashrummyonline.cardgame.ui.base.BaseFragment
import com.rummytitans.playcashrummyonline.cardgame.ui.common.CommonFragmentActivity
import com.rummytitans.playcashrummyonline.cardgame.ui.settings.adapter.ColorListAdapter
import com.rummytitans.playcashrummyonline.cardgame.ui.settings.adapter.MatchTimeTypeAdapter
import com.rummytitans.playcashrummyonline.cardgame.ui.settings.adapter.NotificationAdapter
import com.rummytitans.playcashrummyonline.cardgame.utils.SpacesItemDecoration
import com.rummytitans.playcashrummyonline.cardgame.widget.MyDialog
import android.app.Dialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.alert_language_change_rummy.*
import kotlinx.android.synthetic.main.dialog_change_language_rummy.btnContinue
import javax.inject.Inject

class SettingsFragment : BaseFragment(),
    com.rummytitans.playcashrummyonline.cardgame.ui.base.BaseNavigator, SettingFragmentNavigator,
    ColorChooseInterface {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    lateinit var binding: FragmentMySettingsRummyBinding
    lateinit var viewModel: SettingsViewModel
    var alertDialog: Dialog? = null

    companion object {
        fun newInstance() = SettingsFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setTheme(inflater)
        viewModel = ViewModelProvider(this)
            .get(SettingsViewModel::class.java)
        binding = FragmentMySettingsRummyBinding.inflate(
            localInflater ?: localInflater ?: inflater, container, false
        ).apply {
            lifecycleOwner = this@SettingsFragment
            viewmodel = this@SettingsFragment.viewModel
        }

        binding.executePendingBindings()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        changeSwitchTheme()

        viewModel.apply {
            isHindiLanguage.set(com.rummytitans.playcashrummyonline.cardgame.utils.LocaleHelper.getLanguage(context) == getString(R.string.hindi_code))
            myDialog = MyDialog(requireActivity())
            navigator = this@SettingsFragment
            fragmentNavigator = this@SettingsFragment

            // getSubscriptionsList()

            subscriptionListModel.observe(viewLifecycleOwner, Observer {
                binding.notificationList.apply {
                    adapter = NotificationAdapter(
                        it,
                        viewModel.isUserSubcribe,
                        viewModel.mSelectedTopics,
                        viewModel.prefs
                    )
                    layoutManager = LinearLayoutManager(context)
                    isNestedScrollingEnabled = true
                }
                //  showNotificationCoachMarks()
            })

            mSelectedTopics.observe(viewLifecycleOwner, Observer {
                viewModel.saveSubscriptionsList()
            })

            mThemeCode.observe(viewLifecycleOwner, Observer {
                viewModel.isThemeUpdated = true
                val code = if (viewModel.prefs.onSafePlay) it.safeColor else it.regularcode
                viewModel.prefs.selectedTheme = it.themePos
                viewModel.prefs.regularColor = it.regularcode
                viewModel.prefs.safeColor = it.safeColor
                viewModel.selectedcolor = code
                viewModel.mSelectedThemeCode.set(code)

                (binding.notificationList.adapter as? NotificationAdapter)?.notifyDataSetChanged()
                changeSwitchTheme()
                (activity as? CommonFragmentActivity)?.updateTheme(code)
            })

            binding.colorList.apply {
                adapter = ColorListAdapter(
                    viewModel.mThemeCode,
                    viewModel.prefs.selectedTheme ?: 0,
                    viewModel.prefs.onSafePlay
                )
                addItemDecoration(SpacesItemDecoration(5))
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            }

            binding.timeTypeList.apply {
                adapter = MatchTimeTypeAdapter(
                    viewModel.timeTypeText,
                    viewModel.prefs,
                    viewModel.mSelectedThemeCode
                )
                layoutManager = LinearLayoutManager(context)
            }

            binding.languageType.setOnCheckedChangeListener { compoundButton, b ->
                if (compoundButton.isPressed) {
                    viewModel.checkInternetConnection(b, 1)
                }
            }
        }
    }

    override fun onColorChoose(colorPos: Int) {
    }


    override fun onInternetConncted(state: Boolean) {
        showLanguageDialog(state)
    }

    fun showLanguageDialog(state: Boolean) {
        var dialogView: AlertLanguageChangeRummyBinding =
            DataBindingUtil.inflate(layoutInflater, R.layout.alert_language_change_rummy, null, false)
        dialogView.colorcode = viewModel.selectedcolor
        alertDialog = MyDialog(requireActivity()).getMyDialog(dialogView.root)
        alertDialog?.show()
        alertDialog?.setCancelable(false)
        alertDialog?.btnContinue?.setOnClickListener {
            viewModel.isLanguageChange = true
            var value = ""
            if (state) {
                value = "en"
                com.rummytitans.playcashrummyonline.cardgame.utils.LocaleHelper.setLocale(
                    context, getString(R.string.hindi_code), getString(R.string.hindi)
                )
                viewModel.prefs.quizLanguage = "hi"
                viewModel.saveLanguage(getString(R.string.hindi_code))
            } else {
                value = "hi"
                com.rummytitans.playcashrummyonline.cardgame.utils.LocaleHelper.setLocale(
                    context,
                    getString(R.string.english_code),
                    getString(R.string.english)
                )
                viewModel.prefs.quizLanguage = "en"
                viewModel.saveLanguage(getString(R.string.english_code))
            }

            viewModel.analyticsHelper.fireEvent(
                AnalyticsKey.Names.SelectLanguage, bundleOf(
                    AnalyticsKey.Keys.Language to if (value == "en") "English" else "Hindi",
                    AnalyticsKey.Keys.Screen to AnalyticsKey.Screens.Settings
                )
            )

        }
        alertDialog?.btnCancel?.setOnClickListener {
            binding.languageType.isChecked =
                com.rummytitans.playcashrummyonline.cardgame.utils.LocaleHelper.getLanguage(context) == getString(R.string.hindi_code)
            alertDialog?.dismiss()
        }
    }

    override fun onCancelInternetDialog(from: Int) {
        if (from == 1) {
            binding.languageType.isChecked =
                com.rummytitans.playcashrummyonline.cardgame.utils.LocaleHelper.getLanguage(context) == getString(R.string.hindi_code)
        }
    }

    override fun isNotificationsAllow(): Boolean {
        val isNotificationsAllow = isNotifcationsSatusUpdate()
        if (!isNotificationsAllow)
            showAlert()
        return isNotificationsAllow
    }

    //status updaeted from settings by user..
    private fun isNotifcationsSatusUpdate(): Boolean {
        context?.let {
            return NotificationManagerCompat.from(it).areNotificationsEnabled()
        }
        return false
    }

    fun showAlert() {
        var dialogView: DialogAlertNotificationRummyBinding? = null
        dialogView =
            DataBindingUtil.inflate(layoutInflater, R.layout.dialog_alert_notification_rummy, null, false)
        dialogView.colorcode = viewModel.selectedcolor
        val mOfferDialog = MyDialog(requireActivity()).getMyDialog(dialogView.root)
        mOfferDialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        mOfferDialog.show()

        dialogView.ok.setOnClickListener {
            mOfferDialog.dismiss()
        }
    }

    override fun onResume() {
        super.onResume()
        if (!isNotifcationsSatusUpdate()) {
            viewModel.hideNotifications()
        } else {
            if ((context as? com.rummytitans.playcashrummyonline.cardgame.MainApplication)?.isNotificationApiUpdateRequire == true) {
                viewModel.fetchTopicsAndSubscribe()
                (context as? com.rummytitans.playcashrummyonline.cardgame.MainApplication)?.isNotificationApiUpdateRequire = false
            }
        }
    }

    private fun changeSwitchTheme() {
        viewModel.prefs.selectedTheme?.let {

            val (regularTheme, safeTheme) = when (it) {
                0 -> Pair(R.color.theme1_regular, R.color.theme1_save)
                1 -> Pair(R.color.theme2_regular, R.color.theme2_save)
                2 -> Pair(R.color.theme3_regular, R.color.theme3_save)
                3 -> Pair(R.color.theme4_regular, R.color.theme4_save)
                4 -> Pair(R.color.theme5_regular, R.color.theme5_save)
                else -> Pair(R.color.theme1_regular, R.color.theme1_save)
            }

            val thumbStates = ColorStateList(
                arrayOf(
                    intArrayOf(-android.R.attr.state_enabled),
                    intArrayOf(android.R.attr.state_checked),
                    intArrayOf()
                ),
                intArrayOf(
                    Color.WHITE,
                    ContextCompat.getColor(
                        requireActivity(),
                        if (!viewModel.prefs.onSafePlay) safeTheme else regularTheme
                    ),
                    Color.GRAY
                )
            )

            binding.languageType.thumbTintList = thumbStates
            binding.languageType.thumbTintMode = PorterDuff.Mode.SRC_IN
            with(binding) {
                switch1.apply {
                    thumbTintList = thumbStates
                    thumbTintMode = PorterDuff.Mode.SRC_IN
                }
                switch2.apply {
                    thumbTintList = thumbStates
                    thumbTintMode = PorterDuff.Mode.SRC_IN
                }
                switch3.apply {
                    thumbTintList = thumbStates
                    thumbTintMode = PorterDuff.Mode.SRC_IN
                }
            }
        }
    }

    override fun goBack() {
        activity?.onBackPressed()
    }

    override fun handleError(throwable: Throwable?) {}

    override fun showMessage(message: String?) {
        if (TextUtils.isEmpty(message)) return
        showMessageView(message ?: "", false)
    }

    override fun showError(message: String?) {
        if (alertDialog?.isShowing == true)
            alertDialog?.dismiss()
        if (TextUtils.isEmpty(message)) return
        showErrorMessageView(message ?: "")
    }

    override fun showError(message: Int) {
        if (message == 0) return
        showErrorMessageView(getString(message))
    }

    override fun onLanguageChange(message: String?) {
        alertDialog?.dismiss()
        (activity as? CommonFragmentActivity)?.recreate()
    }

    override fun logoutUser() {

    }

    override fun getStringResource(resourseId: Int) = getString(resourseId)

}

interface SettingFragmentNavigator {
    fun isNotificationsAllow(): Boolean
    fun onInternetConncted(status: Boolean)
    fun onCancelInternetDialog(from: Int)
    fun onLanguageChange(message: String?)
}

interface ColorChooseInterface {
    fun onColorChoose(colorPos: Int)
}
