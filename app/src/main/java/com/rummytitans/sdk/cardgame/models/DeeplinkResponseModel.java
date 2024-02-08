package com.rummytitans.sdk.cardgame.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class DeeplinkResponseModel implements Serializable {
    @SerializedName("Title")
    public String Title="";
    @SerializedName("DeeplinksUrl")
    public String DeeplinksUrl;
}