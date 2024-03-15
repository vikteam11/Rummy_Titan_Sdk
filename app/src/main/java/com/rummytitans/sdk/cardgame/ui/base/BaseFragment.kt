package com.rummytitans.sdk.cardgame.ui.base

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import com.rummytitans.sdk.cardgame.R
import com.rummytitans.sdk.cardgame.utils.LocaleHelper
import com.tapadoo.alerter.Alerter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
open class BaseFragment : Fragment() {

    var localInflater: LayoutInflater? = null

    fun setTheme(inflater: LayoutInflater) {}

     fun changeActivityOrientation(target: Int) {
        requireActivity().apply {
            val orientation = resources.configuration.orientation
            if (target == Configuration.ORIENTATION_PORTRAIT && orientation == Configuration.ORIENTATION_LANDSCAPE) {
                Log.i("jswrapper", "changeOrientation to PORTRAIT ")
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            } else if (target == Configuration.ORIENTATION_LANDSCAPE && orientation == Configuration.ORIENTATION_PORTRAIT) {
                Log.i("jswrapper", "changeOrientation to LANDSCAPE")
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }
        }
    }

    open fun requestData(){}

    open fun performSavedActions(){}

    fun setLanguage() {
        context?.let {
            if (TextUtils.isEmpty(LocaleHelper.getLanguage(it))) LocaleHelper.setLocale(it)
            else LocaleHelper.onAttach(it)
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
