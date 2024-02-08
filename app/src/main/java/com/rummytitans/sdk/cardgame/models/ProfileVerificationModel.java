package com.rummytitans.sdk.cardgame.models;

import com.google.gson.annotations.SerializedName;

public class ProfileVerificationModel {
    @SerializedName("Url")
    public String Url;
    @SerializedName("AddressType")
    public int AddressType;
    @SerializedName("Email")
    public ProfileVerificationItemModel EmailItem;
    @SerializedName("Mobile")
    public ProfileVerificationItemModel MobileItem;
    @SerializedName("Pancard")
    public ProfileVerificationItemModel PancardItem;
    @SerializedName("Bank")
    public ProfileVerificationItemModel BankItem;
    @SerializedName("Address")
    public ProfileVerificationItemModel AddressItem;
    public boolean isEmailVerify(){
        return EmailItem.Verify;
    }
    public boolean profileVerified(){
        return (MobileItem.Verify || !MobileItem.Value.equals(""))
                && (EmailItem.Verify || !EmailItem.Value.equals(""))
                && (PancardItem.Verify || !PancardItem.Value.equals(""))
                && (BankItem.Verify || !BankItem.Value.equals(""))
                && (AddressItem.Verify || !AddressItem.Value.equals(""));
    }
    public static class ProfileVerificationItemModel{
        @SerializedName("Verify")
        public boolean Verify;
        @SerializedName("Message")
        public String Message;
        @SerializedName("Value")
        public String Value;
        @SerializedName("IsUpload")
        public boolean IsUpload;

        /*
         * IsUpload = true means user can can upload documents.
         * IsUpload = false user can't upload documents. verify button should be disabled
         * */
        public boolean isBlocked(){

            return  !IsUpload;
        }
    }
}
