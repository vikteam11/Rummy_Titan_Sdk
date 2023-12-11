package com.rummytitans.sdk.cardgame.utils.otp_read

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import java.util.regex.Pattern

class OtpReceiver : BroadcastReceiver() {
    private var otpListener: OTPReceiveListener? = null
    fun setOTPListener(otpListener: OTPReceiveListener) {
        this.otpListener = otpListener
    }

    /*<#> Your ExampleApp code is: 123ABC78 FA+9qCX9VSu*/
    override fun onReceive(context: Context, intent: Intent) {
        if (SmsRetriever.SMS_RETRIEVED_ACTION == intent.action) {
            val extras = intent.extras
            val status = extras?.get(SmsRetriever.EXTRA_STATUS) as Status?
            when (status?.statusCode) {
                CommonStatusCodes.SUCCESS -> {
                    //This is the full message
                    val message = extras?.get(SmsRetriever.EXTRA_SMS_MESSAGE) as String?
                    if (otpListener != null) {
                        val mPat = Pattern.compile("(( ).[0-9].{0,5})")
                        val matcher = mPat.matcher(message.toString())
                        if (matcher.find()) {
                            val otp = matcher.group().trim()
                            otpListener?.onOTPReceived(otp)
                        }
                    }
                }
            }
        }
    }

    interface OTPReceiveListener {
        fun onOTPReceived(otp: String?)
        fun onNumberReceived(number: String, status: String){}
    }
}