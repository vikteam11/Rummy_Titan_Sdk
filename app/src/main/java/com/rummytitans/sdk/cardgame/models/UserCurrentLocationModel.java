package com.rummytitans.sdk.cardgame.models;

public class UserCurrentLocationModel {

    public String state;
    public String latLog;
    public String country;
    public boolean passOnlyState;

    public UserCurrentLocationModel(){
        state="";
        latLog="";
        country="";
        passOnlyState=false;
    }

    public void resetModel(){
        state="";
        latLog="";
        country="";
        passOnlyState=false;
    }

    public void updateData(String currentState,String currentLatLog,String userCountry) {
        state=currentState;
        latLog=currentLatLog;
        country=userCountry;
        passOnlyState=false;
    }

}
