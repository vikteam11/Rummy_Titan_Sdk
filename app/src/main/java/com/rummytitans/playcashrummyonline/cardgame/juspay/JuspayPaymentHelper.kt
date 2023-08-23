package com.rummytitans.playcashrummyonline.cardgame.juspay

import `in`.juspay.hypersdk.core.PaymentConstants
import `in`.juspay.hypersdk.data.JuspayResponseHandler
import `in`.juspay.hypersdk.ui.HyperPaymentsCallbackAdapter
import `in`.juspay.services.HyperServices
import com.rummytitans.playcashrummyonline.cardgame.BuildConfig
import com.rummytitans.playcashrummyonline.cardgame.utils.ConnectionDetector
import com.rummytitans.playcashrummyonline.cardgame.widget.MyDialog
import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import com.rummytitans.playcashrummyonline.cardgame.R
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class JuspayPaymentHelper(
    private val context: Context,
    private val hyperInstance: HyperServices,
    private val listener: PaymentListener,
    private val connectionDetector: ConnectionDetector?
) {
    private var requestId = ""
    private val JusPayService = "in.juspay.hyperapi"
    var orderId = ""
    var clientAuthToken = ""
    val endUrls = JSONArray()
    var dialog:MyDialog?=null

    /**used to maintain payment type
     * exa:on response show bottomSheet
     * in case of upi*/
    var paymentResultShowBottomSheet=""

    companion object{
        const val PAYMENT_TYPE_UPI="UPI"
        const val PAYMENT_TYPE_NATIVE_APP="nativeApp"
        const val PAYMENT_STATUS_SUCCESS=1
        const val PAYMENT_STATUS_PENDING=2
        const val PAYMENT_STATUS_FAILED=3
    }

    init {
        requestId = UUID.randomUUID().toString()
        endUrls.apply {
            put("https://web.myteam11.com/Home/AndroidSucess")
            put("https://web.myteam11.com/Home/AndroidFailed")
        }
        preFetch()
        kotlin.runCatching {
            dialog=MyDialog(context as Activity)
        }
    }

    private fun preFetch() {
        val payload = JSONObject()
        payload.put("requestId", requestId)
        payload.put(PaymentConstants.SERVICE, JusPayService)
        payload.put(
            PaymentConstants.PAYLOAD, JSONObject().apply { put("clientId", "myteam11-android") })
        HyperServices.preFetch(context, payload)
    }

    fun initJuspay(customerId: String, orderId: String, clientAuthToken: String) {
        this.orderId = orderId
        this.clientAuthToken = clientAuthToken
        val initiationPayload = JSONObject()
        initiationPayload.put("requestId", requestId)
        initiationPayload.put(PaymentConstants.SERVICE, JusPayService)

        val payload = JSONObject()
        payload.put("action", "initiate")
        payload.put("merchantId", "myteam11")
        payload.put("clientId", "myteam11-android")
        payload.put("customerId", customerId)
        payload.put(PaymentConstants.ENV,if (BuildConfig.DEBUG) "sandbox" else "prod")//"prod")//
        initiationPayload.put(PaymentConstants.PAYLOAD, payload)

        hyperInstance.initiate(initiationPayload, object : HyperPaymentsCallbackAdapter() {
            override fun onEvent(data: JSONObject, handler: JuspayResponseHandler) {
                try {
                    println("JusPay Response ---> $data")
                    when (data.getString("event")) {
                        "show_loader" -> {
                            listener.showLoader()
                        }
                        "hide_loader" -> {
                            listener.hideLoader()
                        }
                        "initiate_result" -> {
                            // val response = data.optJSONObject("payload")
                        }
                        "process_result" -> {
                            val error = data.getBoolean("error")
                            val errorMessage = data.optString("errorMessage")
                            val status = data.optJSONObject("payload")?.optString("status")
                            listener.hideLoader()
                            if (paymentResultShowBottomSheet== PAYMENT_TYPE_UPI || paymentResultShowBottomSheet== PAYMENT_TYPE_NATIVE_APP) {
                                var paymentErrorMessage=""
                                val paymentType = if (error){
                                    /**not fully managed
                                     * PENDING
                                     * CANCELED
                                     * FAILED*/
                                    paymentErrorMessage=when (status) {
                                        "AUTHORIZATION_FAILED","AUTHENTICATION_FAILED" -> context.getString(R.string.payment_failed_msg)
                                        "PENDING_VBV" ->  context.getString(R.string.payment_pending_msg)
                                        else-> context.getString(R.string.payment_pending_msg)
                                    }
                                    PAYMENT_STATUS_FAILED
                                }else if(!error && status == "CHARGED")
                                    PAYMENT_STATUS_SUCCESS
                                else
                                    PAYMENT_STATUS_FAILED

                                listener.onUpiPaymentResponseReceive(
                                    paymentType,
                                    paymentErrorMessage
                                )
                            }else
                                listener.onPaymentStatusReceived(!error && status == "CHARGED",errorMessage)
                            paymentResultShowBottomSheet=""
                            /* if (!error && status == "CHARGED") listner.onPaymentSuccess()
                             else listner.onPaymentFailure(errorMessage)*/
                        }
                    }
                } catch (e: Exception) {
                    listener.hideLoader()
                    listener.onPaymentFailure("Something went wrong.")
                }
            }
        })
    }

    fun cancelPayment() {
        hyperInstance.terminate()
    }

    fun startWalletPayment(walletName: String, sdkName: String,paymentType:String="") {
        paymentResultShowBottomSheet=paymentType
        Handler(Looper.getMainLooper()).postDelayed({ listener.hideLoader() }, 4000)

        val payload = JSONObject().apply {
            put("action", "walletTxn")
            put("paymentMethod", walletName)
            put("sdkPresent", sdkName)
        }
        processPayment(payload)
    }

    fun startNetBankingPayment(bankName: String) {
        val payload = JSONObject().apply {
            put("action", "nbTxn")
            put("paymentMethod", bankName)
        }
        processPayment(payload)
    }

    fun startUPIAppPayment(appPackageName: String) {
        paymentResultShowBottomSheet=PAYMENT_TYPE_UPI
        val payload = JSONObject().apply {
            put("action", "upiTxn")
            put("upiSdkPresent", true)
            put("showLoader", false)
            put("payWithApp", appPackageName)
            put("displayNote", "MyTeam11")
        }
        processPayment(payload)
    }

    fun startUPIAddressPayment(upiAddress: String) {
        val payload = JSONObject().apply {
            put("action", "upiTxn")
            put("custVpa", upiAddress)
            put("upiSdkPresent", false)
            put("displayNote", "MyTeam11")
        }
        processPayment(payload)
    }

    fun startNewCardPayment(
        number: String, month: String, year: String, code: String, save: Boolean
    ) {
        val payload = JSONObject().apply {
            put("action", "cardTxn")
            put("paymentMethod", "VISA")
            put("cardNumber", number)
            put("cardExpMonth", month)
            put("cardExpYear", year)
            put("cardSecurityCode", code)
            put("saveToLocker", save)
        }
        processPayment(payload)
    }

    fun startSavedCardPayment(cardToken: String, securityCode: String) {
        val payload = JSONObject().apply {
            put("action", "cardTxn")
            put("paymentMethod", "VISA")
            put("cardToken", cardToken)
            put("cardSecurityCode", securityCode)
        }
        processPayment(payload)
    }

    private fun processPayment(payload: JSONObject) {
        connectionDetector?.let {
            if (!connectionDetector.isConnected) {
                dialog?.retryInternetDialog { processPayment(payload) }
                return
            }
        }
        listener.showLoader()
        val processPayload = JSONObject()
        processPayload.put("requestId", UUID.randomUUID().toString())
        processPayload.put(PaymentConstants.SERVICE, JusPayService)
        payload.put("orderId", orderId)
        payload.put("endUrls", endUrls)
        payload.put("clientAuthToken", clientAuthToken)
        processPayload.put(PaymentConstants.PAYLOAD, payload)
        if (hyperInstance.isInitialised) hyperInstance.process(processPayload)
        else listener.onPaymentFailure("SDK not initialized")
    }
}

interface PaymentListener {
    fun onPaymentStatusReceived(paymentStatus:Boolean,reason: String?)
    fun onUpiPaymentResponseReceive(paymentStatus:Int,reason: String?){}
    fun onPaymentSuccess()
    fun onPaymentFailure(reason: String)
    fun showLoader()
    fun hideLoader()
}