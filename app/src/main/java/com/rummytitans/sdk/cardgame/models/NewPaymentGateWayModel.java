package com.rummytitans.sdk.cardgame.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

public class NewPaymentGateWayModel implements Serializable {
    @Expose
    @SerializedName("TokenExpire")
    public boolean TokenExpire;

    @Expose
    @SerializedName("Message")
    public String Message;

    @Expose
    @SerializedName("Status")
    public boolean Status;

    @Expose
    @SerializedName("IsVPAAllow")
    public boolean IsVPAAllow;

    @Expose
    @SerializedName("JusPayToken")
    public String JusPayToken;

    @SerializedName("ReturnUrl")
    public String ReturnUrl;
    @Expose
    @SerializedName("updateMessage")
    public String updateMessage;

    @Expose
    @SerializedName("isUpdate")
    public boolean isUpdate;

    @Expose
    @SerializedName("Error")
    public String Error;

    @SerializedName("Juspay")
    public JusPayData jusPayData;

    @Expose
    @SerializedName("DelayMinutes")
    public int delayMinutes;

    @SerializedName("GatewayList")
    public ArrayList<GatewayList> GatewayList;

    @SerializedName("IsBanned")
    public Boolean IsUserFromBannedState;


    public static class JusPayData implements Serializable{
        @SerializedName("Token")
        public String Token;
        @SerializedName("OrderId")
        public String OrderId;
        @SerializedName("CustomerId")
        public String CustomerId;
    }

    public static class GatewayList implements Serializable {
        @SerializedName("Title")
        public String Title;

        @SerializedName("Type")
        public int Type;

        @SerializedName("Disable")
        public boolean Disable;

        @SerializedName("List")
        public ArrayList<PaymentResponseModel> List;

        public String AddMoreTitle;
        public String AddMoreDesc;
        public int AddMoreIcon;

        public boolean hideExtraView(){
            return Type==5 || (Type==4 && AddMoreIcon == 0);
        }
    }

    public static class PaymentResponseModel implements Serializable {
        @Expose
        @SerializedName("ImageUrl")
        public String ImageUrl;
        @Expose
        @SerializedName("Name")
        public String Name;
        @Expose
        @SerializedName("PackgeName")
        public String PackgeName;
        @Expose
        @SerializedName("NbMethod")
        public String NbMethod;
        @SerializedName("Message")
        public String Message;
        public boolean isSelected = false;
        @SerializedName("SdkName")
        public String SdkName;
        @SerializedName("IsOffer")
        public boolean IsOffer;
        @SerializedName("IsRecommended")
        public boolean IsRecommended;
        @SerializedName("IsShort")
        public boolean IsShort;
        @Expose
        @SerializedName("ErrorMessage")
        public String ErrorMessage;

        public String Token;
        public String Number;
        public String ReferenceId;
        public String ExpYear;
        public String ExpMonth;
        public String Brand;
        public String NameOnCard;
        public int CvvCount;
        public String BankName;
        public String CardIsIn;

        public String Packge;
        public String Image;
        public String Title;
        public String Code;
        public String Id;
        public Boolean Linked;
        public Boolean DirectDebit;
        public Double Balance;
        public Double DiffAmount;
        public int Type;
        public String cardSecurityKey;
        public boolean isShowCvv = false;
    }

}
