package com.rummytitans.sdk.cardgame.models;

public class AvatarModel {
    public int imageUrl;
    public boolean isSelected;

    public AvatarModel(int imageUrl, boolean isSelected) {
        this.imageUrl = imageUrl;
        this.isSelected = isSelected;
    }
}
