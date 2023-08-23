package com.rummytitans.playcashrummyonline.cardgame.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class AddressResponseModel<T> implements Serializable {
    @SerializedName("Message")
    public String Message="";

    @SerializedName("Title")
    public String Title="";

    @SerializedName("AllowRummyGame")
    public boolean AllowRummyGame;
    @SerializedName("RummyGameUrl")
    public String RummyGameUrl;

    @SerializedName("Status")
    public T Status;
    
}