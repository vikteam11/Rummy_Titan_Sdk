package com.rummytitans.sdk.cardgame.games.locationBottomSheet

import com.rummytitans.sdk.cardgame.R
import com.rummytitans.sdk.cardgame.databinding.ActivityLocationSheetRummyBinding
import com.rummytitans.sdk.cardgame.ui.base.BaseActivity
import com.rummytitans.sdk.cardgame.utils.locationservices.constants.LocationConstants
import com.rummytitans.sdk.cardgame.utils.locationservices.constants.LocationConstants.RESPONSE_DEFAULT_LAT_LOG
import com.rummytitans.sdk.cardgame.utils.locationservices.constants.LocationConstants.RESPONSE_LATITUDE
import com.rummytitans.sdk.cardgame.utils.locationservices.constants.LocationConstants.RESPONSE_LOGITUDE

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.rummytitans.sdk.cardgame.utils.LocaleHelper
import javax.inject.Inject


class LocationBottomSheetActivity : BaseActivity(), LocationNavigator {


    lateinit var mViewModel: LocationViewModel
    lateinit var mBinding: ActivityLocationSheetRummyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        if (TextUtils.isEmpty(LocaleHelper.getLanguage(this))) {
            LocaleHelper.setLocale(
                this, getString(R.string.english_code), getString(R.string.english)
            )
        } else LocaleHelper.onAttach(this)
        window.setBackgroundDrawable(
            ContextCompat
                .getDrawable(this, android.R.drawable.screen_background_dark_transparent)
        )
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.O) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        }
        mViewModel = ViewModelProvider(
            this
        ).get(LocationViewModel::class.java)
        mViewModel.navigator = this
        mViewModel.navigatorAct = this
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_location_sheet_rummy)
        mBinding.let {
            it.viewModel = mViewModel
            it.lifecycleOwner = this
            it.executePendingBindings()
        }
        mViewModel.toggleLocationBottomSheet()
    }

    override fun requestLocation() {
        //RequestGPSActivity.startActivityForResultGetLatLong(this)
    }



    fun onLocationReceived(latitude: Double, longitude: Double, responseCode: Int) {
        val intent = Intent().putExtra(RESPONSE_LATITUDE, latitude)
            .putExtra(RESPONSE_LOGITUDE, longitude)
        setResult(responseCode, intent)
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        data?.extras?.apply {
            val latitude = getDouble(RESPONSE_LATITUDE, RESPONSE_DEFAULT_LAT_LOG)
            val longitude = getDouble(RESPONSE_LOGITUDE, RESPONSE_DEFAULT_LAT_LOG)
            onLocationReceived(latitude, longitude, LocationConstants.RESPONSE_LOCATION_OK)
        }
    }
}

interface LocationNavigator {
    fun requestLocation()
}