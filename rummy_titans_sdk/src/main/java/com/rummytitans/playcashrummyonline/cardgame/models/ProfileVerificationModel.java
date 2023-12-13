package com.rummytitans.playcashrummyonline.cardgame.models;

import com.google.gson.annotations.SerializedName;

public class ProfileVerificationModel {
    @SerializedName("BankVerify")
    public boolean BankVerify;
    @SerializedName("AccNo")
    public String AccNo;
    @SerializedName("PanVerify")
    public boolean PanVerify;
    @SerializedName("PanCardNumber")
    public String PanCardNumber;
    @SerializedName("MobileVerify")
    public boolean MobileVerify;
    @SerializedName("MobileNumber")
    public String MobileNumber;
    @SerializedName("EmailVerify")
    public boolean EmailVerify;
    @SerializedName("Email")
    public String Email;
    @SerializedName("Url")
    public String Url;

    @SerializedName("AddressVerified")
    public boolean AddressVerified;
    @SerializedName("AddressNo")
    public String AddressNo;
    @SerializedName("AddressType")
    public int AddressType;

    public boolean profileVerified(){
        return (MobileVerify || !MobileNumber.equals(""))
                && (EmailVerify || !Email.equals(""))
                && (PanVerify || !PanCardNumber.equals(""))
                && (BankVerify || !AccNo.equals(""))
                && (AddressVerified || !AddressNo.equals(""));
    }
}
