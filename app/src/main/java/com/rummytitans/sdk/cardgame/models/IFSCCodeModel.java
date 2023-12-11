package com.rummytitans.sdk.cardgame.models;

import com.google.gson.annotations.SerializedName;

public class IFSCCodeModel {

    @SerializedName("IFSC")
    public String IFSC;
    @SerializedName("BANKCODE")
    public String BANKCODE;
    @SerializedName("BANK")
    public String BANK;
    @SerializedName("RTGS")
    public boolean RTGS;
    @SerializedName("STATE")
    public String STATE;
    @SerializedName("DISTRICT")
    public String DISTRICT;
    @SerializedName("CITY")
    public String CITY;
    @SerializedName("CONTACT")
    public String CONTACT;
    @SerializedName("ADDRESS")
    public String ADDRESS;
    @SerializedName("BRANCH")
    public String BRANCH;
}
