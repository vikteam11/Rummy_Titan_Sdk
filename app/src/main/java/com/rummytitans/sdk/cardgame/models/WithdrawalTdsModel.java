package com.rummytitans.sdk.cardgame.models;

import com.google.gson.annotations.SerializedName;

public class WithdrawalTdsModel {

    @SerializedName("Title")
    public String title;

    @SerializedName("Value")
    public String value;

    @SerializedName("Amount")
    public Double amount;

    @SerializedName("Amount2")
    public Double amount2;
    public boolean isNegative=false;

    @SerializedName("Note")
    public String note;
    @SerializedName("Value2")
    public String value2;

    @SerializedName("ToolTip")
    public String toolTip;

    @SerializedName("Type")
    public int type;

}
