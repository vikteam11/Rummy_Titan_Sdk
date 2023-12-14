package com.rummytitans.sdk.cardgame.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class WithdrawModel {
    @SerializedName("PaytmBankNo")
    public String PaytmBankNo;
    @SerializedName("BankDetail")
    public BankDetail BankDetail ;

    @SerializedName("WithdrawalOptions")
    public ArrayList<WithdrawalMethodModel> WithdrawalOptions ;
    @SerializedName("ErrorMessage")
    public String ErrorMessage;
    @SerializedName("TDSTnC")
    public String TdsUrl ;
    @SerializedName("TDSFreeWithdrawalMessage")
    public String TDSFreeWithdrawalMessage ;
    @SerializedName("TDSFreeWithdrawalAmount")
    public double TDSFreeWithdrawalAmount ;
    public static class WithdrawOption {
        @SerializedName("ID")
        public int ID;
        @SerializedName("Name")
        public String Name;
        @SerializedName("MinLimit")
        public String MinLimit;
        @SerializedName("Limit")
        public String Limit;
        @SerializedName("Message")
        public String Message;
        @SerializedName("TnCUrl")
        public String TnCUrl;
        @SerializedName("IsSelect")
        public boolean IsSelect;
        @SerializedName("Disable")
        public boolean Disable;
        @SerializedName("DisableMessage")
        public String DisableMessage;
    }

    public static class BankDetail{
        @SerializedName("AccountNo")
        public String AccountNo;
        @SerializedName("IFSC")
        public String IFSC;
        @SerializedName("Branch")
        public String Branch;
        @SerializedName("BankName")
        public String BankName;
        @SerializedName("DisableMessage")
        public String DisableMessage;
    }
}


