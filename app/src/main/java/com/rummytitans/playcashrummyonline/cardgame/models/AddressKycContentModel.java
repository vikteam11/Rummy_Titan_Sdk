package com.rummytitans.playcashrummyonline.cardgame.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class AddressKycContentModel implements Serializable {
    @SerializedName("RejectMessage")
    public String RejectMessage="";

    @SerializedName("Title")
    public String Title="";

    @SerializedName("Description")
    public String Description;

    @SerializedName("Notes")
    public List<String> Notes;

    @SerializedName("ManualNotes")
    public List<String> ManualNotes;

    @SerializedName("DLGuildeList")
    public List<String> DLGuildeList;

    @SerializedName("ButtonTittle")
    public String ButtonTittle;

    @SerializedName("IsManualAllow")
    public boolean IsManualAllow;

    @SerializedName("IsManualAadharAllow")
    public boolean IsManualAadharAllow;

    @SerializedName("IsManualDLAllow")
    public boolean IsManualDLAllow;

    @SerializedName("IsInstantAadharAllow")
    public boolean IsInstantAadharAllow;

    @SerializedName("IsOTPAadharAllow")
    public boolean IsOTPAadharAllow;

    @SerializedName("Banner")
    public String BannerUrl;



}
