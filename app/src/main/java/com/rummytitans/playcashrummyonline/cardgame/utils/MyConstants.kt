package com.rummytitans.playcashrummyonline.cardgame.utils

import com.rummytitans.playcashrummyonline.cardgame.R
import androidx.lifecycle.MutableLiveData
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

object MyConstants {


    const val PRODUCTION_URL = "https://app.myteam11.com/"
    const val STAGING_URL = "https://api.goteam11.com/"
    const val REDIS_URL = "https://redis.goteam11.com/"
    const val N2_URL = "https://n2.myteam11.com/"
    var CURRENT_APP_TYPE = 8
    const val SPLASH_URL =  "https://api.myteam11.com"//"https://app2.goteam11.com"//
    const val APP_CURRENT_URL = PRODUCTION_URL
    //const val APP_CURRENT_URL2 = "http://15.207.63.62:7000/"
   // const val GAME_PLAY_URL = "https://n2.mt11games.com:7000"
    //const val GAME_PLAY_URL = "https://rummytitans.mt11games.com/"
    const val GAME_PLAY_URL = "https://rummy.myteam11.games/"

    const val REQUEST_UPDATE_VERIFY_DETAILS=999

    const val APP_TYPE = 8
    const val TEST_ACCOUNT_EMAIL="vinubagda@gmail.com"
    const val TEST_ACCOUNT_MOBILE="5828666143"
    const val TEST_ACCOUNT_USER_ID=12337310

    const val STANDLONE_GAME_TYPE = 1
    const val STANDLONE_GAME_ID = 9
    const val GAME_RELOAD = 9911
    const val WALLET = 1
    const val UPI = 4
    const val NET_BANKING = 2
    const val OTHER = 5
    const val DEFAULT_ADD_CASH_AMOUNT = 700.0

    const val REQUEST_CODE_GALLERY =111
    const val REQUEST_CODE_CAMERA =112
    const val REQUEST_APP_SETTING =25

    const val SELECT_FILE_ADHAAR_CARD=1
    const val SELECT_FILE_DRIVE_LICENCE=2
    const val DOC_SIDE_FRONT = 0
    const val DOC_SIDE_BACK = 1

    const val REQUEST_CODE_ADD_CASH = 115
    const val CALL_RECENT_MATCH_API = "CALL_RECENT_MATCH_API"
    const val FULL_TIME_TYPE = 0
    const val HOURS_TYPE = 1
    const val DATE_TYPE = 2
    const val SMALL_POPUP = 1
    const val BIG_POPUP = 2
    const val MATCH_POPUP = 3
    const val ADD_CASH_POPUP = 4
    const val SCRACH_POPUP = 5
    const val POLL_POPUP = 6
    const val DIALOG_FOR_CREATE_TEAM = 1
    const val DIALOG_FOR_JOIN_CONTEST = 2
    const val DIALOG_FOR_CREATE_CONTEST = 3
    const val REQUEST_POPUP_DIALOGS = 678
    const val REQUEST__NOTIFICATION_READ = 231
    const val ADD_VIEW=999

    const val RESTRICT_LOC_MESSAGE="Uh-oh! You can't proceed further as you are from a restricted state."

    const val AppDeeplink = "rummytitandeeplink"
    const val myTeamDeepLink = "rummytitandeeplink"
    const val AppDeeplink_ = "rummytitansdeeplink"
    const val CREATE_NEW_TEAM = "create"
    const val EDIT_TEAM = "edit"
    const val HIDE_EDIT_TEAM = "hide_edit_teambtn"
    const val CLONE_TEAM = "clone"
    const val LOGIN = "LOGIN"
    const val SIGN_UP = "SIGNUP"
    const val VARIFY_fROM = "varfiyFrom"
    const val FORGET_PASSWORD = "forgetPassword"

    const val APP_UPDATE_FULL_SCREEN=1
    const val APP_UPDATE_BOTTOM_SHEET=3
    const val PHONE_PICKER_REQUEST = 12345
    const val INTENT_PASS_DARK_STATUS = "intent_pass_dark_status"
    const val INTENT_COME_FROM_GAME = "intent_come_from_game"
    const val MOBILE_STATUS_SELECT = "SELECT"
    const val INTENT_GAME_DEEPLINK = "intent_game_deepLink"
    const val MOBILE_STATUS_CANCELED = "CANCELED"
    const val MOBILE_STATUS_NONE = "NONE"
    const val COMING_FOR_GAME="comingForGame"
    const val REDEEM_GAME_TICKET="redeemGameTicket"
    const val INTENT_POKER_DATA="pokerData"
    const val INTENT_GAME_DATA="intent_game_data"
    const val INTENT_LOGIN_RESPONSE = "intent_login_response"
    const val INTENT_FORGOT_PASSWORD = "intent_forgot_password"
    const val INTENT_FROM_ONBOARDING = "intent_from_onboarding"
    const val INTENT_FROM_CONTEST_INFO = "intent_from_contest_info"
    const val INTENT_FROM_PRIVATE_CONTEST = "intent_from_private_contest"
    const val INTENT_PRIVATE_CONTEST_WINNING_BREAKUP = "intent_pc_winning_breakup"
    const val INTENT_PASS_CONTEST_PRIVATE = "intent_pass_contest_isprivate"
    const val INTENT_PASS_QUIZ_QUESTIONS = "intent_pass_quiz_questions"
    const val INTENT_PASS_TIME = "intent_pass_remaining_time"
    const val INTENT_PASS_SELECTED_LANGUAGE = "intent_pass_selected_language"
    const val INTENT_PASS_LIST="intent_pass_list"
    const val INTENT_PASS_MATCH = "intent_pass_match"
    const val INTENT_PASS_MATCH_ID = "intent_pass_match_id"
    const val INTENT_PASS_CURRENT_INDEX = "intent_pass_current_index"
    const val INTENT_PASS_SHOW_SCORECARD = "intent_pass_show_Scorecard"
    const val INTENT_PASS_ALL_CONTEST="intent_pass_all_contest"
    const val INTENT_PASS_TEAM_CLONE="intent_pass_team_Clone"
    const val INTENT_PASS_TEAM_EDIT="intent_pass_team_edit"
    const val INTENT_PASS_SCRACH_CARD = "intent_pass_scratch_Card"
    const val INTENT_PASS_CONTEST = "intent_pass_contest"
    const val INTENT_PASS_CONTEST_MULTIPLE = "intent_pass_contest_is_multiple"
    const val INTENT_PASS_CONTEST_FEES = "intent_pass_contest_fees"
    const val INTENT_PASS_CONTEST_ID = "intent_pass_contest_id"
    const val INTENT_PASS_CATEGORY_ID = "intent_pass_category_id"
    const val INTENT_PASS_TEAM_ID = "intent_pass_team_id"
    const val INTENT_PASS_MULTIPLE_TEAM_IDs = "intent_pass_multi_team_ids"
    const val INTENT_PASS_SHOW_CREDITS = "intent_pass_show_credits"
    const val INTENT_PASS_TEAM_ARRAY = "intent_pass_team_array"
    const val INTENT_PASS_TEAM_NAME_ARRAY = "intent_pass_team_name_array"
    const val INTENT_PASS_TEAM_SCROLL_POSITION = "intent_pass_team_scroll_position"
    const val INTENT_PASS_TEAM = "intent_pass_team_array"
    const val INTENT_PASS_CATEGORY_TITLE = "intent_pass_category_title"
    const val INTENT_PASS_TEAM_COUNT = "intent_pass_team_count"
    const val INTENT_PASS_JOIN_COUNT = "intent_pass_joined_count"
    const val INTENT_PASS_IS_MULTIPLE_ALLOW = "intent_pass_team_count"
    const val INTENT_PASS_IS_CONFIRM = "intent_pass_confirm"
    const val INTENT_PASS_PLAYERS: String = "intent_pass_players"
    const val INTENT_PASS_WEB_URL = "intent_pass_web_url"
    const val INTENT_PASS_SHOW_TOOLBAR = "intent_pass_show_toolbar"
    const val INTENT_PASS_FROM_PAYMENTS = "intent_pass_from_payment"
    const val INTENT_PASS_SHOW_CROSS = "intent_pass_show_cross"
    const val INTENT_PASS_WEB_TITLE = "intent_pass_web_title"
    const val INTENT_PASS_WHEEL_DATA = "intent_pass_web_title"
    const val INTENT_PASS_SELECT_TAB = "intent_pass_select_Tab"
    const val INTENT_IS_TEAM_EDIT = "intent_pass_is_edit_team"
    const val INTENT_PASS_TEAM_TEAM1_NAME = "intent_pass_team1_name"
    const val INTENT_IS_TEAM_CLONE = "intent_pass_is_clone_team"
    const val INTENT_IS_JOIN_CONTEST = "intent_is_join_contest"
    const val INTENT_IS_JOINED_CONTEST = "intent_is_joined_contest"
    const val INTENT_PASS_REFER_CODE = "referCode"

    const val INTENT_PASS_VERIFICATION_REJECT_MSG = "intent_pass_verification_reject"
    const val INTENT_PASS_IS_ADDRESS_VERIFICATION_REQUIRED = "intent_pass_address_verific_required"

    const val INTENT_PASS_COMING_FROM = "intent_pass_coming_from"
    const val CREATE_TEAM_FROM = "createTeamFrom"
    const val INTENT_COME_FROM = "intent_come_from"
    const val INTENT_PASS_FROM = "FROM"
    const val FROM_CATEGORY_CONTEST = "from_category_contest"
    const val INTENT_PASS_COMMON_TYPE = "intent_pass_common_type"
    const val INTENT_PASS_MAX_HEIGHT = "intent_pass_max_height"
    const val INTENT_PASS_OPEN_REDDEM = "intent_pass_open_reddem"
    const val MATCH_NOT_STARTED = "notstarted"
    const val MATCH_STARTED = "started"
    const val MATCH_COMPLETED = "completed"
    const val MATCH_ABANDONED = "abandoned"
    const val MAX_TEAMS: Int = 6
    const val CRICKET = "CRICKET"
    const val FOOTBALL = "FOOTBALL"
    const val VOLLEYBALL = "VOLLEYBALL"
    const val BASKETBALL = "BASKETBALL"
    const val HOME_DIALOGS="HOME_DIALOGS"
    const val INTENT_RETURN_TEAM_COUNT = "intent_return_team_count"
    const val INTENT_RETURN_TEAM_ID = "intent_return_team_id"
    const val INTENT_PASS_WITHDRAWAL = "intent_pass_withdrawal"
    const val INTENT_PASS_KYS_NOTES = "intent_pass_kyc_notes"
    const val INTENT_PASS_DOC_SELECTED = "INTENT_PASS_DOC_SELECTED"

    const val WICKET_KEEPER = "WICKET-KEEPER"
    const val BATSMAN = "BATSMEN"
    const val ALL_ROUNDER = "ALL-ROUNDERS"
    const val BOWLER = "BOWLERS"
    const val GOAL_KEEPER = "GOAL-KEEPER"
    const val DEFENDER = "DEFENDER"
    const val MID_FIELDER = "MID-FIELDER"
    const val FORWARD = "FORWARD"
    const val LIBERO = "LIBERO"
    const val SETTER = "SETTER"
    const val BLOCKER = "BLOCKER"
    const val ATTACKER = "ATTACKER"
    const val UNIVERSAL = "UNIVERSAL"
    const val POINT_GUARD = "POINT GUARD"
    const val SHOOTING_GUARD = "SHOOTING GUARD"
    const val SMALL_FORWARD = "SMALL FORWARD"
    const val POWER_FORWARD = "POWER FORWARD"
    const val CENTER = "CENTER"

    var CURRENT_TIME = MutableLiveData<Long>(0)
    var APP_THEME = MutableLiveData<Int>(R.style.RummyAppTheme)
    const val KABADDI = "Kabaddi"
    const val Raider = "RAIDER"
    const val COUPONMODEL="COUPONMODEL"
    const val SORT_TYPE="sortType"
    const val SORT_TYPE_IS_ASCENDING="sort_type_is_ascending"
    const val INTENT_PASS_LINEUP_STATUS = "intent_pass_lineup_status"
    const val INTENT_PASS_EXISTING_TEAM = "intent_pass_existing_team"
    const val INTENT_PASS_AMOUNT = "INTENT_PASS_ADD_AMOUNT"
    const val INTENT_ADD_CASH_RESTRICTION = "INTENT_PASS_ADD_RESTRICTION"
    const val INTENT_SHOW_INVITE_CONTEST = "intent_show_invite_contest"
    const val EVENT_CONTEST_CREATED = "event_contest_created_successfully"
    const val INTENT_IS_MY_CONTEST = "intent_is_my_contests"
    const val INTENT_COMPLETED_CONTEST_RESPONSE = "intent_pass_completed_contest_response"
    const val INTENT_COMPLETED_CONTEST_SCORE_RESPONSE =
        "intent_pass_completed_contest_score_response"
    const val INTENT_RANK_MATCH_NAME = "intent_pass_rank_match_name"
    const val INTENT_RANK_MATCH_TYPE = "intent_pass_match_type"
    const val INTENT_PASS_PLAYER = "intent_pass_player_id"
    const val INTENT_PASS_PLAYER_NAME = "intent_pass_player_name"
    const val INTENT_PASS_IMG_URL = "intent_pass_img_url"
    const val INTENT_PASS_GAME_CATEGORY_MODEL = "intent_pass_game_Category_model"
    const val INTENT_PASS_MODEL = "intent_pass_model"
    const val INTENT_PASS_TRANSACTION_ID = "intent_pass_model"
    const val INTENT_PASS_WALLET_OPTION_MODEL = "intent_pass_wallet_option_model"
    const val INTENT_PASS_FROM_ALL_GAMES = "intent_pass_from_all_games"
    const val INTENT_PASS_INT = "intent_pass_int"
    const val INTENT_PASS_GAME_WEB_MODEL = "intent_pass_game_web_model"
    const val INTENT_PASS_TEAM_NAME = "intent_pass_leaderboard_team_name"
    const val INTENT_PASS_IS_TEST_MATCH = "intent_pass_is_test_match"
    const val INTENT_PASS_PLAYER_STATES_BY_MATCH = "intent_pass_player_state_by_match"
    const val INTENT_ADD_CASH_FOR_JOIN = "INTENT_ADD_CASH_FOR_JOIN"
    const val INTENT_PASS_JUSPAY = "intent_pass_juspay"
    const val INTENT_PASS_APP_TYPE_SAFE = "app_type"
    const val INTENT_PASS_COMPARE_TEAM_1 = "intent_pass_compare_team_1"
    const val INTENT_PASS_COMPARE_TEAM_2 = "intent_pass_compare_team_2"
    const val INTENT_PASS_APP_TYPE = "intent_pass_app_type"
    const val INTENT_PASS_LEAGUE_DATA = "intent_pass_league_data"

    const val DOC_UPLOAD_ALREADY_VERIFIED = 0
    const val DOC_UPLOAD_SUCCESS = 1
    const val DOC_UPLOAD_INVALID = 2
    const val DOC_UPLOAD_AGE_BELOW_18 = 3
    const val DOC_UPLOAD_RESTRICT = 4
    const val DOC_UPLOAD_FAILED = 5

    const val VERIFY_ITEM_MOBILE= 1
    const val VERIFY_ITEM_EMAIL = 2
    const val VERIFY_ITEM_PAN = 3
    const val VERIFY_ITEM_BANK = 4
    const val VERIFY_ITEM_ADDRESS = 5

    fun countDown(from: Long, to: Long): Observable<Long> {
        return Observable.zip(
            (Observable.range(from.toInt(), to.toInt())),
            Observable.interval(1, TimeUnit.SECONDS),
            BiFunction<Int, Long, Long> { integer, _ ->
                return@BiFunction to - integer
            }
        ).observeOn(Schedulers.io())
            .subscribeOn(AndroidSchedulers.mainThread())
    }
}
