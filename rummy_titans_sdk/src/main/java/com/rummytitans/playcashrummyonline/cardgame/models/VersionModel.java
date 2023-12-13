package com.rummytitans.playcashrummyonline.cardgame.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class VersionModel implements Serializable {

    @SerializedName("ClearData")
    public boolean ClearData;
    @SerializedName("Message")
    public String Message;
    @SerializedName("MessageShow")
    public boolean showUpdateOnSplash;
    @SerializedName("WebURl")
    public String WebURl;
    @SerializedName("Title")
    public String Title;
    @SerializedName("DownLoadURl")
    public String DownLoadURl;
    @SerializedName("ForceUpdate")
    public boolean ForceUpdate;
    @SerializedName("RunOnWeb")
    public boolean RunOnWeb;
    @SerializedName("IsAppUpdate")
    public boolean IsAppUpdate;
    @SerializedName("SplashImage")
    public String SplashImage;
    @SerializedName("BaseUrl")
    public String BaseUrl;
    @SerializedName("BaseUrl2")
    public String BaseUrl2;
    @SerializedName("AvailableVersion")
    public String newAppVersionCode;
    @SerializedName("DelayLocationMinutes")
    public int delayLocationMinutes;

    @SerializedName("isTrueCallerEnable")
    public boolean isTrueCallerEnable;
    @SerializedName("LoginAuthToken")
    public String LoginAuthTokan;
    @SerializedName("LogsEnable")
    public boolean isLogsEnable;
    @SerializedName("UpdateFrom")
    public int playStoreApkUpdateFrom;
    @SerializedName("VerifyAadharWithOtp")
    public Boolean VerifyAadharWithOtp=false;
    public boolean enableAppUpdateBtn=false;

    @SerializedName("Updates")
    public List<CarouselModel> Updates;

    @SerializedName("UpdateType")
    public int UpdateType;

    public int UPDATE_FROM_WEBSITE=1;
    public int UPDATE_FROM_APP_STORE=2;
    public int UPDATE_FROM_IN_APP_UPDATE=3;


    public List<CarouselModel> getUpdateDetailList(){
            return Updates;
    }
}
