package com.rummytitans.sdk.cardgame.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class WinningConversionDepositModel implements Serializable {

    @SerializedName("Amount")
    public double amount;

    @SerializedName("Bonus")
    public double bonus;
    @SerializedName("BonusPercentage")
    public double bonusPercentage;

}
