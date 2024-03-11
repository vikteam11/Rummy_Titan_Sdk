package com.rummytitans.sdk.cardgame

import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.util.Base64
import androidx.annotation.Keep
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.rummytitans.sdk.cardgame.data.SharedPreferenceStorageRummy
import com.rummytitans.sdk.cardgame.sdk_callbacks.AnalticsCallback
import com.rummytitans.sdk.cardgame.sdk_callbacks.RummySdkOptions
import com.rummytitans.sdk.cardgame.sdk_callbacks.RummyTitansCallback
import com.rummytitans.sdk.cardgame.ui.RummyMainActivity
import com.rummytitans.sdk.cardgame.ui.launcher.SDKSplashActivity
import java.nio.charset.StandardCharsets

@Keep
object RummyTitanSDK {
    internal var rummyCallback: RummyTitansCallback? = null
    internal var analytiCallback: AnalticsCallback? = null
    private var rummySdkOptions: RummySdkOptions = RummySdkOptions()

    private lateinit var appContext: Context

    fun initialize(context: Context) {
        appContext = context.applicationContext
        MainApplication.appContext = context
    }

    fun setCallback(callback: RummyTitansCallback, analticsCallback: AnalticsCallback) {
        rummyCallback = callback
        analytiCallback = analticsCallback
    }

    fun setOptions(options: RummySdkOptions) {
        rummySdkOptions = options
    }

    fun setUpdateInfo(context: Context, update: Boolean) {
        SharedPreferenceStorageRummy(context).let { pref ->
            pref.isInAppAvailable = update
        }
    }

    fun getOption():RummySdkOptions{
        if (!(::appContext.isInitialized)){
            appContext = MainApplication.appContext
        }
        val pref=SharedPreferenceStorageRummy(appContext)
        return Gson().fromJson(pref.sdkOptions, RummySdkOptions::class.java
        )
    }

    fun startLibraryActivity() {
        val intent = Intent(appContext, SDKSplashActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        appContext.startActivity(intent)
    }

    fun startRummyTitans(
        context: Context, encodedString: String,
        splashResponse: String? = "",
        deeplink: String = ""
    ) {
        val decodedString =
            String(Base64.decode(encodedString, Base64.CRLF), StandardCharsets.UTF_8)
        val splashDecodeString =
            String(Base64.decode(splashResponse, Base64.CRLF), StandardCharsets.UTF_8)
        println("Decoded String: $decodedString")
        println("Deeplink String: $deeplink")
        var newDeeplink = deeplink
        if(deeplink.startsWith("https://m11.io")){
            newDeeplink = deeplink.substringAfter("open/?")
        }
        SharedPreferenceStorageRummy(context).let { pref ->
            try {
                pref.loginCompleted = true
                pref.loginResponse = decodedString
                pref.splashResponse = splashDecodeString
                pref.appUrl = rummySdkOptions.baseUrl
                pref.appUrl2 = rummySdkOptions.baseUrl2
                pref.showKycOptions = rummySdkOptions.showKycOptions
                pref.displayProfileIcon = rummySdkOptions.displayProfileIcon
                pref.locationDelay = rummySdkOptions.locationDelay
                pref.gamePlayUrl = rummySdkOptions.gamePlayUrl
                pref.locationApiTimeLimit = rummySdkOptions.locationDelay
                pref.instanceId = rummySdkOptions.fbInstanceId
            } catch (e: JsonSyntaxException) {
                // Handle JSON parsing errors
                e.printStackTrace()
                println("Error while parsing JSON: ${e.message}")
            }
            pref.sdkOptions = Gson().toJson(rummySdkOptions)
            val intent = if (splashResponse?.isEmpty() == true) Intent(
                context,
                SDKSplashActivity::class.java
            ) else
                Intent(context, RummyMainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            if (!TextUtils.isEmpty(deeplink)) {
                intent.putExtra("deepLink", newDeeplink)
            }
            context.startActivity(intent)

        }
    }


}