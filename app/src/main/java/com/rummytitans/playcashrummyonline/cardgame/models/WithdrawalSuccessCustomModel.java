package com.rummytitans.playcashrummyonline.cardgame.models;

import java.io.Serializable;

//custom model used at frontEnd Side
public class WithdrawalSuccessCustomModel implements Serializable {
    public String ID;
    public String Message;
    public String AccountNo;
    public String TnxId;
    public String RequestDate;
    public Double AmountWithdrawal;
    public String withdrawalHeader;
    public String AmountAvailable;
    public String colorCode;//frontEnd use
    public boolean withdrawalStatus;
}
