package com.rummytitans.sdk.cardgame.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

public class ReferModel implements Serializable {


    @SerializedName("Response")
    public ArrayList<Response> Response;
    @SerializedName("ReferCode")
    public String ReferCode;
    @SerializedName("ReferMessage")
    public String ReferMessage;
    @SerializedName("Message")
    public String Message;
    @SerializedName("TokenExpire")
    public boolean TokenExpire;
    @SerializedName("Status")
    public boolean Status;
    @SerializedName("ImageUrl")
    public String ImageUrl;


    @SerializedName("ReferMessage1")
    public String ReferMessage1;


    @SerializedName("ReferMessage2")
    public String ReferMessage2;

    @SerializedName("ReferMessage3")
    public String ReferMessage3;

    @SerializedName("ReferShareMessage")
    public String referralShareMessage;

    public static class Response implements Serializable {
        @SerializedName("MaxAmount")
        public int MaxAmount;
        @SerializedName("Amount")
        public int Amount;
        @SerializedName("ReferCode")
        public String ReferCode;
        @SerializedName("AvtaarId")
        public int AvtaarId;
        @SerializedName("PanTranFlag")
        public boolean PanTranFlag;
        @SerializedName("Name")
        public String Name;

        @SerializedName("Verifyrefer")
        public boolean Verifyrefer;
        @SerializedName("Panverify")
        public boolean PanVerify;
        @SerializedName("SignUpAmount")
        public String SignUpAmount;
        @SerializedName("ReferAdded")
        public String ReferAdded;
        @SerializedName("EranAdmount")
        public String EarnAdmount;

        public Boolean ishide=true;
        @SerializedName("TeamName")
        public String TName;
    }
}
