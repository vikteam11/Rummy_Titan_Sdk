package com.rummytitans.playcashrummyonline.cardgame.models;


import com.google.gson.annotations.SerializedName;

import java.util.List;

public class LevelModel {

    @SerializedName("taskLists")
    public List<LevelStepModel> taskLists;
    @SerializedName("unlockPrizes")
    public List<UnlockPrizes> unlockPrizes;
    @SerializedName("IsShow")
    public boolean IsShow;
    @SerializedName("Url")
    public String Url;
    @SerializedName("PointIsShow")
    public boolean PointIsShow;
    @SerializedName("PointUrl")
    public String PointUrl;
    @SerializedName("Points")
    public double Points;
    @SerializedName("Message")
    public String Message;
    @SerializedName("Nextlevels")
    public int Nextlevels;
    @SerializedName("CurrentLeves")
    public int CurrentLevels;

    public static class UnlockPrizes {
        @SerializedName("Message")
        public String Message;
    }

    public static class LevelStepModel {
        public String Message;
        public boolean IsCompleted;
    }
}