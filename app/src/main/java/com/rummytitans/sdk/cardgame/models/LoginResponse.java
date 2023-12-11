package com.rummytitans.sdk.cardgame.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class LoginResponse implements Serializable {
    @SerializedName("FairPlayMessage")
    public String FairPlayMessage;
    @SerializedName("IsFairPlay")
    public boolean IsFairPlay;
    @SerializedName("IsFirstTime")
    public boolean IsFirstTime;
    @SerializedName("MobileVerify")
    public boolean MobileVerify;
    @SerializedName("EmailVerify")
    public boolean EmailVerify;
    @SerializedName("ExpireToken")
    public String ExpireToken="";
    @SerializedName("AuthExpire")
    public String AuthExpire="";
    @SerializedName("Mobile")
    public String Mobile="";
    @SerializedName("Email")
    public String Email;
    @SerializedName("Name")
    public String Name;
    @SerializedName("UserId")
    public int UserId = 0;
    @SerializedName("IsTwo")
    public Boolean isTwo;
    public String gameState = "";

    public boolean AllowRummyGame;
    public String RummyGameUrl;
}
