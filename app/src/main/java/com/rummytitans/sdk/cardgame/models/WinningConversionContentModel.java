package com.rummytitans.sdk.cardgame.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.List;

public class WinningConversionContentModel implements Serializable {

    @SerializedName("Text")
    public List<WinningConversionBenefitModel> benefits;
    @SerializedName("Calculations")
    public List<WinningConversionRangeModel> rangeList;

    public static class WinningConversionBenefitModel{

        @SerializedName("Amount")
        public String amount;
        @SerializedName("Value")
        public String bonusPercentage;

        @SerializedName("Description")
        public String description;

        @SerializedName("Title")
        public String message;

        @SerializedName("Prefix")
        public String prefix;

        public boolean enable;
    }

    public static class WinningConversionRangeModel{
        @SerializedName("MinAmount")
        public double minAmount;
        @SerializedName("MaxAmount")
        public double maxAmount;
        @SerializedName("BonusPercentage")
        public double bonusPercentage;

        public boolean enable;

        public int appColor;


        public String rangeTitle(){
            String min = new DecimalFormat("##.##").format(minAmount);
            String max = new DecimalFormat("##.##").format(maxAmount);
            return "₹"+min +" - ₹"+max;
        }
        public String getDescription(){
            return new DecimalFormat("##.##").format(bonusPercentage)+"% Cash Bonus";
        }

        public boolean selected;
    }
}
