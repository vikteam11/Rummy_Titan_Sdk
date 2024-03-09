package com.rummytitans.sdk.cardgame.models;

import androidx.annotation.Keep;

import java.io.Serializable;
@Keep
public class BaseModelRummy<T> implements Serializable {
    public String Message;
    public String message;
    public boolean TokenExpire;
    public boolean IsTimesUp;

    public boolean IsSuspended;
    public boolean tokenExpire;
    public boolean Status;
    public boolean status;
    public String CurrentDate;
    public T Response;
    public T response;
    public boolean IsAutoScrollHeader;
    public long ScrollTime = 0L;
    public String PollKey;
    public int Maxteam;
    public int totalWinningAmount;
    public int Fee;
    public boolean MessageStatus;
    public String MessageText;
    public boolean MultipleJoinAllow;
    public boolean TimesUp;
    public int QuestionTime;
    public boolean IsSubmit;
    public String updateMessage;
    public boolean isUpdate;
    public boolean IsRegister;
    public boolean IsLocationAPIRequired;
    public Integer Minutes;
    public String StateName;
    public String NotificationKey;
    public String TnxId;
    public String Title;
    public String ReauestDate;
    public String SharingUrl;
    public String SharingMessage;
    public int AllowJoinCount;
    public String FooterImage;
    public String FooterLink;
    public String PredictionIcon;
    public String PredictionUrl;
    public String apkNames;
    public String Description;
    public boolean IsAddressVerified;
    public String Offer;
    public double AddCashAmount;
    public boolean IsBanned;
    public boolean AllowRummyGame;
    public int DelayMinutes;

    public BaseModelRummy() {

    }

    @Override
    public String toString() {
        return super.toString() +
                Message + "\n" + TokenExpire + "\n" + Status + "\n" + Response;
    }
}
