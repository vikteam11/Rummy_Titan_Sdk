package com.rummytitans.sdk.cardgame;


import android.app.Activity;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.appsflyer.AppsFlyerConversionListener;
import com.appsflyer.AppsFlyerLib;
import com.appsflyer.attribution.AppsFlyerRequestListener;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.gson.Gson;
import com.rummytitans.sdk.cardgame.analytics.AbstractApplicationLifeCycleHelper;
import com.rummytitans.sdk.cardgame.analytics.AnalyticsHelper;
import com.rummytitans.sdk.cardgame.data.SharedPreferenceStorageRummy;

import java.lang.ref.WeakReference;
import java.util.Map;

import dagger.hilt.android.HiltAndroidApp;

public class MainApplication extends Application implements LifecycleEventObserver {

    public static WeakReference<Activity> forgroundActivityRef = null;
    private static GoogleAnalytics sAnalytics;
    public static AnalyticsHelper analyticsHelper;
    public boolean isNotificationApiUpdateRequire = false;
    public boolean showGameAnimation = false;
    public static ClipboardManager manager;
    public static String appUrl = "";
    private AbstractApplicationLifeCycleHelper applicationLifeCycleHelper;
    private final String AF_DEV_KEY = "RSVXVunkaMb3wCuLjhdRXE";
    public static boolean openGameAppFlyer = false;
    public boolean isAppBackgroud = false;
    public boolean isFromBackground = false;

   /* @Override
    protected AndroidInjector<? extends DaggerApplication> applicationInjector() {
        //return DaggerAppComponent.builder().create(this);
        return null;
    }*/

    @Override
    public void onCreate() {
        super.onCreate();
        //FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG);
        manager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        sAnalytics = GoogleAnalytics.getInstance(this);
        sAnalytics.enableAutoActivityReports(this);

        analyticsHelper = new AnalyticsHelper(this, new Gson());
        sAnalytics.newTracker(R.xml.global_tracker).enableAutoActivityTracking(true);
        initAppsFlyer();
        //initOneSignal();
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);

        applicationLifeCycleHelper = new AbstractApplicationLifeCycleHelper() {
            @Override
            public void onActivityPaused(@NonNull Activity activity) {
                super.onActivityPaused(activity);

            }

            @Override
            protected void applicationEnteredForeground() {
              //  analyticsHelper.getPinpointHelper().startSession();
            }

            @Override
            protected void applicationEnteredBackground() {
               // analyticsHelper.getPinpointHelper().stopSession();
            }

            @Override
            public void onActivityCreated(@NonNull Activity activity, @androidx.annotation.Nullable Bundle bundle) {
                super.onActivityCreated(activity, bundle);
                forgroundActivityRef = new WeakReference(activity);
            }

            @Override
            public void onActivityResumed(@NonNull Activity activity) {
                super.onActivityResumed(activity);
                forgroundActivityRef = new WeakReference(activity);
            }
        };
        registerActivityLifecycleCallbacks(applicationLifeCycleHelper);
        createNotificationChannel();
    }

    /*private void initOneSignal(){
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE);
        OneSignal.initWithContext(this);
        OneSignal.setAppId("265de366-b8ec-4cac-93f0-2b17c532ce98");
        OneSignal.promptForPushNotifications();
    }*/

    public static void fireEvent(String key, Bundle bundle) {
        analyticsHelper.fireEvent(key, bundle);
    }

    public void initAppsFlyer() {

        AppsFlyerConversionListener conversionListener = new AppsFlyerConversionListener() {
            @Override
            public void onConversionDataSuccess(Map<String, Object> conversionData) {
                boolean isFirstLaunch = (boolean) conversionData.get("is_first_launch");

                for (String attrName : conversionData.keySet()) {
                    Log.d("LOG_TAG", "onConversionDataSuccess: " + attrName + " = " + conversionData.get(attrName));

                    if (attrName.equalsIgnoreCase("campaignId")) {
                        SharedPreferenceStorageRummy pref = new SharedPreferenceStorageRummy(getApplicationContext());
                        pref.setCampaignId(conversionData.get(attrName).toString());
                    }

                    if (attrName.equalsIgnoreCase("refercode")) {
                        SharedPreferenceStorageRummy pref = new SharedPreferenceStorageRummy(getApplicationContext());
                        pref.setReferCode(conversionData.get(attrName).toString());
                    }

                    if (attrName.equalsIgnoreCase("deep_link_value") && isFirstLaunch) {
                        String deeplinkValue = conversionData.get(attrName).toString();
                        handleAppsFlyerDeepLink(deeplinkValue);
                    }
                }
            }

            @Override
            public void onConversionDataFail(String errorMessage) {
                Log.d("LOG_TAG", "error onConversionDataFail: " + errorMessage);
            }

            @Override
            public void onAppOpenAttribution(Map<String, String> attributionData) {
                for (String attrName : attributionData.keySet()) {
                    Log.d("LOG_TAG", "onAppOpenAttribution: " + attrName + " = " + attributionData.get(attrName));
                }
            }

            @Override
            public void onAttributionFailure(String errorMessage) {
                Log.d("LOG_TAG", "error onAttributionFailure : " + errorMessage);
            }
        };

        AppsFlyerLib appsFlyerLib = AppsFlyerLib.getInstance();
        appsFlyerLib.init(AF_DEV_KEY, conversionListener, this);
        appsFlyerLib.start(this, AF_DEV_KEY, new AppsFlyerRequestListener() {
            @Override
            public void onSuccess() {
                Log.d("AppflyerStatus", " onSuccess : ");
            }

            @Override
            public void onError(int i, @NonNull String s) {
                Log.d("AppflyerStatus", "onError   "+i+" message "+s);
            }
        });
        appsFlyerLib.setDebugLog(true);

        appsFlyerLib.subscribeForDeepLink(deepLinkResult -> {
            String deeplinkValue = deepLinkResult.getDeepLink().getDeepLinkValue();
            handleAppsFlyerDeepLink(deeplinkValue);
        });

    }

    private void handleAppsFlyerDeepLink(String deeplinkValue) {
       // Toast.makeText(this,"AppsFlyer DeepLinks-->",Toast.LENGTH_LONG).show();
        Log.d("LOG_TAG", "hanadle "+deeplinkValue);
        if (deeplinkValue != null ) {

            if (deeplinkValue.contains("game")) {
                Log.d("AppsFlyer DeepLinks-->", deeplinkValue);
                openGameAppFlyer = true;

                SharedPreferenceStorageRummy pref = new SharedPreferenceStorageRummy(getApplicationContext());
                pref.setAppsFlyerDeepLink(deeplinkValue);

        }
        }
    }

    @Override
    public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
        if (event.name().equals("ON_PAUSE")){
            isAppBackgroud = true;
            isFromBackground = false;
        }else if(event.name().equals("ON_START")){
            isAppBackgroud = false;
            isFromBackground = true;
        }
    }
    public void createNotificationChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager =(NotificationManager)
                    getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel(
                    getString(R.string.app_name_rummy),
                    "RummyTitans",
                    NotificationManager.IMPORTANCE_DEFAULT);

            channel.setDescription("RummyTitans");
            channel.setShowBadge(true);
            channel.canShowBadge();
            channel.enableLights(true);
            channel.setLightColor(Color.BLUE);
            channel.enableVibration(true);
            notificationManager.createNotificationChannel(channel);
        }
    }
}