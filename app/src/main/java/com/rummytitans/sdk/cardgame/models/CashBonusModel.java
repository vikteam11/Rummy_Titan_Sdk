package com.rummytitans.sdk.cardgame.models;

import com.google.gson.annotations.SerializedName;

public class CashBonusModel {

    @SerializedName("ExpiryDate")
    public String ExpiryDate;
    @SerializedName("Status")
    public String Status;
    @SerializedName("Remaining")
    public String Remaining;
    @SerializedName("Used")
    public String Used;
    @SerializedName("Bonus")
    public String Bonus;
}
