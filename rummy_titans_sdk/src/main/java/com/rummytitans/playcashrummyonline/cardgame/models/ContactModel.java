
package com.rummytitans.playcashrummyonline.cardgame.models;

import com.google.gson.annotations.SerializedName;

@SuppressWarnings("unused")
public class ContactModel {

    @SerializedName("Email")
    private String mEmail;
    @SerializedName("IsExist")
    private Boolean mIsExist;
    @SerializedName("IsSelected")
    private Boolean mIsSelected;
    @SerializedName("Mobile")
    private String mMobile;
    @SerializedName("Name")
    private String mName;

    private Boolean isSelected;

    public Boolean getSelected() {
        return isSelected;
    }

    public void setSelected(Boolean selected) {
        isSelected = selected;
    }

    public String getEmail() {
        return mEmail;
    }

    public void setEmail(String email) {
        mEmail = email;
    }

    public Boolean getIsExist() {
        return mIsExist;
    }

    public void setIsExist(Boolean isExist) {
        mIsExist = isExist;
    }

    public Boolean getIsSelected() {
        return mIsSelected;
    }

    public void setIsSelected(Boolean isSelected) {
        mIsSelected = isSelected;
    }

    public String getMobile() {
        return mMobile;
    }

    public void setMobile(String mobile) {
        mMobile = mobile;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }
}
