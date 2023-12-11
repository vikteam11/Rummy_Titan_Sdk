package com.rummytitans.sdk.cardgame.models;

import com.google.gson.annotations.SerializedName;

public class KeyValuePairModel {
    @SerializedName("Key")
    public String key;

    @SerializedName("Value")
    public String value ;

    @SerializedName("ColorCode")
    public int colorCode;

}
