package com.rummytitans.playcashrummyonline.cardgame.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class AddCashOfferModel {
    @SerializedName("Headers")
    public ArrayList<WalletInfoModel.Offer> Headers;

    @SerializedName("Offers")
    public ArrayList<AddCash> Offers;

    @SerializedName("Coupans")
    public ArrayList<AvailableCouponModel> Coupans;

    @SerializedName("AddCash")
    public List<AddCash> AddCash;
    @SerializedName("TandC")
    public String TandC;
    @SerializedName("Title")
    public String Title;

    public static class AddCash {
        @SerializedName("Id")
        public String Id;

        @SerializedName("Add")
        public Double Add;

        @SerializedName("Get")
        public Double Get;

        @SerializedName("Popular")
        public boolean Popular;

        public boolean isSelected;

        public String FillInTax;

        public String getPercent(){
            return Math.round(Get/Add*100.0)+"%";
        }
    }
}
