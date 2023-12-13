package com.rummytitans.playcashrummyonline.cardgame.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;

import java.util.Locale;

import com.rummytitans.playcashrummyonline.cardgame.R;

public class LocaleHelper {

    private static final String SELECTED_LANGUAGE = "Locale.Helper.Selected.Language";

    private static final String SELECTED_LANGUAGE_NAME = "Locale.Helper.Selected.Language.Name";
    private static final String PREFS_NAME = "rummytitans";

    public static Context onAttach(Context context) {
        String lang = getPersistedData(context, Locale.getDefault().getLanguage());
        String name = getPersistedDataName(context, Locale.getDefault().getDisplayLanguage(Locale.US));
        return setLocale(context, lang, name);
    }

    public static Context onAttach(Context context, String defaultLanguage, String defaultLanguageName) {
        String lang = getPersistedData(context, defaultLanguage);
        String langname = getPersistedDataName(context, defaultLanguageName);
        return setLocale(context, lang, langname);
    }

    public static String getLanguage(Context context) {
        return getPersistedData(context, Locale.getDefault().getLanguage());
    }

    public static String getLanguageName(Context context) {
        return getPersistedDataName(context, Locale.getDefault().getDisplayLanguage(Locale.US));
    }

    public static Context setLocale(Context context, String languagecode, String languagename) {
        persist(context, languagecode, languagename);
        return updateResourcesLegacy(context, languagecode);
    }

    public static Context setLocale(Context context) {
        String languagecode = context.getString(R.string.english_code);
        String languagename = context.getString(R.string.english);
        persist(context, languagecode, languagename);
        return updateResourcesLegacy(context, languagecode);
    }

    private static String getPersistedData(Context context, String defaultLanguage) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getString(SELECTED_LANGUAGE, defaultLanguage);
    }

    private static String getPersistedDataName(Context context, String defaultLanguage) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getString(SELECTED_LANGUAGE_NAME, defaultLanguage);
    }

    private static void persist(Context context, String languagecode, String languagename) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(SELECTED_LANGUAGE, languagecode);
        editor.putString(SELECTED_LANGUAGE_NAME, languagename);
        editor.apply();
    }

    private static Context updateResourcesLegacy(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();
        configuration.locale = locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
            configuration.setLayoutDirection(locale);
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
        return context;
    }
}