package com.rummytitans.playcashrummyonline.cardgame.ui.base

import com.rummytitans.playcashrummyonline.cardgame.R
import com.rummytitans.playcashrummyonline.cardgame.data.SharedPreferenceStorage
import com.rummytitans.playcashrummyonline.cardgame.games.rummy.FragmentRummyWebViewer
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import androidx.appcompat.view.ContextThemeWrapper
import androidx.fragment.app.Fragment
import com.tapadoo.alerter.Alerter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
open class BaseFragment : Fragment() {

    var localInflater: LayoutInflater? = null

    fun setTheme(inflater: LayoutInflater) {

    }

     fun changeActivityOrientation(target: Int) {
        requireActivity().apply {
            val orientation = resources.configuration.orientation
            if (target == FragmentRummyWebViewer.PORTRAIT && orientation == Configuration.ORIENTATION_LANDSCAPE) {
                Log.i("jswrapper", "changeOrientation to PORTRAIT ")
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            } else if (target == FragmentRummyWebViewer.LANDSCAPE && orientation == Configuration.ORIENTATION_PORTRAIT) {
                Log.i("jswrapper", "changeOrientation to LANDSCAPE")
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }
        }
    }

    open fun requestData(){}

    open fun performSavedActions(){}

    fun setLanguage() {
        context?.let {
            if (TextUtils.isEmpty(com.rummytitans.playcashrummyonline.cardgame.utils.LocaleHelper.getLanguage(it))) com.rummytitans.playcashrummyonline.cardgame.utils.LocaleHelper.setLocale(it)
            else com.rummytitans.playcashrummyonline.cardgame.utils.LocaleHelper.onAttach(it)
        }
    }

    fun showErrorMessageView(message: String, dismissListener: () -> Unit = { }) {
        if (TextUtils.isEmpty(message)) return
        Alerter.create(activity).enableVibration(false).setText(message)
            .setBackgroundColorRes(R.color.error_red)
            .setOnHideListener { dismissListener() }.show()
    }

    fun showErrorMessageView(message: Int, dismissListener: () -> Unit = { }) {
        showErrorMessageView(getString(message), dismissListener)
    }

    fun showMessageView(message: String, isBackOnDismiss: Boolean = false) {
        if (TextUtils.isEmpty(message)) return
        Alerter.create(activity).enableVibration(false).setText(message)
            .setBackgroundColorRes(R.color.bluish_green)
            .setOnHideListener { if (isBackOnDismiss) activity?.onBackPressed() }
            .show()
    }
    fun showWarningMessageView(message: String, dismissListener: () -> Unit = { }) {
        if (TextUtils.isEmpty(message)) return
        Alerter.create(activity).enableVibration(false).setText(message).showIcon(false)
            .setBackgroundColorRes(R.color.orange)
            .setOnHideListener { dismissListener() }.show()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
