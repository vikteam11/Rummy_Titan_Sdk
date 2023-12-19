package com.rummytitans.sdk.cardgame.data

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import androidx.annotation.WorkerThread
import androidx.core.content.edit
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.rummytitans.sdk.cardgame.models.LoginResponse
import com.rummytitans.sdk.cardgame.utils.MyConstants
import com.rummytitans.sdk.cardgame.utils.MyConstants.FULL_TIME_TYPE
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Storage for app and user preferences.
 */
interface PreferenceStorageRummy {
    var onSafePlay: Boolean
    var introductionCompleted: Boolean
    var loginCompleted: Boolean
    var notificationsPreferenceShown: Boolean
    var preferToReceiveNotifications: Boolean
    var snackbarIsStopped: Boolean
    var sendUsageStatistics: Boolean
    var loginResponse: String?
    var splashResponse: String?
    var sportSelected: Int?
    var loginType: String?
    var androidId: String?
    var sportResponse: String?
    var pollId: String?
    var languageResponse: String?
    var seletedLanguage: String?
    var firebaseToken: String?
    var isUserFirstTime: Boolean
    var firebaseTokenChanged: Boolean
    var isEmailSent: Boolean
    var isOldUser: Boolean
    var IsContactSaved: Boolean
    var winningMatchIds: String?
    var avtarId: Int?
    var sportsDefaultValue: Int?
    var userName: String?
    var userTeamName: String?
    var regularColor: String?
    var safeColor: String?
    var refertext: String?
    var matchTimeType: Int?
    var splashImageUrl: String?
    var selectedTheme: Int?
    var lastOfferId: Int?
    var notificationCount: Int?
    var isShowCaseDone: Boolean
    var isWalletShowCaseNew: Boolean
    var firstOpen: Boolean
    var campaignId: String?
    var createTeamSettingResponse: String?
    var appUrl: String?
    var appUrl2: String?
    var displayProfileIcon: Boolean
    var showKycOptions: Boolean
    var locationDelay: Long
    var gamePlayUrl: String?
    var referUrl: String?
    var referCode: String?
    var referShareMessage: String?
    var isQuizSoundOn: Boolean
    var isQuizReminder: Boolean
    var quizLanguage: String?
    var quizRemindersIds: String?
    var matchRemindersIds: String?
    var currentDataForQuiz: String?
    var currentDataForCricket: String?
    var todaysCricketMatchIdList: String?
    var quizBackMessage: String?
    var isDeviceIDSet: Boolean
    var isTeamNameSet: Boolean
    var userTotalAmount: String?
    var isUserLoginOnCleverTap: Boolean
    var winnerMatchIdList: String?
    var firstPaidLeagueJoined: Boolean
    var firstFreeLeagueJoined: Boolean
    var firstDeposite: Boolean
    var firstPassPurchased: Boolean
    var loginAuthTokan: String?
    var isFirstSmartechLauch: Boolean
    var advertisingId: String?
    var chatMsgCount: String?
    var gameTimelimit: Long
    var gameTimeMinutes:Int?
    var homeCurrentTab: Int?
    var userStateName: String?
    var appsFlyerDeepLink: String?
    var appsFlyerRedirected: Boolean
    var isUserProperySet: Boolean
    var userLatLong: String?
    var PinCode: String?
    var KycStatus: Int?
    var isInAppAvailable :Boolean
    var locationApiPendingMinutes:Int?
    var locationApiTimeLimit:Long
    var userCurrentLocationRes:String?
    var disableAppUpdateScreen: Boolean
    var disableAppUpdateVersion: String?
}


/**
 * [PreferenceStorage] impl backed by [android.content.SharedPreferences].
 */
class SharedPreferenceStorageRummy @Inject constructor(@ApplicationContext context: Context) : PreferenceStorageRummy {

    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

    private val observableShowSnackbarResult = MutableLiveData<Boolean>()
    private val changeListener = OnSharedPreferenceChangeListener { _, key ->
        if (key == PREF_SNACKBAR_IS_STOPPED) observableShowSnackbarResult.value = snackbarIsStopped
    }

    init {
        prefs.registerOnSharedPreferenceChangeListener(changeListener)
    }

    override var locationApiPendingMinutes by IntegerPreference(prefs,PREF_LOCATION_PENDING_MINUTES,0)

    override var locationApiTimeLimit by LongPreference(prefs, PREF_LOCATION_TIME, 0L)

    override var userCurrentLocationRes by StringPreference(prefs,PREF_USER_CURRENT_LOCATION_RES,"")

    override var isInAppAvailable by BooleanPreference(prefs, PREF_IS_IN_APP_AVAILABLE, false)

    override var homeCurrentTab by IntegerPreference(prefs,PREF_CURRENT_HOME_TAB,-1)

    override var gameTimelimit by LongPreference(prefs, PREF_GAME_TIME_LIMIT, -1)

    override var gameTimeMinutes by IntegerPreference(prefs, PREF_GAME_TIME_MINUTES, -1)

    override var loginAuthTokan by StringPreference(prefs, LOGIN_AUTHTOKAN, "")

    override var userLatLong by StringPreference(prefs, PREF_USER_LAT_LOG, "")

    override var userStateName by StringPreference(prefs, PREF_USER_STATE_NAME, "")

    override var isDeviceIDSet by BooleanPreference(prefs, PREF_DEVICE_ID_SET, false)

    override var isUserProperySet by BooleanPreference(prefs, PREF_IS_USER_PROPERTY_SET, false)

    override var isFirstSmartechLauch by BooleanPreference(prefs, PREF_SMARTECH_FIRST_LAUNCH, false)

    override var advertisingId by StringPreference(prefs, PREF_ADVERTISING_ID, "")

    override var isTeamNameSet by BooleanPreference(prefs, PREF_TEAM_NAME_SET, false)

    override var firstPaidLeagueJoined by BooleanPreference(prefs, PREF_FIRST_PAID_JOINED, false)

    override var firstFreeLeagueJoined by BooleanPreference(prefs, PREF_FIRST_FREE_JOINED, false)

    override var firstDeposite by BooleanPreference(prefs, PREF_FIRST_DEPOSITE, false)

    override var firstPassPurchased by BooleanPreference(prefs, PREF_FIRST_PASS_PURCHASED, false)

    override var userTotalAmount by StringPreference(prefs, PREF_USER_TOTAL_AMOUNT, "")
    override var isUserLoginOnCleverTap by BooleanPreference(prefs, PREF_USER_LOGIN_CLEVER_TAP, false)
    override var isShowCaseDone by BooleanPreference(prefs, PREF_SHOWCASE_DONE, false)
    override var firstOpen by BooleanPreference(prefs, PREF_FIRST_OPEN, true)
    override var isQuizSoundOn by BooleanPreference(prefs, PREF_QUIZ_SOUND_ON, false)
    override var isQuizReminder by BooleanPreference(prefs, PREF_QUIZ_REMINDER, true)

    override var isWalletShowCaseNew by BooleanPreference(prefs, PREF_WALLET_SHOWCASE_NEW, false)

    override var notificationCount by IntegerPreference(prefs, PREF_SET_TO_DEFAULT, -1)

    override var isOldUser by BooleanPreference(prefs, PREF_OLD_USER, false)

    override var seletedLanguage by StringPreference(prefs, PREF_SELECTED_LANGUAGE, "en")
    override var quizLanguage by StringPreference(prefs, PREF_QUIZ_LANGUAGE, "en")
    override var quizRemindersIds by StringPreference(prefs, PREF_REMINDERS_FOR, "")
    override var matchRemindersIds by StringPreference(prefs, PREF_MATCH_REMINDERS_FOR, "")

    override var appUrl by StringPreference(prefs, PREF_APP_URL, MyConstants.APP_CURRENT_URL)

    override var appUrl2 by StringPreference(prefs, PREF_APP_URL2, "")
    override var displayProfileIcon by BooleanPreference(prefs, PREF_DISPLAY_PROFILE_ICON,false)
    override var showKycOptions by BooleanPreference(prefs, PREF_SHOW_KYC_OPTION,false)
    override var locationDelay by LongPreference(prefs, PREF_LOCATION_DELAY,0L)
    override var gamePlayUrl by StringPreference(prefs, PREF_GAME_URL, "")

    override var quizBackMessage by StringPreference(prefs, PREF_QUIZ_BACK_MESSAGE, "")

    override var referUrl by StringPreference(prefs, PREF_BRANCH_URL, "")

    override var currentDataForQuiz by StringPreference(prefs, PREF_CURRENT_DATE, "")

    override var currentDataForCricket by StringPreference(prefs, PREF_CURRENT_CRICKET_JOIN, "")

    override var todaysCricketMatchIdList by StringPreference(
        prefs,
        PREF_TODAYS_CRICKET_MATCH_ID_LIST,
        "{}"
    )



    override var winnerMatchIdList by StringPreference(prefs, PREF_WINNER_MATCH_ID_LIST, "")

    override var referCode by StringPreference(prefs, PREF_BRANCH_REFER_CODE, "")

    override var referShareMessage by StringPreference(prefs, PREF_BRANCH_REFER_MESSAGE, "")

    override var firebaseToken by StringPreference(prefs, PREF_FIREBASE_TOKEN, "")

    override var createTeamSettingResponse by StringPreference(prefs, PREF_CREATE_TEAM_SETTING, "")

    override var languageResponse by StringPreference(prefs, PREF_LANGUAGE_RESPONSE, "")

    override var pollId by StringPreference(prefs, PREF_POLL_ID, "")

    override var sportResponse by StringPreference(prefs, PREF_SPORT_RESPONSE, "")

    override var androidId by StringPreference(prefs, PREF_ANDROID_ID, "")

    override var loginType by StringPreference(prefs, PREF_LOGIN_TYPE, "EMAIL")

    override var loginResponse by StringPreference(prefs, PREF_LOGIN_RESPONSE, "")

    override var splashResponse by StringPreference(prefs, PREF_SPLASH_RESPONSE, "")

    override var regularColor by StringPreference(prefs, PREF_REGULAR_COLOR, "#7B0041")

    override var safeColor by StringPreference(prefs, PREF_SAFE_COLOR, "#0684F2")

    override var refertext by StringPreference(prefs, PREF_REFER_TEXT, "Earn ₹50")

    override var userName by StringPreference(prefs, PREF_USER_NAME, "")

    override var userTeamName by StringPreference(prefs, PREF_USER_TEAM_NAME, "")
    override var appsFlyerDeepLink by StringPreference(prefs, PREF_APPSFLYER_DEEPLINK, "")

    override var appsFlyerRedirected by BooleanPreference(prefs, "appsFlyerRedirected", false)

    override var onSafePlay by BooleanPreference(prefs, PREF_SAFE, true)

    override var firebaseTokenChanged by BooleanPreference(
        prefs, PREF_FIREBASE_TOKEN_CHANGED, false
    )

    override var sportSelected by IntegerPreference(prefs, PREF_SPORT_TYPE, 1)

    override var sportsDefaultValue by IntegerPreference(prefs, PREF_SPORT_DEFAULT_VALUE, -1)

    override var matchTimeType by IntegerPreference(prefs, PREF_MATCH_TIME_TYPE, FULL_TIME_TYPE)

    override var selectedTheme by IntegerPreference(prefs, PREF_SELECTED_THEME, 0)

    override var avtarId by IntegerPreference(prefs, PREF_AVTAR_ID, -1)

    override var splashImageUrl by StringPreference(prefs, PREF_SPLASH_IMAGE_URL, "")

    override var loginCompleted: Boolean by BooleanPreference(prefs, PREF_LOGIN, false)

    override var IsContactSaved: Boolean by BooleanPreference(prefs, PREF_CONTACT_SAVE, false)

    override var isEmailSent: Boolean by BooleanPreference(prefs, PREF_IS_EMAIL_SENT, false)

    override var introductionCompleted: Boolean by BooleanPreference(
        prefs, PREF_ONINTRODUCTION, false
    )

    override var notificationsPreferenceShown
            by BooleanPreference(prefs, PREF_NOTIFICATIONS_SHOWN, false)

    override var preferToReceiveNotifications
            by BooleanPreference(prefs, PREF_RECEIVE_NOTIFICATIONS, true)

    override var snackbarIsStopped by BooleanPreference(prefs, PREF_SNACKBAR_IS_STOPPED, false)


    override var sendUsageStatistics by BooleanPreference(prefs, PREF_SEND_USAGE_STATISTICS, true)

    override var isUserFirstTime by BooleanPreference(prefs, PREF_IS_FIRST_TIME, true)

    override var winningMatchIds by StringPreference(prefs, PREF_WINNING_MESSAGE_ID, "")

    override var lastOfferId by IntegerPreference(prefs, PREF_LAST_OFFER_ID, 0)

    override var campaignId by StringPreference(prefs, PREF_BRANCH_CAMPAIGNID, "")

    override var chatMsgCount by StringPreference(prefs, PREF_CHAT_MAG_COUNT, "[]")

    override var PinCode by StringPreference(prefs, PREF_USER_PIN_CODE,"")
    override var KycStatus by IntegerPreference(prefs, PREF_USER_KYC_STATUS,0)

    override var disableAppUpdateScreen by BooleanPreference(prefs, PREF_DISABLE_UPDATE_SCREEN, false)

    override var disableAppUpdateVersion by StringPreference(prefs,PREF_NEW_APP_CODE,"")

    var notificationReadIdsList: String
        get(){
            val loginUser = Gson().fromJson(loginResponse, LoginResponse::class.java)
            return prefs.getString("READ_IDS_${loginUser.UserId}","")?:""
        }
        set(value) {
            val loginUser = Gson().fromJson(loginResponse, LoginResponse::class.java)
            prefs.edit().putString("READ_IDS_${loginUser.UserId}", value).apply()
        }

    var notificationKey: String
        get(){
            val loginUser = Gson().fromJson(loginResponse, LoginResponse::class.java)
            return prefs.getString("NOT_KEY_${loginUser.UserId}","0")?:"0"
        }
        set(value) {
            val loginUser = Gson().fromJson(loginResponse, LoginResponse::class.java)
            prefs.edit().putString("NOT_KEY_${loginUser.UserId}", value).apply()
        }

    var latestNotificationAvailable: Boolean
        get(){
            val loginUser = Gson().fromJson(loginResponse, LoginResponse::class.java)
            return prefs.getBoolean("UnRead_NOT${loginUser.UserId}",false)?:false
        }
        set(value) {
            val loginUser = Gson().fromJson(loginResponse, LoginResponse::class.java)
            prefs.edit().putBoolean("UnRead_NOT${loginUser.UserId}", value).apply()
        }

    companion object {
        const val PREF_CHAT_MAG_COUNT = "pref_chat_msg_count"
        const val PREFS_NAME = "rummyTitansSDK"
        const val PREF_SAFE = "pref_safe"
        const val PREF_IS_GAME_DISABLED = "pref_is_Games_disabled"
        const val PREF_ONINTRODUCTION = "pref_introduction"
        const val PREF_LOGIN = "pref_login"
        const val PREF_LOGIN_RESPONSE = "pref_login_response"
        const val PREF_SPLASH_RESPONSE = "pref_splash_response"
        const val PREF_NOTIFICATIONS_SHOWN = "pref_notifications_shown"
        const val PREF_RECEIVE_NOTIFICATIONS = "pref_receive_notifications"
        const val PREF_SNACKBAR_IS_STOPPED = "pref_snackbar_is_stopped"
        const val PREF_SEND_USAGE_STATISTICS = "pref_send_usage_statistics"
        const val PREF_NOTIFICATION_KEY = "pref_notification_key"
        const val PREF_SPORT_TYPE = "pref_sport_type"
        const val PREF_SPORT_DEFAULT_VALUE = "pref_sport_default_value"
        const val PREF_LOGIN_TYPE = "pref_login_type"
        const val PREF_ANDROID_ID = "pref_android_id"
        const val PREF_SPORT_RESPONSE = "pref_sport_response"
        const val PREF_POLL_ID = "pref_poll_id"
        const val PREF_LANGUAGE_RESPONSE = "pref_language_response"
        const val PREF_SELECTED_LANGUAGE = "Locale.Helper.Selected.Language"
        const val PREF_DISPLAY_PROFILE_ICON = "pref_display_profile_icon"
        const val PREF_SHOW_KYC_OPTION = "pref_show_kyc_option"
        const val PREF_LOCATION_DELAY = "pref_location_delay"
        const val PREF_IS_FIRST_TIME = "pref_ois_first_time"
        const val PREF_IS_EMAIL_SENT = "pref_is_email_sent"
        const val PREF_FIREBASE_TOKEN = "pref_firebase_token"
        const val PREF_FIREBASE_TOKEN_CHANGED = "pref_firebase_token_changed"
        const val PREF_OLD_USER = "pref_old_user"
        const val PREF_WINNING_MESSAGE_ID = "pref_winning_messageIds"
        const val PREF_AVTAR_ID = "pref_avtar_id"
        const val PREF_THEME_COLOR = "pref_theme_color"
        const val PREF_USER_NAME = "pref_user_name"
        const val PREF_USER_TEAM_NAME = "pref_user_team_name"
        const val PREF_SPLASH_IMAGE_URL = "pref_splash_image_url"
        const val PREF_REGULAR_COLOR = "pref_regular_color"
        const val PREF_SAFE_COLOR = "pref_safe_color"
        const val PREF_REFER_TEXT = "pref_refer_text"
        const val PREF_CONTACT_SAVE = "pref_contact_save"
        const val PREF_MATCH_TIME_TYPE = "match_time_type"
        const val PREF_SELECTED_THEME = "pref_selected_theme"
        const val PREF_LAST_OFFER_ID = "pref_last_offer_id"
        const val PREF_SET_TO_DEFAULT = "pref_count_notification"
        const val PREF_SHOWCASE_DONE = "pref_showcase_done"
        const val PREF_FIRST_OPEN = "pref_first_open"
        const val PREF_QUIZ_SOUND_ON = "pref_quiz_sound"
        const val PREF_QUIZ_REMINDER = "pref_quiz_reminder"
        const val PREF_WALLET_SHOWCASE_NEW = "pref_wallet_showcase_new"
        const val PREF_BRANCH_CAMPAIGNID = "pref_branch_campaignid"
        const val PREF_CREATE_TEAM_SETTING = "pref_create_team_setting"
        const val PREF_APP_URL = "pref_app_url"
        const val PREF_APP_URL2 = "pref_app_url2"
        const val PREF_GAME_URL = "pref_game_url2"
        const val PREF_QUIZ_BACK_MESSAGE = "pref_quiz_back_message"
        const val PREF_BRANCH_URL = "pref_branch_url"
        const val PREF_BRANCH_REFER_CODE = "pref_branch_refercode"
        const val PREF_BRANCH_REFER_MESSAGE = "pref_branch_refermessage"
        const val PREF_QUIZ_LANGUAGE = "pref_quiz_language"
        const val PREF_REMINDERS_FOR = "pref_quiz_reminders_for"
        const val PREF_MATCH_REMINDERS_FOR = "pref_match_reminders_for"
        const val PREF_CURRENT_DATE = "pref_current_date_for_quiz"
        const val PREF_CURRENT_CRICKET_JOIN = "pref_current_date_cricket_join"
        const val PREF_TODAYS_CRICKET_MATCH_ID_LIST = "pref_todays_cricket_match_id_list"
        const val PREF_WINNER_MATCH_ID_LIST = "pref_winner_match_id_list"
        const val PREF_ALLOW_RUMMY_GAME = "pref_allow_rummy_game"
        const val PREF_RUMMY_GAME_URL = "pref_rummy_game_url"
        const val PREF_DEVICE_ID_SET = "pref_is_device_id_set"
        const val PREF_TEAM_NAME_SET = "pref_is_team_name_set"
        const val PREF_FIRST_PAID_JOINED = "pref_is_first_paid_joined"
        const val PREF_FIRST_FREE_JOINED = "pref_is_first_free_joined"
        const val PREF_USER_TOTAL_AMOUNT = "pref_user_total_amount"
        const val PREF_USER_LOGIN_CLEVER_TAP = "pref_user_login_clever_tap"
        const val PREF_FIRST_DEPOSITE = "pref_first_deposite"
        const val PREF_FIRST_PASS_PURCHASED = "pref_first_pass_purchased"
        const val LOGIN_AUTHTOKAN = "login_Auth_tokan"
        const val NOTIFICARION_IDS_READED = "notifications_read_ids"
        const val PREF_SMARTECH_FIRST_LAUNCH = "isFirstTimeSmartechLaunch"
        const val PREF_ADVERTISING_ID = "pref_advertising_id"
        const val PREF_GAME_TIME_LIMIT = "pref_game_time_limit"
        const val PREF_GAME_TIME_MINUTES = "pref_game_time_minutes"
        const val PREF_CURRENT_HOME_TAB = "pref_current_home_tab"
        const val PREF_USER_STATE_NAME = "pref_user_state_name"
        const val PREF_APPSFLYER_DEEPLINK = "appsFlyerDeepLink"
        const val PREF_IS_USER_PROPERTY_SET = "pref_is_user_property_set"
        const val PREF_USER_LAT_LOG = "pref_user_lat_long"
        const val PREF_USER_PIN_CODE = "pref_user_pin_code"
        const val PREF_USER_KYC_STATUS = "pref_user_kyc_status"
        const val PREF_IS_IN_APP_AVAILABLE = "pref_is_in_app_available"
        const val PREF_USER_CURRENT_LOCATION_RES = "prefs_user_current_location_res"
        const val PREF_LOCATION_TIME = "prefs_location_time"
        const val PREF_LOCATION_PENDING_MINUTES = "prefs_location_pending_minutes"
        const val PREF_DISABLE_UPDATE_SCREEN = "prefs_disable_App_update_screen"
        const val PREF_NEW_APP_CODE = "prefs_new_app_update_code"
    }

    fun registerOnPreferenceChangeListener(listener: OnSharedPreferenceChangeListener) {
        prefs.registerOnSharedPreferenceChangeListener(listener)
    }
}

class BooleanPreference(
    private val preferences: SharedPreferences,
    private val name: String,
    private val defaultValue: Boolean
) : ReadWriteProperty<Any, Boolean> {
    @WorkerThread
    override fun getValue(thisRef: Any, property: KProperty<*>): Boolean {
        return preferences.getBoolean(name, defaultValue)
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: Boolean) {
        preferences.edit { putBoolean(name, value) }
    }
}

class StringPreference(
    private val preferences: SharedPreferences,
    private val name: String,
    private val defaultValue: String?
) : ReadWriteProperty<Any, String?> {
    @WorkerThread
    override fun getValue(thisRef: Any, property: KProperty<*>): String? {
        return preferences.getString(name, defaultValue)
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: String?) {
        preferences.edit { putString(name, value) }
    }
}

class IntegerPreference(
    private val preferences: SharedPreferences,
    private val name: String,
    private val defaultValue: Int
) : ReadWriteProperty<Any, Int?> {
    override fun getValue(thisRef: Any, property: KProperty<*>): Int? {
        return preferences.getInt(name, defaultValue)
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: Int?) {
        preferences.edit { putInt(name, value ?: 0) }
    }
}

class LongPreference(
    private val preferences: SharedPreferences,
    private val name: String,
    private val defaultValue: Long
) : ReadWriteProperty<Any, Long?> {
    override fun getValue(thisRef: Any, property: KProperty<*>): Long {
        return preferences.getLong(name, defaultValue)
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: Long?) {
        preferences.edit { putLong(name, value ?: 0) }
    }
}

