
package com.rummytitans.sdk.cardgame.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

@SuppressWarnings("unused")
public class GamesResponseModel implements Serializable{
    @SerializedName("CategoryName")
    public String catName;
    @SerializedName("GameList")
    public ArrayList<GamesModel> games;
    public static class GamesModel  implements Serializable {

        @SerializedName("GameId")
        public int gameId;
        @SerializedName("GameMessage")
        public String gameMessage;
        @SerializedName("GameType")
        public byte gameType;
        @SerializedName("GameUrl")
        public String gameUrl;
        @SerializedName("Imageicon")
        public String imageicon;
        @SerializedName("IsDisable")
        public Boolean isDisable;
        @SerializedName("Isupdate")
        public Boolean isupdate;
        @SerializedName("Myloby")
        public Boolean myloby;
        @SerializedName("Name")
        public String name;
        @SerializedName("Orientation")
        public String Orientation;
        @SerializedName("PackageName")
        public String PackageName;
        @SerializedName("BedgeText")
        public String BedgeText;
        public String parentCategoryName;

    }
}

