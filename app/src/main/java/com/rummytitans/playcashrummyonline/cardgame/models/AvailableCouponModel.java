package com.rummytitans.playcashrummyonline.cardgame.models;

import com.google.gson.annotations.SerializedName;

public class AvailableCouponModel {
    @SerializedName("ID")
    public int id;
    @SerializedName("Code")
    public String Code;
    @SerializedName("Description")
    public String Description;
    @SerializedName("Expiry")
    public String Expiry;
    public boolean isSelected = false;
    public String selectedColor = "";
}
