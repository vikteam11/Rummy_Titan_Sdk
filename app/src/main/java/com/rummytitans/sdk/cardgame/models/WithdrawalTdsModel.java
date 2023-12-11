package com.rummytitans.sdk.cardgame.models;

import com.google.gson.annotations.SerializedName;

public class WithdrawalTdsModel {

    @SerializedName("Title")
    public String title;

    @SerializedName("Value")
    public String value;

    @SerializedName("Amount")
    public Double amount;
    public boolean isNegative=false;

    @SerializedName("Note")
    public String note;

    @SerializedName("ToolTip")
    public String toolTip;

}
