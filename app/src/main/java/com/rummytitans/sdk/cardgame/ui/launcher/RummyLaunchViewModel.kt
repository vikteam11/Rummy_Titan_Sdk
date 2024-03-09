package com.rummytitans.sdk.cardgame.ui.launcher

import com.rummytitans.sdk.cardgame.BuildConfig
import com.rummytitans.sdk.cardgame.analytics.AnalyticsHelper
import com.rummytitans.sdk.cardgame.analytics.AnalyticsKey
import com.rummytitans.sdk.cardgame.data.SharedPreferenceStorageRummy
import com.rummytitans.sdk.cardgame.models.LoginResponseRummy
import com.rummytitans.sdk.cardgame.models.VersionModelRummy
import com.rummytitans.sdk.cardgame.ui.BaseViewModel
import com.rummytitans.sdk.cardgame.utils.ConnectionDetector
import com.rummytitans.sdk.cardgame.utils.MyConstants
import com.rummytitans.sdk.cardgame.utils.getNotLoginUser
import com.rummytitans.sdk.cardgame.widget.MyDialog
import android.text.TextUtils
import androidx.annotation.Keep
import androidx.core.os.bundleOf
import androidx.databinding.ObservableInt
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.rummytitans.sdk.cardgame.MainApplication
import com.rummytitans.sdk.cardgame.RummyTitanSDK
import com.rummytitans.sdk.cardgame.ui.base.BaseNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
@Keep
@HiltViewModel
class RummyLaunchViewModel @Inject constructor(
    val prefs: SharedPreferenceStorageRummy,
    val gson: Gson, val connectionDetector: ConnectionDetector, var analyticsHelper: AnalyticsHelper
) : BaseViewModel<BaseNavigator>() {

    var isFailed: ObservableInt = ObservableInt(0)
    var failedReason: MutableLiveData<Throwable> = MutableLiveData()
    var versionResponse = MutableLiveData<VersionModelRummy>()
    var name: String = ""
    var myDialog: MyDialog? = null


    fun fetchVersion() {
        if (!connectionDetector.isConnected) {
            myDialog?.noInternetDialogExit(
               retryListener =  { fetchVersion() },
                cancelListener = {navigator.goBack()}
            )
            return
        }
        val loginResponse = prefs.loginResponse.let {
            try {
                if (TextUtils.isEmpty(it)) getNotLoginUser()
                else gson.fromJson(prefs.loginResponse, LoginResponseRummy::class.java)
            } catch (e: Exception) {
                getNotLoginUser()
            }
        }

        val apis = getApiEndPointObject(RummyTitanSDK.getOption().gameSplashUrl)

        compositeDisposable.add(
            apis.getVersion(
                loginResponse?.UserId ?: 0,
                BuildConfig.VERSION_CODE, prefs.advertisingId?:""
            ).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (it.Status) {
                       /* it.Response.enableAppUpdateBtn=if (BuildConfig.isPlayStoreApk==1)
                            it.Response.IsAppUpdate && prefs.isInAppAvailable
                        else it.Response.IsAppUpdate*/
                        MainApplication.appUrl = it.Response.BaseUrl
                        prefs.appUrl =it.Response.BaseUrl
                        prefs.appUrl2 = it.Response.BaseUrl2
                        prefs.loginAuthTokan = it.Response.LoginAuthTokan ?: ""
                        versionResponse.value = it.Response
                        prefs.splashResponse = gson.toJson(it.Response)
                        MyConstants.LOGS_ENABLE = it.Response.isLogsEnable
                    } else {
                        navigator.showError(it.Message)
                    }
                }, {
                    analyticsHelper.fireEvent(
                        AnalyticsKey.Names.AppOpenFailed,
                        bundleOf(AnalyticsKey.Keys.Message to it.message)
                    )
                    failedReason.value = it
                    isFailed = ObservableInt(1)
                })
        )
    }
}

enum class LaunchDestination { ONBOARDING, MAIN_ACTIVITY, LOGIN_ACTIVITY }

