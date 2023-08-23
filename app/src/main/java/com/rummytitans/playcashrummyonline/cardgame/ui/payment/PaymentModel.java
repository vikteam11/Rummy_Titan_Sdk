package com.rummytitans.playcashrummyonline.cardgame.ui.payment;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class PaymentModel {


    @Expose
    @SerializedName("FullWl")
    public ArrayList<PaymentResponseModel> FullWl;
    @Expose
    @SerializedName("ShortWl")
    public ArrayList<PaymentResponseModel> ShortWl;
    @Expose
    @SerializedName("FullNb")
    public ArrayList<PaymentResponseModel> FullNb;
    @Expose
    @SerializedName("ShortNb")
    public ArrayList<PaymentResponseModel> ShortNb;
    @Expose
    @SerializedName("OtherGetWayTop")
    public ArrayList<AdditionalGateways> OtherGetWayTop;
    @Expose
    @SerializedName("AddCard")
    public ArrayList<AddCard> AddCard;

    @Expose
    @SerializedName("OtherGetWay")
    public ArrayList<AdditionalGateways> OtherGetWay;
    @Expose
    @SerializedName("CustomerId")
    public String CustomerId;
    @Expose
    @SerializedName("OrderId")
    public String OrderId;
    @Expose
    @SerializedName("Message")
    public String Message;
    @Expose
    @SerializedName("Status")
    public boolean Status;
    @Expose
    @SerializedName("JusPayToken")
    public String JusPayToken;

    public static class PaymentResponseModel {
        @Expose
        @SerializedName("ImageUrl")
        public String ImageUrl;
        @Expose
        @SerializedName("Name")
        public String Name;
        @Expose
        @SerializedName("NbMethod")
        public String NbMethod;
        @SerializedName("Message")
        public String Message;
    }


    public static class AddCard {
        @Expose
        @SerializedName("ImageUrl")
        public String ImageUrl;
        @Expose
        @SerializedName("NameOnCard")
        public String NameOnCard;
        @Expose
        @SerializedName("CardBrand")
        public String CardBrand;
        @Expose
        @SerializedName("CardExpMonth")
        public String CardExpMonth;
        @Expose
        @SerializedName("CardExpYear")
        public String CardExpYear;
        @Expose
        @SerializedName("CardNumber")
        public String CardNumber;
        @Expose
        @SerializedName("CardReference")
        public String CardReference;
        @Expose
        @SerializedName("CardToken")
        public String CardToken;
        @Expose
        @SerializedName("CvvCount")
        public Integer CvvCount;
        public String cardSecurityKey;
        public boolean isShowCvv = false;
    }

    public class AdditionalGateways {
        @SerializedName("Url")
        public String Url;
        @SerializedName("Image")
        public String Image;
        @SerializedName("IsJusPay")
        public boolean IsJusPay;
        @SerializedName("Name")
        public String Name;
        @SerializedName("Message")
        public String Message;
    }


}
