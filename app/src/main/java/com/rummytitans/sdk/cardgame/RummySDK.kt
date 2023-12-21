package com.rummytitans.sdk.cardgame

import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.util.Base64
import androidx.annotation.Keep
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
    }

    fun setCallback(callback: RummyTitansCallback,analticsCallback: AnalticsCallback) {
        rummyCallback = callback
        analytiCallback = analticsCallback
    }

    fun setOptions(options: RummySdkOptions) {
        rummySdkOptions = options
    }

    fun getOption() = rummySdkOptions

    fun initialize1(context: Context) {
        appContext = context
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


            val intent = if (splashResponse?.isEmpty() == true) Intent(
                context,
                SDKSplashActivity::class.java
            ) else
                Intent(context, RummyMainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            if (!TextUtils.isEmpty(deeplink)) {
                intent.putExtra("deeplink", deeplink)
            }
            context.startActivity(intent)

        }
    }


}