package com.rummytitans.playcashrummyonline.cardgame.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CreatePrivateContestModel {


    @Expose
    @SerializedName("leaugeCode")
    public String leaugeCode;
    @Expose
    @SerializedName("MatchID")
    public int MatchID;
    @Expose
    @SerializedName("LeagueID")
    public int LeagueID;
    @Expose
    @SerializedName("Fees")
    public int Fees;
    @Expose
    @SerializedName("NoofMembers")
    public int NoofMembers;
}
