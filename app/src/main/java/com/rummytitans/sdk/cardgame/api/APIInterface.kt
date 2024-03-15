package com.rummytitans.sdk.cardgame.api

import com.rummytitans.sdk.cardgame.models.*
import com.rummytitans.sdk.cardgame.utils.MyConstants
import com.google.gson.JsonObject
import com.rummytitans.sdk.cardgame.ui.payment.viewmodel.WalletInitializeModel
import io.reactivex.Observable
import io.reactivex.Single
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface APIInterface {

    @GET("notification/notification-key")
    fun getNotificationKey(
        @Header(USERID) userId: Int,
        @Header(EXPIRETOKEN) expireToken: String,
        @Header(AUTHEXPIRE) authExpire: String,
    ): Single<BaseModelRummy<Any>>

    @GET("games/v1/splash")
    fun getVersion(
        @Header(USERID) userId: Int,
        @Header("Version") code: Int,
        @Header("AdvertisingId") AdvertisingId: String
    ): Single<BaseModelRummy<VersionModelRummy>>

    @POST("users/get")
    fun loginUser(
        @Header(EMAIL_ADDRESS) email: String,
        @Header(AUTH_EXPIRE) password: String,
        @Header(CAMPAIGN_ID) campaignId: String
    ): Single<BaseModelRummy<Any>>

    @POST("users/loginwithotp")
    fun requestOtp(
        @Header(MOBILE_NUMER1) mobile: String,
        @Header(LOGIN_AUTH_EXPIRE) auth: String,
        @Header(REFER_CODE) referCode: String,
        @Header(CAMPAIGN_ID1) campaignId: String
    ): Single<BaseModelRummy<Any>>

    @POST("v1/user/change-mobile")
    fun requestOtpForUpdate(
        @Header("UserId") mobile: Int,
        @Header("MobileNumber") auth: String,
        @Header("DeviceID") referCode: String,
        @Header("ExpireToken") campaignId: String,
        @Header("AuthExpire") authExpire: String
    ): Single<BaseModelRummy<Any>>

    @PUT("v1/games/login")
    fun verifyLoginOtp(
        @Header("Mobile") mobile: String,
        @Header("LoginAuthToken") auth: String,
        @Header("ReferCode") referCode: String,
        @Header("CampaignId") campaignId: String,
        @Header("Otp") Otp: String,
        @Header("DeviceID") deviceID: String,
        @Header(TOKEN_FIREBASE_1) FirebaseToken: String,
        @Header("AdvertisingId") AdvertisingId: String,
        @Header("AppsFlyerId") AppsFlyerId: String,
        @Header("Type") type: Int=1
    ): Single<BaseModelRummy<Any>>

    @POST("account/v2/login/truecaller")
    fun loginTrueCaller(
        @Header("Code") Code: String,
        @Header("Verifier") Verifier: String,
        @Header("ReferCode") referCode: String,
        @Header("CampaignId") campaignId: String,
        @Header("DeviceID") deviceID: String,
        @Header(TOKEN_FIREBASE_1) FirebaseToken: String,
        @Header("AdvertisingId") AdvertisingId: String,
        @Header("AppsFlyerId") AppsFlyerId: String
    ): Single<BaseModelRummy<Any>>





    @POST("v1/user/login-email-mobile")
    fun loginWithEmail(
        @Header(EMAIL) email: String,
        @Header(PASSWORD_1) password: String,
        @Header(CAMPAIGN_ID1) campaignId: String,
        @Header(DEVICE_ID_2) deviceID: String,
        @Header("AdvertisingId") AdvertisingId: String,
        @Header("AppsFlyerId") AppsFlyerId: String
    ): Single<BaseModelRummy<Any>>

    @GET("users/usercheck")
    fun checkUser(@Header(EMAIL) email: String, @Header(SID) sId: String): Single<BaseModelRummy<Any>>

    @POST("users/gmaillogin")
    fun loginUserG(
        @Header(EMAIL_ADDRESS) email: String,
        @Header(USER_REFER_CODE) userreferCode: String,
        @Header(TOKEN) token: String,
        @Header(TOKEN_FIREBASE) tokenFireBase: String,
        @Header(GMAIL_ID) GmailID: String,
        @Header(GMAIL_ACCESS_TOKEN) gmailAccessToken: String,
        @Header(CAMPAIGN_ID) campaignId: String
    ): Single<BaseModelRummy<Any>>




    @POST("users/register")
    fun registerUser(
        @Header(EMAIL_ADDRESS) email: String,
        @Header(PASSWORD) password: String,
        @Header(USER_REFER_CODE) userreferCode: String,
        @Header(TOKEN) token: String,
        @Header(TOKEN_FIREBASE) tokenFireBase: String,
        @Header(CAMPAIGN_ID) campaignId: String
    ): Single<BaseModelRummy<Any>>

    @POST("users/sentotpnew")
    fun sendOTP(
        @Header(MOBILE_NUMBER) Mobile_Number: String,
        @Header(USERID) UserId: Int,
        @Header(EXPIRETOKEN) ExpireToken: String,
        @Header(AUTHEXPIRE) AuthExpire: String
    ): Single<BaseModelRummy<Any>>
    @POST("users/forgotpasswordnew")
    fun forgotPasswordSendOTP(
        @Header(MOBILE_NUMBER) Mobile_Number: String,
        @Header(TOKEN) token: String
    ): Single<BaseModelRummy<Any>>

    @PUT("users/verifyOtpnew")
    fun verifyOTP(
        @Header(MOBILE_NUMBER) mobileNumber: String,
        @Header(OTP) otp: String,
        @Header(USERID) userId: Int,
        @Header(EXPIRETOKEN) expireToken: String,
        @Header(AUTHEXPIRE) authExpire: String
    ): Single<BaseModelRummy<Any>>

    @PUT("users/verifyOtpnew")
    fun verifyOtpForUpdateMobile(
        @Header(MOBILE_NUMBER) mobileNumber: String,
        @Header(OTP) otp: String,
        @Header(USERID) userId: Int,
        @Header(EXPIRETOKEN) expireToken: String,
        @Header(AUTHEXPIRE) authExpire: String
    ): Single<BaseModelRummy<Any>>

    @POST("users/resetotpverifynew")
    fun forgotPasswordVerifyOTP(
        @Header(MOBILE_NUMBER) mobileNumber: String,
        @Header(OTP) otp: String,
        @Header(TOKEN) token: String
    ): Single<BaseModelRummy<Any>>

    @POST("users/resetpasswordnew")
    fun resetPassword(
        @Header(MOBILE_NUMBER) mobileNumber: String,
        @Header(OTP1) otp: String,
        @Header(TOKEN_1) token: String,
        @Header(PASSWORD_1) password: String
    ): Single<BaseModelRummy<Any>>

    @POST("users/updateuserpincode")
    fun updatePincode(
        @Header(AUTHEXPIRE) mobileNumber: String,
        @Header(EXPIRETOKEN) otp: String,
        @Header(USERID) token: String,
        @Header(PINCODE) password: String,
        @Header(DOB) dob: String
    ): Single<BaseModelRummy<Any>>

    @GET("users/teamcheck")
    fun checkTeamnameAvailability(
        @Header(USERID) userId: Int,
        @Header(EXPIRETOKEN) expireToken: String,
        @Header(AUTHEXPIRE) authExpire: String,
        @Header(TEAMNAME) teamName: String
    ): Single<BaseModelRummy<Any>>

    @GET("v3/team/getteamsuggestions")
    fun checkTeamnameSuggestions(
        @Header(USERID) userId: Int,
        @Header(EXPIRETOKEN) expireToken: String,
        @Header(AUTHEXPIRE) authExpire: String
    ): Observable<BaseModelRummy<String>>



    @PUT("users/updateteamname")
    fun updateCompleteProfile(
        @Header(USERID) userId: Int,
        @Header(EXPIRETOKEN) expireToken: String,
        @Header(AUTHEXPIRE) authExpire: String,
        @Header(NAME) name: String,
        @Header(TEAMNAME1) teamName: String,
        @Header(STATE_NAME) stateName: String,
        @Header(AVATAR_ID) avatarId: Int
    ): Single<BaseModelRummy<String>>

    @GET("users/getuserprofile")
    fun getProfile(
        @Header(USERID) userId: Int,
        @Header(EXPIRETOKEN) expireToken: String,
        @Header(AUTHEXPIRE) authExpire: String
    ): Single<BaseModelRummy<Any>>

    @GET("users/getuserprofile")
    fun getProfileIno(
        @Header(USER_ID) UserId: String,
        @Header(EXPIRE_TOKEN) ExpireToken: String,
        @Header(AUTH_EXPIRE) AuthExpire: String,
        @Header("InstanceId") instanceId: String
    ): Single<BaseModelRummy<ProfileInfoModel>>

    @GET("myprofile/getlevels")
    fun getLevels(
        @Header(USER_ID) UserId: String,
        @Header(EXPIRE_TOKEN) ExpireToken: String,
        @Header(AUTH_EXPIRE) AuthExpire: String
    ): Single<BaseModelRummy<LevelModel>>

    @POST("player/userteamsave")
    fun saveTeam(
        @Header(USERID) userId: Int,
        @Header(EXPIRETOKEN) expireToken: String,
        @Header(AUTHEXPIRE) authExpire: String,
        @Header(MATCH_ID) matchId: Int,
        @Header(PLAYER_RESP) playerRes: String,
        @Header(CAMPAIGN_ID) campaignId: String
    ): Single<BaseModelRummy<String>>

    @POST("player/userteamsavev1")
    fun saveTeamNew(
        @Header(USERID) userId: Int,
        @Header(EXPIRETOKEN) expireToken: String,
        @Header(AUTHEXPIRE) authExpire: String,
        @Header(MATCH_ID) matchId: Int,
        @Header(PLAYER_RESP) playerRes: String,
        @Header(CAMPAIGN_ID) campaignId: String
    ): Single<BaseModelRummy<String>>

    @POST("v1/user/update-team-name")
    fun updateTeamName(
        @Header(USERID) userId: Int,
        @Header(TEAMNAME) teamName: String,
        @Header(EXPIRETOKEN) expireToken: String,
        @Header(AUTHEXPIRE) authExpire: String,
        @Header(DEVICE_ID_2) deviceID: String
    ): Single<BaseModelRummy<String>>

    @POST("player/usereditsave")
    fun saveEditTeam(
        @Header(USERID) userId: Int,
        @Header(EXPIRETOKEN) expireToken: String,
        @Header(AUTHEXPIRE) authExpire: String,
        @Header(MATCH_ID) matchId: Int,
        @Header(PLAYER_RESP) playerRes: String,
        @Header(TEAM_ID) teamId: Long,
        @Header(CAMPAIGN_ID) campaignId: String
    ): Single<BaseModelRummy<String>>
    @POST("player/usereditsavev1")
    fun saveEditTeamNew(
        @Header(USERID) userId: Int,
        @Header(EXPIRETOKEN) expireToken: String,
        @Header(AUTHEXPIRE) authExpire: String,
        @Header(MATCH_ID) matchId: Int,
        @Header(PLAYER_RESP) playerRes: String,
        @Header(TEAM_ID) teamId: Long,
        @Header(CAMPAIGN_ID) campaignId: String
    ): Single<BaseModelRummy<String>>


    @POST("joinleauge/joinleaguev2")
    fun joinContest(
        @Header(USERID) userId: Int,
        @Header(EXPIRETOKEN) expireToken: String,
        @Header(AUTHEXPIRE) authExpire: String,
        @Header(TEAM_ID) teamId: String,
        @Header(CONTEST_ID) contestId: Int,
        @Header(MATCH_ID) matchId: Int,
        @Header(CAMPAIGN_ID) campaignId: String,
        @Header("PassCount") passCount: Int = 0
    ): Single<BaseModelRummy<String>>


    @POST("goldenticket/purchase")
    fun purchaseCoupon(
        @Header(USERID) userId: Int,
        @Header(EXPIRETOKEN) expireToken: String,
        @Header(AUTHEXPIRE) authExpire: String,
        @Header("PassId") passID: String
    ):Single<BaseModelRummy<String>>

    @POST("quiz/joinquiz")
    fun joinQuiz(
        @Header(USERID) userId: Int,
        @Header(EXPIRETOKEN) expireToken: String,
        @Header(AUTHEXPIRE) authExpire: String,
        @Header(CONTEST_ID) contestId: Int,
        @Header(MATCH_ID) matchId: Int,
        @Header(CAMPAIGN_ID) campaignId: String,
        @Header(LANGUAGE_CODE_1) languageCode: String,
        @Header(TOKEN_FIREBASE_1) FirebaseToken: String = ""
    ): Single<BaseModelRummy<String>>


    @POST("joinleauge/switchteam")
    fun switchTeam(
        @Header(USERID) userId: Int,
        @Header(EXPIRETOKEN) expireToken: String,
        @Header(AUTHEXPIRE) authExpire: String,
        @Header(MATCH_ID) matchId: Int,
        @Header(CONTEST_ID) contestId: Int,
        @Header(TEAM_ID) teamId: Long,
        @Header(OLD_TEAM_ID) oldTeamId: Long
    ): Single<BaseModelRummy<String?>>

    @POST("myprofile/redeem-coupan")
    fun redeemCode(
        @Header(USER_ID) UserId: String,
        @Header(EXPIRE_TOKEN) ExpireToken: String,
        @Header(AUTH_EXPIRE) AuthExpire: String,
        @Header(COUPON) coupon: String,
        @Header(DEVICE_ID_2) deviceId: String
    ): Single<BaseModelRummy<String>>
    @POST("bankaccount/bankdelete")
    fun deleteBank(
        @Header(USER_ID) UserId: String,
        @Header(EXPIRE_TOKEN) ExpireToken: String,
        @Header(AUTH_EXPIRE) AuthExpire: String
    ): Single<BaseModelRummy<Any>>

    @POST("/account/v1/sendverificationmail")
    fun sendVerificationEmail(
        @Header(USER_ID) UserId: String,
        @Header(EXPIRE_TOKEN) ExpireToken: String,
        @Header(AUTH_EXPIRE) AuthExpire: String,
        @Body map:JsonObject
    ): Single<BaseModelRummy<Any>>

    @POST("/account/v1/verify-email")
    fun verifyEmail(
        @Header(USER_ID) UserId: String,
        @Header(EXPIRE_TOKEN) ExpireToken: String,
        @Header(AUTH_EXPIRE) AuthExpire: String,
        @Body map:JsonObject
    ): Single<BaseModelRummy<Any>>

    @GET("/account/v1/email/verificationstatus")
    fun verificationStatus(
        @Header(USER_ID) UserId: String,
        @Header(EXPIRE_TOKEN) ExpireToken: String,
        @Header(AUTH_EXPIRE) AuthExpire: String,
    ): Single<BaseModelRummy<EmailVerificationStatusModel>>

    @POST("myprofile/emailchange")
    fun updateEmailAddress(
        @Header(USER_ID) UserId: String,
        @Header(EXPIRE_TOKEN) ExpireToken: String,
        @Header(AUTH_EXPIRE) AuthExpire: String,
        @Header("NewEmail") newEmail: String
    ): Single<BaseModelRummy<String>>

    @Multipart
    @POST("pancard/pancardupload")
    fun addPanCardDetail(
        @Header(USER_ID) UserId: String,
        @Header(EXPIRE_TOKEN) ExpireToken: String,
        @Header(AUTH_EXPIRE) AuthExpire: String,
        @Part(PAN_NAME) name: RequestBody,
        @Part(PAN_NUMBER) number: RequestBody,
        @Part(PAN_DOB) dob: RequestBody,
        @Part(PAN_STATE) state: RequestBody,
        @Part(PAN_PHOTO + "\"; filename=abc.jpg") file: RequestBody
    ): Single<BaseModelRummy<String>>



    @GET("kyc/v1/aadhar/init")
    fun initAadhaar(
        @Header(USER_ID) UserId: String,
        @Header(EXPIRE_TOKEN) ExpireToken: String,
        @Header(AUTH_EXPIRE) AuthExpire: String,
    ): Single<BaseModelRummy<String>>



    @Multipart
    @POST("bank/uploadbank")
    fun addBankDetail(
        @Header(USER_ID) UserId: String,
        @Header(EXPIRE_TOKEN) ExpireToken: String,
        @Header(AUTH_EXPIRE) AuthExpire: String,
        @Part(BANK_USER_NAME) name: RequestBody,
        @Part(BANK_ACC_NUMBER) bank_acc_number: RequestBody,
        @Part(BANK_IFSC_CODE) ifsc: RequestBody,
        @Part(BANK_NAME) bname: RequestBody,
        @Part(BANK_BRANCH) branch: RequestBody,
        @Part(BANK_PHOTO + "\"; filename=" + "abc.jpg") file: RequestBody
    ): Single<BaseModelRummy<String>>

    @POST("quiz/update-wheel-data")
    fun updateWheelData(
        @Header(USER_ID) UserId: String,
        @Header(EXPIRE_TOKEN) ExpireToken: String,
        @Header(AUTH_EXPIRE) AuthExpire: String,
        @Header("WheelID") SportsType: String
    ): Single<BaseModelRummy<Any>>

    @POST("users/updateuserprofile")
    fun updateProfile(
        @Header(USER_ID) UserId: String,
        @Header(EXPIRE_TOKEN) ExpireToken: String,
        @Header(AUTH_EXPIRE) AuthExpire: String,
        @Header(UserName) name: String,
        @Header(DOB) dob: String,
        @Header(GENDER) gender: String,
        @Header(ADDRESS) address: String,
        @Header(PINCODE) pincode: String,
        @Header(STATE) state: String,
        @Header(EMAIL) email: String
    ): Single<BaseModelRummy<String>>

    @POST("myprofile/updateavtarimage")
    fun changeUserAvtaar(
        @Header(USER_ID) UserId: String,
        @Header(EXPIRE_TOKEN) ExpireToken: String,
        @Header(AUTH_EXPIRE) AuthExpire: String,
        @Header(AVTAR_ID) avtarId: String
    ): Single<BaseModelRummy<String>>

    @POST("users/chnagepassword")
    fun changePassword(
        @Header(USER_ID) UserId: String,
        @Header(EXPIRE_TOKEN) ExpireToken: String,
        @Header(AUTH_EXPIRE) AuthExpire: String,
        @Header(OLD_PASSWORD) oldpassword: String,
        @Header(PASSWORD_2) password: String
    ): Single<BaseModelRummy<String>>



    @POST("transaction/v1/withdraw")
    fun withdrawMoney(
        @Header(USER_ID) UserId: String,
        @Header(EXPIRE_TOKEN) ExpireToken: String,
        @Header(AUTH_EXPIRE) AuthExpire: String,
        @Body map:JsonObject,
        //@Header(AMMOUNT) ammount: String,
        //@Header(WITHDRAWAL_METHOD) methodId: String
    ): Single<BaseModelRummy<String>>

    @POST("myprofile/WdRquestCancel")
    fun cancelWithdrawalDetail(
        @Header(USER_ID) UserId: String,
        @Header(EXPIRE_TOKEN) ExpireToken: String,
        @Header(AUTH_EXPIRE) AuthExpire: String,
        @Header("TnxId") transactionID: String
    ): Single<BaseModelRummy<String>>

    @POST("myprofile/withdrawunutilized")
    fun withdrawUnutilizedAmount(
        @Header(USER_ID) UserId: String,
        @Header(EXPIRE_TOKEN) ExpireToken: String,
        @Header(AUTH_EXPIRE) AuthExpire: String,
        @Header(AMMOUNT) ammount: String,
        @Header(WITHDRAWAL_METHOD) methodId: String
    ): Single<BaseModelRummy<String>>


    @GET("PaymentsAndroid/DeleteCard")
    fun deleteCard(
        @Header(USER_ID) UserId: Int,
        @Header(EXPIRE_TOKEN) ExpireToken: String,
        @Header(AUTH_EXPIRE) AuthExpire: String,
        @Query(CARD_TOKEN) cardToken: String
    ): Single<Boolean>



    @POST("myprofile/savefb")
    fun submitFeedback(
        @Header(USER_ID) UserId: String,
        @Header(EXPIRE_TOKEN) ExpireToken: String,
        @Header(AUTH_EXPIRE) AuthExpire: String,
        @Header(TITLE) title: String,
        @Header(MESSAGE) mes: String,
        @Header(CATEGORY_NAME) category: String
    ): Single<BaseModelRummy<String>>

    @POST("users/saveipaddress")
    fun logout(
        @Query(USER_ID) UserId: String,
        @Query(IP_ADDRESS) ipaddress: String,
        @Query(MAC_ADDRESS) macaddress: String,
        @Query(TYPE) type: String
    ): Single<BaseModelRummy<String>>

    @POST("v1/user/logout-all-devices")
    fun logoutFromAllDevice(
        @Header("UserId") UserId: String,
        @Header("ExpireToken") expire: String,
        @Header("AuthExpire") auth: String
    ): Single<BaseModelRummy<String>>




    @GET("v3/match/pollsave")
    fun submitPoll(
        @Header(USER_ID) UserId: Int,
        @Header(EXPIRE_TOKEN) ExpireToken: String,
        @Header(AUTH_EXPIRE) AuthExpire: String,
        @Header(POLL_ID) pollId: String,
        @Header(POLL_OPTION_ID) optionId: String
    ): Single<BaseModelRummy<String>>


    @GET("v3/match/pollsaveshare")
    fun shareScratchCoupon(
        @Header(USER_ID) UserId: Int,
        @Header(EXPIRE_TOKEN) ExpireToken: String,
        @Header(AUTH_EXPIRE) AuthExpire: String,
        @Header(POLL_ID) pollId: String,
        @Header(POLL_OPTION_ID) optionId: String
    ): Single<BaseModelRummy<String>>

    @POST("v3/language/save")
    fun saveLanguage(
        @Query(USER_ID) UserId: Int,
        @Query(DEVICE_ID) deviceId: String,
        @Query(LANGUAGE_CODE) lamguageCode: String,
        @Query(DEVICE_TYPE) type: String
    ): Single<BaseModelRummy<String>>


    @POST("v3/team/saveteam")
    fun saveFavTeam(
        @Header(USER_ID) UserId: Int,
        @Header(EXPIRE_TOKEN) ExpireToken: String,
        @Header(AUTH_EXPIRE) AuthExpire: String,
        @Header(FAV_TEAM_ID) teamId: Long,
        @Header(TOUR_ID) tourId: Int
    ): Single<BaseModelRummy<String>>


    @POST("myprofile/ScarchCard")
    fun redeemScratchCard(
        @Header(USER_ID) UserId: Int,
        @Header(EXPIRE_TOKEN) ExpireToken: String,
        @Header(AUTH_EXPIRE) AuthExpire: String,
        @Header(ID) cardId: Long
    ): Single<BaseModelRummy<String>>

    @GET("users/savefirebasetoken")
    fun updateFirebaseToken(
        @Header(USER_ID) UserId: Int,
        @Header(EXPIRE_TOKEN) ExpireToken: String,
        @Header(AUTH_EXPIRE) AuthExpire: String,
        @Header(TOKEN_FIREBASE_1) FirebaseToken: String
    ): Single<BaseModelRummy<String>>


    @GET("myprofile/getscarchcard")
    fun getScratchCard(
        @Header(USER_ID) UserId: String,
        @Header(EXPIRE_TOKEN) ExpireToken: String,
        @Header(AUTH_EXPIRE) AuthExpire: String
    ): Single<BaseModelRummy<String>>

    @POST("myprofile/updatescarchcard")
    fun updateScratchCard(
        @Header(USER_ID) UserId: String,
        @Header(EXPIRE_TOKEN) ExpireToken: String,
        @Header(AUTH_EXPIRE) AuthExpire: String
    ): Single<BaseModelRummy<String>>




    @POST("quiz/answerquiz")
    fun submitAnswer(
        @Header(USER_ID) UserId: String,
        @Header(EXPIRE_TOKEN) ExpireToken: String,
        @Header(AUTH_EXPIRE) AuthExpire: String,
        @Header(MATCH_Id) matchId: Int,
        @Header(QUESTION_ID) questionId: Int,
        @Header(OPTION_ID) optionId: Int
    ): Single<BaseModelRummy<Any>>


    @GET("Quiz/CheckQuizCompletTime")
    fun getQuizTime(
        @Header(USER_ID) UserId: String,
        @Header(EXPIRE_TOKEN) ExpireToken: String,
        @Header(AUTH_EXPIRE) AuthExpire: String,
        @Header(MATCH_Id) matchId: Int
    ): Single<BaseModelRummy<String>>


    @POST("coupans/check-availability")
    fun checkCouponCodeAvailability(
        @Header(EXPIRE_TOKEN) ExpireToken: String,
        @Header(AUTH_EXPIRE) AuthExpire: String,
        @Header(USER_ID) UserId: Int,
        @Header(ID) couponId: String
    ): Single<BaseModelRummy<Any>>





    @POST("v1/pinpoint/register")
    fun updateValuesOnPinPoints(
        @Header(USER_ID) UserId: Int,
        @Header(EXPIRE_TOKEN) ExpireToken: String,
        @Header(AUTH_EXPIRE) AuthExpire: String,
        @Header("DemographicData") DemographicData: String,
        @Header("LocationData") LocationData: String,
        @Body raw: JsonObject
    ): Single<BaseModelRummy<Any>>




    @POST("payment/v1/wallet/authenticate")
    fun authenticateWallet(
        @Header(USER_ID) UserId: Int,
        @Header(EXPIRE_TOKEN) ExpireToken: String,
        @Header(AUTH_EXPIRE) AuthExpire: String,
        @Body map:JsonObject
    ): Single<BaseModelRummy<Any>>


    @POST("payment/v1/card/save")
    fun saveDebitCard(
        @Header(USER_ID) UserId: Int,
        @Header(EXPIRE_TOKEN) AuthExpire: String,
        @Header(AUTH_EXPIRE) ExpireToken: String,
        @Body map:JsonObject
    ): Single<BaseModelRummy<String>>


    @POST("payment/v1/wallet/direct-debit")
    fun directDebit(
        @Header(USER_ID) UserId: Int,
        @Header(EXPIRE_TOKEN) AuthExpire: String,
        @Header(AUTH_EXPIRE) ExpireToken: String,
        @Body map:JsonObject
    ): Single<BaseModelRummy<String>>

    @POST("payment/v1/vpa/verify")
    fun verifyUPI(
        @Header(USER_ID) UserId: Int,
        @Header(EXPIRE_TOKEN) AuthExpire: String,
        @Header(AUTH_EXPIRE) ExpireToken: String,
        @Header("UPI") UPI: String,
    ): Single<BaseModelRummy<String>>



    @GET("v2/banner/")
    fun getBanner(
        @Header(USER_ID) UserId: Int,
        @Header(EXPIRETOKEN) AuthExpire: String,
        @Header(AUTHEXPIRE) ExpireToken: String,
    ): Single<BaseModelRummy<ArrayList<HeaderItemModel>>>

    @GET("v2/category/")
    fun getCategories(
        @Header(USER_ID) UserId: Int,
        @Header(EXPIRETOKEN) AuthExpire: String,
        @Header(AUTHEXPIRE) ExpireToken: String,
    ): Single<BaseModelRummy<ArrayList<RummyCategoryModel>>>

    @GET("v2/lobby/list")
    fun getLobbies(
        @Header(USER_ID) UserId: Int,
        @Header(EXPIRETOKEN) AuthExpire: String,
        @Header(AUTHEXPIRE) ExpireToken: String,
        @Query("categoryId")categoryId:String,
    ): Single<BaseModelRummy<ArrayList<RummyLobbyModel>>>

    /*Location*/
    @POST("account/v1/location")
    fun checkUserIsBanned(
        @Header(USER_ID) userId: String,
        @Header(EXPIRE_TOKEN) ExpireToken: String,
        @Header(AUTH_EXPIRE) AuthExpire: String,
        @Body raw: JsonObject
    ): Single<BaseModelRummy<Any>>

    @POST("v2/game/active-match")
    fun checkForActiveMatch(
        @Header(USER_ID) UserId: Int,
        @Header(EXPIRETOKEN) AuthExpire: String,
        @Header(AUTHEXPIRE) ExpireToken: String,
        @Body json:JsonObject
    ): Single<BaseModelRummy<JoinLobbyModel>>

    @GET("match/v1/getmatch")
    fun getSingleMatchDetails(
        @Header(USERID) userId: Int,
        @Header(EXPIRETOKEN) expireToken: String,
        @Header(AUTHEXPIRE) authExpire: String,
        @Header(MATCH_ID) matchId: Int,
        @Header(APP_CODE) appCode: Int=2,
        @Header(MATCH_STATUS) isMatchStatusLive: String=MyConstants.MATCH_NOT_STARTED
    ): Single<BaseModelRummy<MatchModel>>

    @POST("private/leaugecode")
    fun getPrivateContestDetails(
        @Header(USERID) userId: Int,
        @Header(EXPIRETOKEN) expireToken: String,
        @Header(AUTHEXPIRE) authExpire: String,
        @Header(CONTEST_CODE) leaugeCode: String
    ): Single<BaseModelRummy<CreatePrivateContestModel>>

    @POST("v3/wallet/buy-in-range")
    fun confirmLobby(
        @Header(USER_ID) UserId: Int,
        @Header(EXPIRETOKEN) AuthExpire: String,
        @Header(AUTHEXPIRE) ExpireToken: String,
        @Body json:JsonObject
    ): Single<BaseModelRummy<JoinGameConfirmationModel>>

    @POST("v2/game/play")
    fun joinLobby(
        @Header(USER_ID) UserId: Int,
        @Header(EXPIRETOKEN) AuthExpire: String,
        @Header(AUTHEXPIRE) ExpireToken: String,
        @Body json:JsonObject
    ): Single<BaseModelRummy<JoinLobbyModel>>

    @GET("v2/rakeback/details")
    fun getRakeBackDetail(
        @Header(USER_ID) userId: String,
        @Header(EXPIRE_TOKEN) ExpireToken: String,
        @Header(AUTH_EXPIRE) AuthExpire: String,
    ): Single<BaseModelGameRummy<RakeBackDetailModelRummy>>

    @POST("v2/rakeback/reedem")
    fun redeemRakeBack(
        @Header(USER_ID) userId: String,
        @Header(EXPIRE_TOKEN) ExpireToken: String,
        @Header(AUTH_EXPIRE) AuthExpire: String,
        @Body json :JsonObject
    ): Single<BaseModelGameRummy<Any>>

    @FormUrlEncoded
    @POST("/users/save-friends")
    fun saveContacts(
        @Header(USER_ID) UserId: String,
        @Header(EXPIRE_TOKEN) ExpireToken: String,
        @Header(AUTH_EXPIRE) AuthExpire: String,
        @Field(FRIENDS_DATA) list: String,
        @Header("DeviceID") devideId: String
    ): Single<SavedContactModel>

    @GET("v2/games/refer-list")
    fun getReferList(
        @Header(USER_ID) UserId: String,
        @Header(EXPIRE_TOKEN) ExpireToken: String,
        @Header(AUTH_EXPIRE) AuthExpire: String
    ): Single<com.rummytitans.sdk.cardgame.models.ReferModel>

    @GET("games/v1/refer-content")
    fun getReferContent(
        @Header(USER_ID) userId: String,
        @Header(EXPIRE_TOKEN) ExpireToken: String,
        @Header(AUTH_EXPIRE) AuthExpire: String,
    ): Single<BaseModelRummy<ReferContentModel>>

    @GET("/users/get-friends")
    fun getContacts(
        @Header(USER_ID) UserId: String,
        @Header(EXPIRE_TOKEN) ExpireToken: String,
        @Header(AUTH_EXPIRE) AuthExpire: String,
        @Header("DeviceID") devideId: String
    ): Single<MyContactsResponseModel>

    @GET("games/v1/wallet/details")
    fun getWalletDetails(
        @Header(USER_ID) UserId: String,
        @Header(EXPIRE_TOKEN) ExpireToken: String,
        @Header(AUTH_EXPIRE) AuthExpire: String
    ): Single<BaseModelRummy<WalletBalanceModel>>

    @POST("payment/v1/gateway/initialize")
    fun getPaymentGateWay(
        @Header(USERID) userId: Int,
        @Header(EXPIRE_TOKEN) ExpireToken: String,
        @Header(AUTH_EXPIRE) AuthExpire: String,
        @Body map:JsonObject
    ): Single<NewPaymentGateWayModel>

    @GET("transaction/v2/account-balance")
    fun getWalletIno(
        @Header(USER_ID) UserId: String,
        @Header(EXPIRE_TOKEN) ExpireToken: String,
        @Header(AUTH_EXPIRE) AuthExpire: String,
        @Header(LANGUAGE_CODE_1) languageCode: String
    ): Single<BaseModelRummy<WalletInfoModel>>

    @POST("payment/v1/coupan/verify")
    fun verifyAppliedCoupon(
        @Header(USER_ID) userId: String,
        @Header(EXPIRE_TOKEN) ExpireToken: String,
        @Header(AUTH_EXPIRE) AuthExpire: String,
        @Body jsonObject: JsonObject
    ): Single<BaseModelRummy<CouponAppliedModel>>

    @GET("/payment/v1/offers")
    fun getAddCashOfferList(
        @Header(USER_ID) UserId: Int,
        @Header(EXPIRE_TOKEN) ExpireToken: String,
        @Header(AUTH_EXPIRE) AuthExpire: String
    ): Single<BaseModelRummy<AddCashOfferModel>>

    @GET("v1/myteam11/banners")
    fun getHeaders(): Single<HeaderBaseResponse>
    @GET
    fun getCashBonusList(
        @Url url: String,
        @Header(USER_ID) UserId: Int,
        @Header(EXPIRE_TOKEN) ExpireToken: String,
        @Header(AUTH_EXPIRE) AuthExpire: String
    ): Single<BaseModelRummy<ArrayList<CashBonusModel>>>

    @GET("transaction/v2/list/{TypeId}/{PageNo}")
    fun getRecentTransactions(
        @Header(USER_ID) UserId: String,
        @Header(EXPIRE_TOKEN) ExpireToken: String,
        @Header(AUTH_EXPIRE) AuthExpire: String,
        @Path("PageNo") PageNo: String,
        @Path("TypeId") TypeId: Int
    ): Single<BaseModelRummy<ArrayList<TransactionModel>>>

    @GET("transaction/v3/detail/{tranId}/{gameId}")
    fun getTransectionDetails(
        @Header(USER_ID) UserId: String,
        @Header(EXPIRE_TOKEN) ExpireToken: String,
        @Header(AUTH_EXPIRE) AuthExpire: String,
        @Header("TranType") typeId: String,
        @Path("tranId") txtId: String,
        @Path("gameId") gameId: String,
    ): Single<BaseModelRummy<TransactionModel.TransactionListModel>>

    @POST("joinleauge/usableandamountNewv2")
    fun getUsableJoinAmount(
        @Header(USERID) userId: Int,
        @Header(EXPIRETOKEN) expireToken: String,
        @Header(AUTHEXPIRE) authExpire: String,
        @Header(CONTEST_ID) contestId: Int,
        @Header(CONTEST_MEMBERS) contestMembers: Int,
        @Header(CONTEST_FEES) contestFee: Int,
        @Header(LANGUAGE_CODE_1) languageCode: String,
        @Header("teamCount") teamCount: Int,
        @Header("passcount") passcount: Int,
        @Header("Modipasscount") Modipasscount: Int = 0
    ): Single<BaseModelRummy<UsableAmountModel>>


    @GET("transaction/v1/withdrawal/options")
    fun getWithdrawalOptions(
        @Header(USER_ID) UserId: Int,
        @Header(EXPIRE_TOKEN) ExpireToken: String,
        @Header(AUTH_EXPIRE) AuthExpire: String,
        @Query(LANGUAGE_CODE_1) languageCode: String
    ): Single<BaseModelRummy<WithdrawModel>>

    @POST("/user/v2/Withdrawal/tds-calculation")
    fun getTdsOnAmount(
        @Header(USER_ID) UserId: Int,
        @Header(EXPIRE_TOKEN) ExpireToken: String,
        @Header(AUTH_EXPIRE) AuthExpire: String,
        @Header(LANGUAGE_CODE_1) languageCode: String,
        @Body json: JsonObject
    ): Single<BaseModelRummy<List<WithdrawalTdsModel>>>

    @GET("myprofile/Wddetail")
    fun getWithdrawalDetail(
        @Header(USER_ID) UserId: String,
        @Header(EXPIRE_TOKEN) ExpireToken: String,
        @Header(AUTH_EXPIRE) AuthExpire: String,
        @Header("TnxId") transactionID: String
    ): Single<BaseModelRummy<WithdrawalDetailModel>>

    @POST("games/v1/tickets")
    fun getGamesTicket(
        @Header(USER_ID) UserId: Int,
        @Header(AUTH_EXPIRE) ExpireToken: String,
        @Header(EXPIRE_TOKEN) AuthExpire: String,
        @Header("GameId") GameId: Int
    ): Single<BaseModelRummy<GameTicketModel>>

    @GET("platfrom/gamedetails")
    fun getGameDetails(
        @Header(USERID) userId: Int,
        @Header(EXPIRETOKEN) expireToken: String,
        @Header(AUTHEXPIRE) authExpire: String,
        @Header(STATE_NAME2) stateName: String,
        @Header(GAME_ID) gameId: Int,
        @Header(LOCATION_CORDINATE) cordinates: String
    ): Single<BaseModelRummy<GamesResponseModel.GamesModel>>

    @GET("myprofile/gethelpdesk")
    fun getHelpDesk(
        @Header(USER_ID) UserId: String,
        @Header(EXPIRE_TOKEN) ExpireToken: String,
        @Header(AUTH_EXPIRE) AuthExpire: String
    ): Single<BaseModelRummy<HelpDeskModel>>

    @GET("kyc/v1/details")
    fun getVerificationInfo(
        @Header(USER_ID) UserId: String,
        @Header(EXPIRE_TOKEN) ExpireToken: String,
        @Header(AUTH_EXPIRE) AuthExpire: String
    ): Single<BaseModelRummy<ProfileVerificationModel>>


    @POST("match/v1/Segmentdetail")
    fun getDeeplinkUrl(
        @Header(USER_ID) UserId: Int,
        @Header(EXPIRE_TOKEN) AuthExpire: String,
        @Header(AUTH_EXPIRE) ExpireToken: String,
        @Body json:JsonObject
    ): Single<BaseModelRummy<DeeplinkResponseModel>>

    @GET("payment/v1/card/validator")
    fun checkCardInformation(
        @Header(USER_ID) UserId: Int,
        @Header(EXPIRE_TOKEN) AuthExpire: String,
        @Header(AUTH_EXPIRE) ExpireToken: String,
        @Header(CARD_NUMBER) number: String,
        @Header(CARD_TYPE) type: String="1"
    ): Single<com.rummytitans.sdk.cardgame.models.CardValidationModel>

    @PUT("payment/v1/card/delete")
    fun deleteSaveCard(
        @Header(USER_ID) UserId: Int,
        @Header(EXPIRE_TOKEN) ExpireToken: String,
        @Header(AUTH_EXPIRE) AuthExpire: String,
        @Header(CARD_TOKEN) cardToken: String
    ): Single<com.rummytitans.sdk.cardgame.models.DeleteCardModel>

    @POST("payment/v1/wallet/initialize")
    fun sendOtpForLinkWallet(
        @Header(USER_ID) UserId: Int,
        @Header(EXPIRE_TOKEN) ExpireToken: String,
        @Header(AUTH_EXPIRE) AuthExpire: String,
        @Body map:JsonObject
    ): Single<BaseModelRummy<WalletInitializeModel>>

    @GET("myprofile/get-subscription-items")
    fun getSubscriptionList(
        @Header(USERID) userId: Int,
        @Header(EXPIRETOKEN) expireToken: String,
        @Header(AUTHEXPIRE) authExpire: String,
        @Header("Initial") initial: Boolean
    ): Single<BaseModelRummy<ArrayList<SubscriptionItemModel>>>

    @POST("myprofile/save-subscription-items")
    fun saveSubscriptionList(
        @Header(USERID) userId: Int,
        @Header(EXPIRETOKEN) expireToken: String,
        @Header(AUTHEXPIRE) authExpire: String,
        @Header(TOPIC_NAMES) TopicNames: String
    ): Single<com.rummytitans.sdk.cardgame.models.SaveSubscriptionModel>

    @GET("kyc/v1/content")
    fun getKycContent(
        @Header(USER_ID) UserId: String,
        @Header(EXPIRE_TOKEN) ExpireToken: String,
        @Header(AUTH_EXPIRE) AuthExpire: String,
        @Header(LANGUAGE_CODE_1)language:String
    ): Single<BaseModelRummy<AddressKycContentModel>>

    @Multipart
    @POST("profile/v2/upload/address-proof")
    fun uploadAddressProofFromAadhaar(
        @Header(USER_ID) UserId: String,
        @Header(EXPIRE_TOKEN) ExpireToken: String,
        @Header(AUTH_EXPIRE) AuthExpire: String,
        @Part("DocType") DocType:RequestBody,
        @Part(PAN_STATE) state: RequestBody,
        @Part images: ArrayList<MultipartBody.Part>
    ): Single<BaseModelRummy<AddressResponseModel<Boolean>>>

    @POST("kyc/v1/dl/verification")
    fun uploadAddressProofFromDL(
        @Header(USER_ID) UserId: String,
        @Header(EXPIRE_TOKEN) ExpireToken: String,
        @Header(AUTH_EXPIRE) AuthExpire: String,
        @Body jsonBody:JsonObject
    ): Single<BaseModelRummy<AddressResponseModel<Int>>>

    @POST("/kyc/v1/aadhar/basic-verification")
    fun verifyWithAadhaar(
        @Header(USER_ID) UserId: String,
        @Header(EXPIRE_TOKEN) ExpireToken: String,
        @Header(AUTH_EXPIRE) AuthExpire: String,
        @Body json: JsonObject
    ): Single<BaseModelRummy<AddressResponseModel<Int>>>

    @GET("account/v1/states")
    fun getStateList(): Single<BaseModelRummy<ArrayList<StateModel>>>

    @POST("print-log")
    fun printRummyLog(
        @Body json:JsonObject
    ): Single<BaseModelRummy<JsonObject>>

    @POST("payment/v1/calculate/gst")
    fun getGstCalcualtion(
        @Header(USER_ID) UserId: String,
        @Header(EXPIRETOKEN) AuthExpire: String,
        @Header(AUTHEXPIRE) ExpireToken: String,
        @Body json:JsonObject
    ): Single<BaseModelRummy<GstCalculationModel>>

    @POST("profile/v1/update")
    fun updateProfileData(
        @Header(USERID) userId: String,
        @Header(EXPIRETOKEN) expireToken: String,
        @Header(AUTHEXPIRE) authExpire: String,
        @Body json:JsonObject
    ): Single<BaseModelRummy<Any>>

    @GET("transaction/v1/calculate/winning-to-deposit")
    fun getWinningConversionRange(
        @Header(USERID) userId: String,
        @Header(EXPIRETOKEN) expireToken: String,
        @Header(AUTHEXPIRE) authExpire: String,
    ): Single<BaseModelRummy<WinningConversionContentModel>>

    @POST("transaction/v1/convert/winning-to-deposit")
    fun convertToDeposit(
        @Header(USERID) userId: String,
        @Header(EXPIRETOKEN) expireToken: String,
        @Header(AUTHEXPIRE) authExpire: String,
        @Body json: JsonObject
    ): Single<BaseModelRummy<List<WinningConversionContentModel.WinningConversionBenefitModel>>>

    companion object {
        const val TRANSACTION_ID = "TxnId"
        const val JOINED_MATCH_ID = "UserJoinedMatchId"
        const val LEAGUE_ID = "LeagueId"
        const val INDEX = "index"
        const val EMAIL_ADDRESS = "email_address"
        const val PASSWORD = "password_hash"
        const val PASSWORD_2 = "passowrd"
        const val PASSWORD_1 = "Password"
        const val EMAIL = "email"
        const val SID = "sId"
        const val USER_REFER_CODE = "userreferCode"
        const val REFER_CODE = "ReferCode"
        const val TOKEN = "token"
        const val TOKEN_1 = "Token"
        const val TOKEN_FIREBASE = "tokenFireBase"
        const val TOKEN_FIREBASE_1 = "FireBaseToken"
        const val GMAIL_ID = "GmailID"
        const val GMAIL_ACCESS_TOKEN = "GmailAccessToken"
        const val FACEBOOK_ID = "FacebokkId"
        const val FACEBOOK_ACCESS_TOKEN = "FacebookAccessToken"
        const val MOBILE_NUMBER = "Mobile_Number"
        const val MOBILE = "Mobile"
        const val MOBILE_NUMER1 = "MobileNumber"
        const val USERID = "UserId"
        const val OTP = "Otp"
        const val OTP1 = "OTP"
        const val EXPIRETOKEN = "ExpireToken"
        const val AUTHEXPIRE = "AuthExpire"
        const val TEAMNAME = "teamName"
        const val TEAMNAME1 = "Team_Name"
        const val STATE_NAME = "State_Name"
        const val NAME = "Name"
        const val AVATAR_ID = "AvtaarID"
        const val APP_CODE = "appCode"
        const val MATCH_STATUS = "Status"
        const val MATCH_TYPE = "matchType"
        const val MATCH_ID = "matchID"
        const val MATCH_Id = "matchId"
        const val CATEGORY_ID = "catId"
        const val CONTEST_ID = "leaugeId"
        const val TEAM_ID = "teamId"
        const val COMPARE_TEAM_ID = "compareTeamId"
        const val OLD_TEAM_ID = "oldteamId"
        const val PLAYER_RESP = "playersave"
        const val CONTEST_MEMBERS = "member"
        const val CONTEST_FEES = "fee"
        const val CONTEST_CODE = "leaugeCode"
        const val CONTEST_WINNING_AMOUNT = "winningAmount"
        const val CONTEST_IS_MULTIPLE = "isMultiple"
        const val PLAYER_ID = "playerId"
        const val USER_ID = "UserId"
        const val IS_MY_LEAGUE = "myleauge"
        const val EXPIRE_TOKEN = "ExpireToken"
        const val TOPIC_NAMES = "TopicNames"
        const val AUTH_EXPIRE = "AuthExpire"
        const val LOGIN_AUTH_EXPIRE = "LoginAuthToken"
        const val ID = "ID"
        const val FRIENDS_DATA = "FriendsData"
        const val COUPON = "Coupan"
        const val PAN_NUMBER = "panNumber"
        const val PAN_NAME = "name"
        const val PAN_DOB = "dob"
        const val PAN_STATE = "stateName"
        const val PAN_PHOTO = "photo"
        const val BANK_USER_NAME = "userName"
        const val BANK_ACC_NUMBER = "accountNumber"
        const val BANK_IFSC_CODE = "ifscCode"
        const val BANK_NAME = "bankName"
        const val BANK_BRANCH = "branch"
        const val BANK_PHOTO = "photo"
        const val TRANS_TYPE_ID = "typeId"
        const val OLD_PASSWORD = "oldPassowrd"
        const val UserName = "UserName"
        const val DOB = "dob"
        const val GENDER = "gender"
        const val ADDRESS = "address"
        const val PINCODE = "PinCode"
        const val STATE = "state"
        const val AMMOUNT = "amount"
        const val AVTAR_ID = "avtarId"
        const val VERSION = "version"
        const val AMOUNT = "amount"
        const val CARD_AUTH = "Authorization"
        const val CARD_TYPE = "Type"
        const val CARD_NUMBER = "CardNumber"
        const val CARD_CVV = "Cvv"
        const val CARD_EXPIRY = "ExpiryDate"
        const val CARD_TOKEN = "cardToken"
        const val TITLE = "title"
        const val MESSAGE = "message"
        const val IP_ADDRESS = "ipAddress"
        const val MAC_ADDRESS = "macAddress"
        const val TYPE = "type"
        const val SPORT_TYPE = "sportType"
        const val SERIES_ID = "seriesId"
        const val POLL_ID = "pollId"
        const val POLL_OPTION_ID = "optionId"
        const val DEVICE_ID = "deviceId"
        const val DEVICE_ID_2 = "deviceID"
        const val LANGUAGE_CODE = "languageCode"
        const val LANGUAGE_CODE_1 = "langCode"
        const val DEVICE_TYPE = "deviceType"
        const val FAV_TEAM_ID = "teamId"
        const val TOUR_ID = "tourId"
        const val CATEGORY_NAME = "Category"
        const val WITHDRAWAL_METHOD = "WithdrawalOptionID"
        const val INNING = "InningNo"
        const val LANG_CODE = "LanguageCode"
        const val SPORTS_ID = "SportsID"
        const val CAMPAIGN_ID = "campaignId"
        const val CAMPAIGN_ID1 = "CampaignId"
        const val QUESTION_ID = "questionId"
        const val OPTION_ID = "optionId"
        const val COUPON_PASS_ID = "passId"
        const val OFFER_PASS_ID = "OfferIds"
        const val VPA = "vpa"
        const val MERCHANT_ID = "merchant_id"
        const val STATE_NAME2 = "statename"
        const val GAME_ID = "GameId"
        const val LOCATION_CORDINATE = "Coordinate"
        const val DL_NUMBER = "dLNumber"
        const val UPLOAD_DOC_TYPE = "DocType"
        const val LOCATION_COORDINATE = "Coordinate"
        const val LOCATION_COORDINATES = "Coordinate"
        const val LOCATION_STATE = "State"
        const val COORDINATES="Coordinates"


    }

}