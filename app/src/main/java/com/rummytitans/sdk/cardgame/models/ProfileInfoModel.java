package com.rummytitans.sdk.cardgame.models;

import com.google.gson.annotations.SerializedName;

public class ProfileInfoModel {

    @SerializedName("Address")
    public String Address;
    @SerializedName("Gender")
    public String Gender;
    @SerializedName("DOB")
    public String DOB;
    @SerializedName("StateName")
    public String StateName;
    @SerializedName("TeamNmae")
    public String TeamNmae;
    @SerializedName("IsFirstTime")
    public boolean IsFirstTime;
    @SerializedName("MobileVerify")
    public boolean MobileVerify;
    @SerializedName("EmailVerify")
    public boolean EmailVerify;
    @SerializedName("Mobile")
    public String Mobile;
    @SerializedName("Email")
    public String Email;
    @SerializedName("Name")
    public String Name;
    @SerializedName("UserId")
    public int UserId;
    @SerializedName("PinCode")
    public String PinCode;
    @SerializedName("AvtarId")
    public int AvtarId;
    @SerializedName("REferAmount")
    public String REferAmount;
}
