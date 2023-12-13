package com.rummytitans.playcashrummyonline.cardgame.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MatchModel implements Serializable, Cloneable {

    @SerializedName("Message")
    public String Message;
    @SerializedName("IsShow")
    public boolean IsShow;
    @SerializedName("IsMessage")
    public boolean IsMessage;
    @SerializedName("TeamJoin")
    public int TeamJoin;
    @SerializedName("LeaugeJoin")
    public int LeaugeJoin;
    @SerializedName("IsAppType")
    public int IsAppType;
    @SerializedName("StatusInfo")
    public String StatusInfo;
    @SerializedName("MatchType")
    public int MatchType;
    @SerializedName("RelatedName")
    public String RelatedName;
    @SerializedName("CurrentTime")
    public String CurrentTime;
    @SerializedName("StartDate")
    public String StartDate;
    @SerializedName("Status")
    public String Status;
    public String parentStatus;
    @SerializedName("MatchId")
    public int MatchId;
    public int parentMatchId;
    @SerializedName("TeamFlag3")
    public String TeamFlag3;
    @SerializedName("TeamFlag2")
    public String TeamFlag2;
    @SerializedName("TeamFlag1")
    public String TeamFlag1;
    @SerializedName("TeamName3")
    public String TeamName3;
    @SerializedName("TeamName2")
    public String TeamName2;
    @SerializedName("TeamName1")
    public String TeamName1;
    @SerializedName("MegaLeague")
    public String MegaLeague;

    @SerializedName("TeamFullName1")
    public String TeamFullName1;
    @SerializedName("TeamFullName2")
    public String TeamFullName2;
    @SerializedName("TeamFullName3")
    public String TeamFullName3;
    @SerializedName("TeamColor1")
    public String TeamColor1;
    @SerializedName("TeamColor2")
    public String TeamColor2;
    @SerializedName("TeamColor3")
    public String TeamColor3;
    @SerializedName("MatchRefenceID")
    public int MatchRefenceID;

    @SerializedName("PowerBy")
    public String PowerBy;

    @SerializedName("TolTipMessage")
    public String TolTipMessage;

    @SerializedName("PowerByURl")
    public String PowerByURl;

    @SerializedName("IsRealMessage")
    public String IsRealMessage;

    @SerializedName("SignalR")
    public Boolean SignalR = false;
    @SerializedName("IsPrediction")
    public Boolean IsPrediction;


    @SerializedName("LineUpOut")
    public boolean LineUpOut;

    @SerializedName("TotalQuestion")
    public String TotalQuestion;

    @SerializedName("QuizImage")
    public String QuizImage;

    @SerializedName("QuizEndtDate")
    public String QuizEndtDate;

    @SerializedName("IsReal")
    public Boolean isRealTime;

    @SerializedName("Is3TC")
    public Boolean Is3TC;

    @SerializedName("IsImportant")
    public Boolean IsImportant;


    @SerializedName("SignalRURL")
    public String SignalRURL;

    @SerializedName("StatusText")
    public String StatusText;


    @SerializedName("StatusColor")
    public String StatusColor;

    @SerializedName("TeamShareUrl")
    public String TeamShareUrl;
    public Boolean isReminderSet = false;
    @SerializedName("botamBars")
    public ArrayList<Icons> botamBars;

    @SerializedName("IsDetailScoreCard")
    public Boolean IsDetailScoreCard;

    @SerializedName("LiveFantasy")
    public ArrayList<LiveFantasyModel> LiveFantasy;
    public String MatchCloseMessage;

    public boolean scoreboardForLeagueList =false;

    public int selectCurrentIndex=-1;

    public ArrayList<LiveFantasyModel> getUpcomingLiveFantasy(){
        ArrayList<LiveFantasyModel> upcomingMatches=new ArrayList<>();
        for (int i = 0; i < LiveFantasy.size(); i++) {
            if (LiveFantasy.get(i).Status.equalsIgnoreCase("notstarted")) {
                upcomingMatches.add(LiveFantasy.get(i));
            }
        }
        return upcomingMatches;
    }

    //Used to show data in single recyclerview
    public List<MatchModel> liveMatchesList;


    public String getTeamName1() {
        return getTeamName1UpperCase();
    }

    public String getTeamName2() {
        if (null == TeamName2 || TeamName2.isEmpty()) return "";
        else return TeamName2.toUpperCase();
    }

    public String getTeamName3() {
        if (null == TeamName3 || TeamName3.isEmpty()) return "";
        else return TeamName3.toUpperCase();
    }

    public String getTeamName1UpperCase() {
        if (null == TeamName1 || TeamName1.isEmpty()) return "";
        return TeamName1.toUpperCase();
    }

    public String getTeamName2UpperCase() {
        if (null == TeamName2 || TeamName2.isEmpty()) return "";
        else return TeamName2.toUpperCase();
    }


    public static class Icons implements Serializable, Cloneable {
        @SerializedName("IcanURL")
        public String IcanURL;
        @SerializedName("TolTipMessage")
        public String TolTipMessage;
        @SerializedName("Url")
        public String url;
        @SerializedName("IsExternal")
        public boolean IsExternal;
        @SerializedName("IsPowered")
        public boolean IsPowered;
    }

    public static class LiveFantasyModel implements Serializable, Cloneable {
        @SerializedName("MatchId")
        public int MatchId;
        @SerializedName("IconUrl")
        public String IconUrl;
        @SerializedName("Title")
        public String Title;
        @SerializedName("Description")
        public String Description;
        @SerializedName("Message")
        public String Message;
        @SerializedName("MatchCloseMessage")
        public String MatchCloseMessage;
        @SerializedName("Status")
        public String Status;
    }

    public boolean isMatchLiveFantasy() {
        try {
            return LiveFantasy != null && LiveFantasy.size() != 0;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean showLiveFantasyTabs() {
        try {
            if (isMatchLiveFantasy())
                return LiveFantasy.size() > 0;
            else
                return false;
        } catch (Exception e) {
            return false;
        }
    }

    public MatchModel getCloneModel() {
        try {
            return (MatchModel) clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void updateMatchWithFantasy(LiveFantasyModel fantasyModel) {
        parentMatchId=MatchId;
        parentStatus=Status;
        MatchId = fantasyModel.MatchId;
        IsMessage = !fantasyModel.Message.equals("");
        Message = fantasyModel.Message;
        if (fantasyModel.Status != null && !fantasyModel.Status.isEmpty())
            Status = fantasyModel.Status;
        MatchCloseMessage = fantasyModel.MatchCloseMessage;
    }

    public boolean isFullMatch(int matchId) {
        try {
            if (LiveFantasy == null || LiveFantasy.isEmpty())
                return true;
             return matchId==parentMatchId;
              /* return   matchId == LiveFantasy.get(0).MatchId;
             else
                return parentMatchId == LiveFantasy.get(0).MatchId;*/
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    public MatchModel getUpdatedMatchModel(int requestedMatchId) {
        if (requestedMatchId == MatchId)
            return this;
        else {
            for (int i = 0; i < LiveFantasy.size(); i++) {
                if (requestedMatchId == LiveFantasy.get(i).MatchId) {
                    updateMatchWithFantasy(LiveFantasy.get(i));
                    break;
                }
            }
            return this;
        }
    }

    public int getIndex(int matchId) {
        int index = 0;
        if (isMatchLiveFantasy()) {
            ArrayList<LiveFantasyModel> upcoming=getUpcomingLiveFantasy();
            for (int i = 0; i < upcoming.size(); i++) {
                if (matchId == upcoming.get(i).MatchId) {
                    index = i;
                    break;
                }
            }
        }
        return index;
    }

    public String getLiveFantasyMatchDescription(){
        if(isMatchLiveFantasy()){
            String des = "";
            for (LiveFantasyModel matchModel : getUpcomingLiveFantasy()) {
                if(matchModel.MatchId == MatchId){
                    des = matchModel.Description.equals("") ? matchModel.Title : matchModel.Description;
                    break;
                }
            }
            return des;
        }else {
            return "";
        }
    }

    public String getLiveFantasySlot(){
        if(isMatchLiveFantasy()){
            String des = "";
            for (LiveFantasyModel matchModel : getUpcomingLiveFantasy()) {
                if(matchModel.MatchId == MatchId){
                    des = matchModel.Title;
                    break;
                }
            }
            return des;
        }else {
            return "";
        }
    }

    public String getLiveFantasyMatchType(){
        if(isMatchLiveFantasy()){
            return "LiveFantasy";
        }else {
            return "";
        }
    }
}


