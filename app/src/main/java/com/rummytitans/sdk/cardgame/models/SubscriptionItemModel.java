package com.rummytitans.sdk.cardgame.models;

import com.google.gson.annotations.SerializedName;

public class SubscriptionItemModel {

    @SerializedName("DESCRIPTION")
    public String mDESCRIPTION;
    public Boolean Status;
    @SerializedName("Title")
    public String mTitle;
    @SerializedName("TopicName")
    public String mTopicName;

}