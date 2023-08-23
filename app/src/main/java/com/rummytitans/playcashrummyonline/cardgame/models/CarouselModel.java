package com.rummytitans.playcashrummyonline.cardgame.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class CarouselModel implements Serializable {
    @SerializedName("Title")
    public String Title;
    @SerializedName("Description")
    public String Description;
    @SerializedName("Image")
    public String Image;
    public int ImageIcon=0;
    public String lottieResourceImage;
    public int viewType;
}
