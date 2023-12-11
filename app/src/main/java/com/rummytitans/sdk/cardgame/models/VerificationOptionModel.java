package com.rummytitans.sdk.cardgame.models;

public class VerificationOptionModel {
    public String title;
    public boolean isSelect;
    public int imgId;
    public int documentType;

   public VerificationOptionModel(String title, boolean isSelect, int imgId, int documentType){
        this.title = title;
        this.isSelect = isSelect;
        this.imgId = imgId;
        this.documentType = documentType;
    }
}
