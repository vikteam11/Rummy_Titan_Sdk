
package com.rummytitans.sdk.cardgame.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class MyContactsResponseModel {

    @SerializedName("CurrentDate")
    private Object mCurrentDate;
    @SerializedName("IsAutoScrollHeader")
    private Boolean mIsAutoScrollHeader;
    @SerializedName("Maxteam")
    private Long mMaxteam;
    @SerializedName("Message")
    private String mMessage;
    @SerializedName("NotificationKey")
    private Object mNotificationKey;
    @SerializedName("PollKey")
    private Object mPollKey;
    @SerializedName("Response")
    private ArrayList<ContactModel> mContactList;
    @SerializedName("Status")
    private Boolean mStatus;
    @SerializedName("TokenExpire")
    private Boolean mTokenExpire;

    public Object getCurrentDate() {
        return mCurrentDate;
    }

    public void setCurrentDate(Object currentDate) {
        mCurrentDate = currentDate;
    }

    public Boolean getIsAutoScrollHeader() {
        return mIsAutoScrollHeader;
    }

    public void setIsAutoScrollHeader(Boolean isAutoScrollHeader) {
        mIsAutoScrollHeader = isAutoScrollHeader;
    }

    public Long getMaxteam() {
        return mMaxteam;
    }

    public void setMaxteam(Long maxteam) {
        mMaxteam = maxteam;
    }

    public String getMessage() {
        return mMessage;
    }

    public void setMessage(String message) {
        mMessage = message;
    }

    public Object getNotificationKey() {
        return mNotificationKey;
    }

    public void setNotificationKey(Object notificationKey) {
        mNotificationKey = notificationKey;
    }

    public Object getPollKey() {
        return mPollKey;
    }

    public void setPollKey(Object pollKey) {
        mPollKey = pollKey;
    }

    public ArrayList<ContactModel> getList() {
        return mContactList;
    }

    public void setList(ArrayList<ContactModel> list) {
        mContactList = list;
    }

    public Boolean getStatus() {
        return mStatus;
    }

    public void setStatus(Boolean status) {
        mStatus = status;
    }

    public Boolean getTokenExpire() {
        return mTokenExpire;
    }

    public void setTokenExpire(Boolean tokenExpire) {
        mTokenExpire = tokenExpire;
    }

}
