package com.rummytitans.sdk.cardgame.models;

import com.google.gson.annotations.SerializedName;

public class WalletBalanceModel {

    @SerializedName("Balance")
    public String Balance;

    @SerializedName("Tickets")
    public int Tickets;

    @SerializedName("ReferTabMesage")
    public String ReferTabMesage;

    @SerializedName("WalletTabMesage")
    public String WalletTabMesage;

    public boolean showWalletBadge(){
        return WalletTabMesage != null &&  !WalletTabMesage.equalsIgnoreCase("");
    }
    public boolean showReferBadge(){
        return ReferTabMesage != null &&  !ReferTabMesage.equalsIgnoreCase("");
    }


}
