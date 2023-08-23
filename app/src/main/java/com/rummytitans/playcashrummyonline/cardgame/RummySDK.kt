package com.rummytitans.playcashrummyonline.cardgame

import android.content.Context
import android.content.Intent
import androidx.annotation.Keep
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.rummytitans.playcashrummyonline.cardgame.data.SharedPreferenceStorage
import com.rummytitans.playcashrummyonline.cardgame.ui.LaunchActivity
import com.rummytitans.playcashrummyonline.cardgame.ui.RummyMainActivity
import com.rummytitans.playcashrummyonline.cardgame.ui.launcher.SDKSplashActivity
import java.io.ByteArrayInputStream
import java.io.ObjectInputStream

@Keep
object RummyTitanSDK {

    private lateinit var appContext: Context

    fun initialize(context: Context) {
        appContext = context.applicationContext
    }


    fun initialize1(context: Context) {
        appContext = context
    }
    fun startLibraryActivity() {
        val intent = Intent(appContext, SDKSplashActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        appContext.startActivity(intent)
    }

    fun inItSDK(context: Context){
        val intent = Intent(context,SDKSplashActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)

    }

    fun inItSDK1(context: Context){
        val intent = Intent(context,LaunchActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)

    }

    fun inItSDK2(context: Context,encodedString : String){
        val byteArray = encodedString.toByteArray()

        val byteArrayInputStream = ByteArrayInputStream(byteArray)
        val objectInputStream = ObjectInputStream(byteArrayInputStream)

        val decodedObject = objectInputStream.readObject()

        objectInputStream.close()

        println("Decoded String: $decodedObject")
        SharedPreferenceStorage(context).let { prefs->
            try {
                // Parse the decoded JSON string using Gson
                val gson = Gson()
                SharedPreferenceStorage(context).loginResponse = gson.toJson(decodedObject)
                // Use the parsed model
                // ...
            } catch (e: JsonSyntaxException) {
                // Handle JSON parsing errors
                e.printStackTrace()
                println("Error while parsing JSON: ${e.message}")
            }


            val intent = Intent(context, RummyMainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)

        }



    }


}