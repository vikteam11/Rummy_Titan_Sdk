package com.rummytitans.sdk.cardgame.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class RummyCategoryModel {

    @Expose
    @SerializedName("Name")
    public String Name;

    @Expose
    @SerializedName("Type")
    public String Type;

    @Expose
    @SerializedName("CategoryId")
    public String CategoryId;

    @Expose
    @SerializedName("Selected")
    public boolean Selected;

    @Expose
    @SerializedName("ColorCode")
    public String ColorCode = "#c83ecb";

    @Expose
    @SerializedName("IconImg")
    public String IconImg ;

    @Expose
    @SerializedName("RuleUrl")
    public String RuleUrl ;

    @Expose
    @SerializedName("CardVarients")
    public ArrayList<Integer> CardVariants;

    @Expose
    @SerializedName("SelectedCardVarientIndex")
    public Integer SelectedVariant = 0;

    public boolean showVariants(){
        return CardVariants!= null && CardVariants.size()>1;
    }
    public boolean showRule(){
        return RuleUrl!= null && !RuleUrl.equals("");
    }
    public boolean isPointCategory(){
        return Type != null && Type.equalsIgnoreCase("Point") ;
    }

}
