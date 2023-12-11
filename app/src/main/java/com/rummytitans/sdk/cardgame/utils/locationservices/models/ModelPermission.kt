package com.rummytitans.sdk.cardgame.utils.locationservices.models


import com.rummytitans.sdk.cardgame.utils.locationservices.utils.JsonObject
import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize
import android.os.Parcelable
import org.json.JSONArray
import org.json.JSONObject

@Parcelize
@Keep
data class ModelPermission(
    val permName: String,
    val permInfoName: String,
    var isGranted: Boolean,
    val isMandatory: Boolean
) : Parcelable {
    constructor(permName: String, permFullName: String): this(permName, permFullName, false, true)
}

fun ModelPermission.toJson(): JSONObject{
    return JsonObject {
        "perm_name" to permName
        "perm_full_name" to permInfoName
        "is_granted" to isGranted
        "is_mandatory" to isMandatory
    }
}

fun JSONObject?.jsonToModelPermission(): ModelPermission? {
    this?.runCatching {
        return ModelPermission(
            getString("perm_name"),
            optString("perm_full_name", getString("perm_name")),
            optBoolean("is_granted", false),
            optBoolean("is_mandatory", true)
        )
    }
    return null
}

fun JSONArray?.jsonToModelPermissionsList(): List<ModelPermission>? {
    this?.runCatching {
        val list = ArrayList<ModelPermission>()
        repeat(this.length()){
            getJSONObject(it).jsonToModelPermission()?.let { model-> list.add(model)}
        }
        return list
    }
    return null
}