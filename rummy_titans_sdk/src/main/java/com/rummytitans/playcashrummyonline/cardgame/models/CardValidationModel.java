package com.rummytitans.playcashrummyonline.cardgame.models;

import com.google.gson.annotations.SerializedName;

public class CardValidationModel {

    @SerializedName("Result")
    public Result Result;
    @SerializedName("Message")
    public String Message;
    @SerializedName("Status")
    public boolean Status;

    public static class Result {
        @SerializedName("expiry_status")
        public boolean expiry_status;
        @SerializedName("cvv_status")
        public String cvv_status;
        @SerializedName("cvv_length")
        public int cvv_length;
        @SerializedName("valid")
        public boolean valid;
        @SerializedName("card_type")
        public String card_type;
    }
}
