package com.rummytitans.sdk.cardgame.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

public class ReferContentModel implements Serializable {

    @SerializedName("Content")
    public ArrayList<ReferContent> content;

    @SerializedName("ReferCode")
    public String referCode;

    @SerializedName("Message")
    public String referMessage;

    @SerializedName("ShareMessage")
    public String referralShareMessage;

    @SerializedName("Banner")
    public String banner;

    @SerializedName("TnCURL")
    public String tnCURL;

    public static class ReferContent implements Serializable {

        @SerializedName("Icon")
        public String Icon;
        @SerializedName("Message")
        public String Message;
    }
}
