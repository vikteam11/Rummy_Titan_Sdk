package com.rummytitans.playcashrummyonline.cardgame

import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.util.Base64
import androidx.annotation.Keep
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.rummytitans.playcashrummyonline.cardgame.data.SharedPreferenceStorage
import com.rummytitans.playcashrummyonline.cardgame.sdk_callbacks.RummySdkOptions
import com.rummytitans.playcashrummyonline.cardgame.sdk_callbacks.RummyTitansCallback
import com.rummytitans.playcashrummyonline.cardgame.ui.LaunchActivity
import com.rummytitans.playcashrummyonline.cardgame.ui.RummyMainActivity
import com.rummytitans.playcashrummyonline.cardgame.ui.launcher.SDKSplashActivity
import java.io.ByteArrayInputStream
import java.io.ObjectInputStream
import java.nio.charset.StandardCharsets

@Keep
object RummyTitanSDK {
    internal var rummyCallback:RummyTitansCallback?= null
    private var rummySdkOptions:RummySdkOptions = RummySdkOptions()

    private lateinit var appContext: Context

    fun initialize(context: Context) {
        appContext = context.applicationContext
    }

    fun setCallback(callback: RummyTitansCallback){
        rummyCallback = callback
    }
    fun setOptions(options: RummySdkOptions){
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

    fun startRummyTitans(context: Context,encodedString : String,
                         splashResponse:String,
                         deeplink:String=""){
        val decodedString=String(Base64.decode(encodedString, Base64.CRLF), StandardCharsets.UTF_8)
        val splashDecodeString=String(Base64.decode(splashResponse, Base64.CRLF), StandardCharsets.UTF_8)
        println("Decoded String: $decodedString")
        SharedPreferenceStorage(context).let { prefs->
            try {
               val pref =  SharedPreferenceStorage(context)
                pref.loginCompleted = true
                pref.loginResponse = decodedString
                pref.splashResponse = splashDecodeString
                pref.appUrl = rummySdkOptions.baseUrl
                pref.appUrl2 = rummySdkOptions.baseUrl2
                pref.gamePlayUrl= rummySdkOptions.gamePlayUrl
                pref.locationApiTimeLimit = rummySdkOptions.locationDelay
            } catch (e: JsonSyntaxException) {
                // Handle JSON parsing errors
                e.printStackTrace()
                println("Error while parsing JSON: ${e.message}")
            }


            val intent = Intent(context, SDKSplashActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            if(!TextUtils.isEmpty(deeplink)){
                intent.putExtra("deeplink",deeplink)
            }
            context.startActivity(intent)

        }
    }


}