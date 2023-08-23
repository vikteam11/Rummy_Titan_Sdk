package com.rummytitans.playcashrummyonline.cardgame.models;

import com.google.gson.annotations.SerializedName;

public class HelpDeskModel {
    @SerializedName("FaqStaus")
    public boolean FaqStaus;
    @SerializedName("ChatStaus")
    public boolean ChatStaus;
    @SerializedName("MobileStaus")
    public boolean MobileStaus;
    @SerializedName("EmailStatus")
    public boolean EmailStatus;
    @SerializedName("MessageStatus")
    public boolean MessageStatus;
    @SerializedName("Message")
    public String Message;
    @SerializedName("Mobile")
    public String Mobile;
    @SerializedName("Email")
    public String Email;
}
