package com.rummytitans.sdk.cardgame.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;

public class RummyLobbyModel implements Serializable {

    @Expose
    @SerializedName("StakeId")
    public String StakeId;
    @Expose
    @SerializedName("CategoryId")
    public String CategoryId;
    @Expose
    @SerializedName("SubCategoryId")
    public String SubCategoryId;
    @Expose
    @SerializedName("BuyIn")
    public double BuyIn;
    @Expose
    @SerializedName("MaxWin")
    public double MaxWin;

    @Expose
    @SerializedName("MaxPlayer")
    public int MaxPlayer;

    @Expose
    @SerializedName("ActivePlayers")
    public long ActivePlayers;

    @Expose
    @SerializedName("Bonus")
    public int Bonus;

    @Expose
    @SerializedName("DiscountAmt")
    public double DiscountAmt;

    @Expose
    @SerializedName("Selected")
    public boolean Selected;

    @Expose
    @SerializedName("IsDiscount")
    public boolean IsDiscount;

    @SerializedName("Tag")
    public List<String> Tag;

    @Expose
    @SerializedName("CardVarient")
    public int CardVariant;

    public String CatName;

    @Expose
    @SerializedName("Type")
    public String Type;

    @Expose
    @SerializedName("BonusText")
    public String BonusText;

    public boolean showPointIcon(){
        return Type.equalsIgnoreCase("Point") && tagShow() ;
    }

    public boolean isPointCategory(){
        return Type.equalsIgnoreCase("Point") ;
    }

    public boolean tagShow(){
        return Tag != null && Tag.size()>0;
    }

    public String tags(){
        return tagShow() ? Tag.get(0) :"";
    }

    public String formatAmount(double amount){
        String numberString = "";
        if (Math.abs(amount / 1000000) >= 1) {
            numberString = formatValue((amount / 1000000)) + "M";
        }else if (Math.abs(amount / 100000) >= 1) {
            numberString = formatValue((amount / 100000)) + "L";
        } else if (Math.abs(amount / 10000) >= 1) {
            numberString = formatValue((amount / 1000)) + "K";
        } else {
            numberString = formatValue(amount)+"";
        }
        return showPointIcon()? numberString :"₹"+numberString;
    }

    public String formatValue(double amount){
        DecimalFormat format = new DecimalFormat("##.##");
        format.setRoundingMode(RoundingMode.DOWN);
        return  format.format(amount);
    }

    public String joinButtonText(){
        String joinText = "";
        if(tagShow()){
            joinText = "Play";
        } else if (DiscountAmt <= 0.0) {
            joinText = "Free";
        }else {
            joinText = formatAmount(DiscountAmt);
        }
        return joinText;
    }
}
