package com.rummytitans.playcashrummyonline.cardgame.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class WithdrawalMethodModel implements Serializable {
    @SerializedName("ID")
    public String ID;
    @SerializedName("Name")
    public String Name;
    @SerializedName("Limit")
    public Double Limit;
    @SerializedName("MinLimit")
    public Double MinLimit;
    @SerializedName("Message")
    public String Message;
    @SerializedName("TnCUrl")
    public String TnCUrl;
    @SerializedName("isSelected")
    public boolean isSelected;
    @SerializedName("Disable")
    public boolean Disable;
    @SerializedName("DisableMessage")
    public String DisableMessage;
    public boolean applicableMethod;
    public boolean isFirstItem;

    public int getMinLimit(){
        return MinLimit.intValue();
    }

    public boolean isWithdrawalInstant(){
        return ID.equals("1") && !Disable;
    }
}
