package com.rummytitans.sdk.cardgame.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class JoinGameConfirmationModel {

    @Expose
    @SerializedName("Disclaimer")
    public String disclaimer;
    @SerializedName("BonusList")
    public List<JoinGameBonus> bonusList ;
    @SerializedName("UsableBalance")
    public double usableBalance;

    @Expose
    @SerializedName("TotalAmountCollect")
    public double totalAmountCollect;

    @SerializedName("Bonus")
    public double bonus;

    @SerializedName("TotalAmount")
    public double totalAmount;

    @SerializedName("EntryFee")
    public double entryFee;

    @Expose
    @SerializedName("Ticket")
    public double ticket;

    @Expose
    @SerializedName("IsAddressVerified")
    public boolean IsAddressVerified;

    public double getTicketAmount(){
        return ticket;
    }
    public static class JoinGameBonus {
        @SerializedName("Name")
        public String Name;
        @SerializedName("Val")
        public double Val;
    }
}
