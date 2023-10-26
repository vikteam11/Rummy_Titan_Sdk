package com.rummytitans.playcashrummyonline.cardgame.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class TransactionModel implements Serializable {
    @SerializedName("Date")
    public String Date;
    public static byte SHOW_VIEW=1;
    public static byte HIDE_VIEW=0;

    @SerializedName("Transaction")
    public ArrayList<TransactionListModel> Transaction;

    public class TransactionListModel implements Serializable {
        @SerializedName("GameId")
        public String gameId;
        @SerializedName("TaxInvoice")
        public Boolean taxInvoice;
        @SerializedName("GamePlayUrl")
        public String GamePlayUrl;

        @SerializedName("Url")
        public String Url;
        @SerializedName("Cr")
        public double Cr;
        @SerializedName("Dr")
        public double Dr;
        @SerializedName("Description")
        public String Description;
        @SerializedName("Dateon")
        public String Dateon;
        @SerializedName("ID")
        public String ID;
        @SerializedName("Title")
        public String Title;
        @SerializedName("Status")
        public String Status;

        @SerializedName("Dateonadd")
        public String Dateonadd;
        @SerializedName("Amount")
        public double Amount;
        @SerializedName("TxnId")
        public String TxnId;
        @SerializedName("UserJoinedMatchId")
        public String UserJoinedMatchId;
        @SerializedName("LeagueId")
        public String LeagueId;
        @SerializedName("TranType")
        public int TranType;
        @SerializedName("IsCr")
        public int IsCr;
        @SerializedName("Unutilized")
        public double Unutilized;
        @SerializedName("Wiining")
        public double Wiining;
        @SerializedName("Myteam11Credit")
        public double Myteam11Credit;
        @SerializedName("BankAccount")
        public String BankAccount;
        @SerializedName("StatusCode")
        public int statusCode;

        public String transactionStatus;
        public String date;
        public boolean isDateShow = false;
        public boolean isDetailAvailable=false;
        public Byte showDetailView = HIDE_VIEW;
        public int withdrawalStatusColor;

        @SerializedName("TransactionBreakup")
        public List<KeyValuePairModel> transactionBreakup;

        @SerializedName("TransactionDetails")
        public List<KeyValuePairModel> transactionDetails;

        @SerializedName("ClosingBalance")
        public List<KeyValuePairModel> closingBalance;

        public Boolean isRummyTransaction(){
            return Title.toLowerCase(Locale.ROOT).contains("rummy");
        }

        public Boolean isDepositTransaction(){
            return TranType ==1;//TranType==1; //1 for deposit transaction
        }

        public Boolean transactionBreakupAvailable(){
            return transactionBreakup!=null && transactionBreakup.size()>0;
        }
        public Boolean transactionDetailAvailable(){
            return transactionDetails!=null && transactionDetails.size()>0;
        }
        public Boolean closingBalanceAvailable(){
            return closingBalance!=null && closingBalance.size()>0;
        }

    }
}

