package com.rummytitans.playcashrummyonline.cardgame.models;


import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class WalletInfoModel {


    @SerializedName("Offer")
    public List<Offer> Offer;
    @SerializedName("Balance")
    public Balance Balance;
    @SerializedName("UserInfo")
    public UserInfo UserInfo;
    @SerializedName("ScratchCard")
    public int ScarchCard;
    @SerializedName("AddCashAmount")
    public double AddCashAmount;

    @SerializedName("GameTicketMesage")
    public String GameTicketMesage;
    @SerializedName("GameTicketMesageColor")
    public String GameTicketMesageColor;

    @SerializedName("BankVerify")
    public boolean BankVerify;
    @SerializedName("PanVerify")
    public boolean PanVerify;
    @SerializedName("AddressVerify")
    public boolean AddressVerify;


    public static class Offer {
        @SerializedName("Image")
        public String Image;
        @SerializedName("Desc")
        public String Desc;
        @SerializedName("Title")
        public String Title;
        @SerializedName("IsShow")
        public boolean IsShow;
        @SerializedName("Tc")
        public String Tc;

    }

    public static class Balance implements Serializable {
        @SerializedName("TotalAmount")
        public double TotalAmount;
        @SerializedName("SignUpBonus")
        public double SignUpBonus;
        @SerializedName("Bonus")
        public double Bonus;
        @SerializedName("Winning")
        public double Winning;
        @SerializedName("Unutilized")
        public double Unutilized;
        @SerializedName("DailyBonus")
        public double DailyBonus;

        public int ScrachCardsCoun=9;
        @SerializedName("BonusList")
        public ArrayList<WalletBonesModel> BonusList;
    }

    public static class UserInfo {
        @SerializedName("AvtarId")
        public int AvtarId;
        @SerializedName("Name")
        public String Name;
        @SerializedName("WidText")
        public String WidText;
    }

    public static class WalletBonesModel implements Serializable {
        @SerializedName("Val")
        public String value;
        @SerializedName("Name")
        public String name;
        @SerializedName("WalletType")
        public Integer walletType;
        @SerializedName("isbouns")
        public boolean isbouns;
        public int colorCode;
        public String title;
        public boolean isArrowUpDown = false;

        public boolean isCreditBonus(){
            return walletType == 2;
        }
        public boolean isWinning(){
            return walletType == 0;
        }
        public boolean isDeposit(){
            return walletType == 1;
        }
    }
}
