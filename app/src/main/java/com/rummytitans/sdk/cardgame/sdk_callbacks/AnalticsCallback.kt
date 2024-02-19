package com.rummytitans.sdk.cardgame.sdk_callbacks

import android.os.Bundle
import com.rummytitans.sdk.cardgame.models.LoginResponse
import org.json.JSONObject

interface AnalticsCallback {
    fun addTriggerSDK(key:String,value:String){}
    fun sendEventToFireBaseSDK(eventName: String, eventData: Bundle)
    fun fireAttributesEventSDK(eventName: String?, userId: String?)
    fun fireEventSDK(key: String, bundle: Bundle? = Bundle())
    fun setUserPropertySDK(property: String, value: String)
    fun setJsonUserPropertySDK(json: JSONObject,update : Boolean = false)
    fun setCleverTapUserLocationSDK(lat:Double ,long:Double)
    fun setUserDataToToolsSDK(){}
}