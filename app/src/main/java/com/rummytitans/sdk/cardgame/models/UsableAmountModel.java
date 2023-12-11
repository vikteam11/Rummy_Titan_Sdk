package com.rummytitans.sdk.cardgame.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UsableAmountModel {

    @Expose
    @SerializedName("fee")
    public double fee;
    @Expose
    @SerializedName("Message")
    public String Message;
    @Expose
    @SerializedName("UsableSignUp")
    public double UsableSignUp;
    @Expose
    @SerializedName("UsableBonus")
    public double UsableBonus;
    @Expose
    @SerializedName("DailyBonus")
    public double DailyBonus;
    @Expose
    @SerializedName("UsableDailyBonus")
    public double UsableDailyBonus;
    @Expose
    @SerializedName("Winning")
    public double Winning;
    @Expose
    @SerializedName("Unutilized")
    public double Unutilized;
    @Expose
    @SerializedName("Discount")
    public double discount;

    @Expose
    @SerializedName("GoldenTic")
    public GoldenTicketModel GoldenTic;

    public class GoldenTicketModel{

        @SerializedName("Min")
        public int min;

        @SerializedName("Message")
        public String message;

        @SerializedName("Max")
        public int max;
    }
}
