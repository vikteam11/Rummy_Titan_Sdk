package com.rummytitans.sdk.cardgame.models;

import com.google.gson.annotations.SerializedName;

public class JoinLobbyModel {

    @SerializedName("gameplayUrl")
    public String gameplayUrl;

    @SerializedName("matchFound")
    public boolean matchFound;

}
