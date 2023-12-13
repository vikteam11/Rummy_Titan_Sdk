package com.rummytitans.playcashrummyonline.cardgame.models;


import com.google.gson.annotations.SerializedName;

import java.util.List;

public class RakeBackDetailModel {
    @SerializedName("AvailableRakeBack")
    public Double availableRakeBackAmount;

    @SerializedName("UpcomingRakeBack")
    public Double upcomingRakeBackAmount;

    @SerializedName("TotalRakeBack")
    public Double totalRakeBackAmount;

    @SerializedName("RedemptionHistory")
    public List<RakeBackHistoryModel> redeemHistory;

    @SerializedName("RakeBackDescription")
    public String description;

    @SerializedName("RakebackRedeemedDate")
    public String rakeBackRedeemedDate;

    @SerializedName("EligibleDate")
    public String eligibleDate;


    @SerializedName("TncUrl")
    public String tncUrl;

    public String rakeBackDescription(){
        String desc = "You can check your total earned rakeback" +
                " <b>" +totalRakeBackAmount+ "</b><br>" +
                " in your wallet. To know more about<br>rakeback program " +
                "<b><font color='#FF0000'>click here</font></b>.";
        return  desc ;
    }

    public static class RakeBackHistoryModel {

        @SerializedName("Date")
        public String date;

        @SerializedName("Amount")
        public Double Amount;

    }

}
