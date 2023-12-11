package com.rummytitans.sdk.cardgame.utils

object AnalyticsConstants {
    const val USER_ID = "userId"
}

object AnalyticsEventsKeys {
    const val PAN_VARIFY_REQUESTED = "PanVerifyRequested"
    const val BANK_VARIFY_REQUESTED = "BankVerifyRequested"
    const val EMAIL_VARIFY_REQUESTED = "EmailVerifyRequested"

    const val LOGIN_DONE = "UserLogin"
    const val ACCOUNT_CREATE = "AccountCreate"

    //in case of  comming from forget password
    const val SNP_MOBILE_VARIFICATION_DONE = "SignupMobileVerificationDone"
    const val LGN_MOBILE_VARIFICATION_DONE = "LoginMobileVerificationDone"

    //in case of  comming from forget password
    const val FRG_MOBILE_VARIFICATION_DONE = "ForgetMobileVerificationDone"

    //Branch Event
    const val FirstDeposite = "FirstDeposite" //SHIFTTED ON BACK-END
    const val FirstLeagueJoined = "FirstLeagueJoined"
    const val FirstPracticeLeagueJoined = "FirstPracticeLeagueJoined"
    const val FirstPassPurchased = "FirstPassPurchased"
}