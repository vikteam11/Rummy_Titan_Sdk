package com.rummytitans.playcashrummyonline.cardgame.sdk_callbacks

import android.os.Bundle
import com.rummytitans.playcashrummyonline.cardgame.models.LoginResponse
import org.json.JSONObject

interface AnalticsCallback {
    fun addTriggerSDK(key:String,value:String)
    fun sendEventToFireBaseSDK(eventName: String, eventData: Bundle)
    fun fireAttributesEventSDK(eventName: String?, userId: String?)
    fun fireEventSDK(key: String, bundle: Bundle? = Bundle())
    fun setUserPropertySDK(property: String, value: String)
    fun setJsonUserPropertySDK(json: JSONObject)
    fun setUserDataToToolsSDK()
}