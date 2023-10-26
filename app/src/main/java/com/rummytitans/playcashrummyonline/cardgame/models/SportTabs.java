package com.rummytitans.playcashrummyonline.cardgame.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class SportTabs implements Serializable {

    @SerializedName("Name")
    public String Name;
    @SerializedName("Id")
    public int Id;
    @SerializedName("isDefault")
    public boolean isDefault;
    public boolean isSelected = false;
    @SerializedName("iconUrl")
    public String iconUrl;
    @SerializedName("SelectIcanURL")
    public String selectedIconUrl;
    @SerializedName("isNew")
    public boolean isNew;
    @SerializedName("defaultvale")
    public int defaultvale;

    public SportTabs() {
    }

    public SportTabs(int id, String name, boolean isSelected) {
        Name = name;
        Id = id;
        this.isSelected = isSelected;
    }
}
