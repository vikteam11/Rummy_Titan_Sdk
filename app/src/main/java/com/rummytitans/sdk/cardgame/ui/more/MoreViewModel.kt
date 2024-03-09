package com.rummytitans.sdk.cardgame.ui.more

import com.rummytitans.sdk.cardgame.analytics.AnalyticsHelper
import com.rummytitans.sdk.cardgame.api.APIInterface
import com.rummytitans.sdk.cardgame.data.SharedPreferenceStorageRummy
import com.rummytitans.sdk.cardgame.models.LoginResponseRummy
import com.rummytitans.sdk.cardgame.models.VersionModelRummy
import com.rummytitans.sdk.cardgame.ui.BaseViewModel
import com.rummytitans.sdk.cardgame.utils.ConnectionDetector
import com.rummytitans.sdk.cardgame.utils.WebViewUrls
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
@HiltViewModel
class MoreViewModel @Inject constructor(
    val prefs: SharedPreferenceStorageRummy,
    val gson: Gson,
    val apiInterface: APIInterface,
    val connectionDetector: ConnectionDetector, val analyticsHelper: AnalyticsHelper
) : BaseViewModel<MoreNavigator>() {

    val loginResponse: LoginResponseRummy = gson.fromJson(prefs.loginResponse, LoginResponseRummy::class.java)
    var versionResponse: VersionModelRummy =
        gson.fromJson(prefs.splashResponse, VersionModelRummy::class.java)

    var regularColor = prefs.regularColor
    var safeColor = prefs.safeColor
    val selectedColor = ObservableField(if (prefs.onSafePlay) safeColor else regularColor)
    val newNotificationAvailable = ObservableBoolean(false)

    fun logout() {
        logoutStatus(apiInterface, loginResponse.UserId, prefs.androidId ?: "", "0")
        prefs.loginResponse = gson.toJson(LoginResponseRummy())
        prefs.loginCompleted = false
        navigator.logoutUser()

    }

    fun getWebUrls(url: String): String {
        return WebViewUrls.AppDefaultURL  + url
    }

    fun setDefaultItems() {
        selectedColor.set(if (prefs.onSafePlay) safeColor else regularColor)
    }

    fun updateThemeCode() {
        regularColor = prefs.regularColor
        safeColor = prefs.safeColor
    }

    fun getNotificationKey() {
        /*if(prefs.latestNotificationAvailable){
            navigatorAct.notificationApiCalled()
            newNotificationAvailable.set(true)
            return
        }

        if (!connectionDetector.isConnected) {
            return
        }
        compositeDisposable.add(
            apiInterface.getNotificationKey(
                loginResponse.UserId,
                loginResponse.ExpireToken,
                loginResponse.AuthExpire,)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (it.Status) {
                        if (!TextUtils.isEmpty(it.NotificationKey)) {
                            if (it.NotificationKey.equals("0")) {
                                newNotificationAvailable.set(false)
                                prefs.latestNotificationAvailable= false
                            }
                            else {
                                newNotificationAvailable.set(it.NotificationKey != prefs.notificationKey)
                                prefs.latestNotificationAvailable = it.NotificationKey != prefs.notificationKey
                            }
                        }
                    }
                }, {
                })
        )*/
    }

}