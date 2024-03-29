package com.rummytitans.playcashrummyonline.cardgame.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class BaseModelGame<T> implements Serializable {
    public String Message;
    public boolean TokenExpire;
    public boolean Status;
    public int Code;
    @SerializedName("Result")
    public T Response;
    public boolean IsAutoScrollHeader;
    public long ScrollTime = 0L;

    public BaseModelGame() {

    }

    public boolean checkStatus(){
        return Code==-1;
    }

    @Override
    public String toString() {
        return super.toString() +
                Message + "\n" + TokenExpire + "\n" + Status + "\n" + Response;
    }
}
