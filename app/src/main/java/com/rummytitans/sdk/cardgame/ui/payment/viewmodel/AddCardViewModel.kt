package com.rummytitans.sdk.cardgame.ui.payment.viewmodel

import com.rummytitans.sdk.cardgame.analytics.AnalyticsHelper
import com.rummytitans.sdk.cardgame.api.APIInterface
import com.rummytitans.sdk.cardgame.data.SharedPreferenceStorageRummy
import com.rummytitans.sdk.cardgame.models.LoginResponseRummy
import com.rummytitans.sdk.cardgame.ui.BaseViewModel
import com.rummytitans.sdk.cardgame.ui.payment.AddCardNavigator
import com.rummytitans.sdk.cardgame.utils.ConnectionDetector
import com.rummytitans.sdk.cardgame.utils.getCardImage
import com.rummytitans.sdk.cardgame.widget.MyDialog
import android.view.View
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.rummytitans.sdk.cardgame.utils.MyConstants
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
@HiltViewModel
class AddCardViewModel @Inject constructor(
    val prefs: SharedPreferenceStorageRummy,
    val apis: APIInterface,
    val gson: Gson, val analyticsHelper: AnalyticsHelper,
    val connectionDetector: ConnectionDetector
) : BaseViewModel<AddCardNavigator>() {

    var isLoading = MutableLiveData(false)
    var cardApi: APIInterface? = null
    var cardLength = ObservableInt(3)
    val isMestroCard=ObservableBoolean(false)
    val cardValidImage = ObservableInt(0)
    var _yearList = MutableLiveData<ArrayList<String>>(ArrayList())
    val yearList: LiveData<ArrayList<String>> get() = _yearList
    var monthList = ObservableField<ArrayList<String>>(ArrayList())
    var selectedYear: Int = 0
    var selectedMonth: Int = 0

    var loginResponse: LoginResponseRummy = gson.fromJson(prefs.loginResponse ?: "", LoginResponseRummy::class.java)
    var amount = ObservableField(0.0)
    var myDialog: MyDialog? = null


    val checked = ObservableBoolean(false)
    val isCardValid = ObservableBoolean(false)
    val showPayButton = ObservableBoolean(false)

    val regularColor = prefs.regularColor
    val safeColor = prefs.safeColor
    val selectedColor = ObservableField(if (prefs.onSafePlay) safeColor else regularColor)
    var isNewCardSaved = false
    var returnUrl = ""

    fun goBack() {
        navigator.goBack()
    }

    fun onCheckedChange(v: View, b: Boolean) {
        checked.set(b)
    }


    fun checkCardDetail(number: String, isSubmit: Boolean = false) {
        if (!connectionDetector.isConnected) {
            myDialog?.noInternetDialog {
                isLoading.value=true
                checkCardDetail(number,isSubmit)
            }
            isLoading.value=true
            return
        }

        if (cardApi == null) {
            cardApi = getApiEndPointObject(prefs.appUrl2?: MyConstants.APP_CURRENT_URL)
        }
        isLoading.value=true
        cardApi?.let {api->
            compositeDisposable.add(
                api.checkCardInformation(
                    loginResponse.UserId,
                    loginResponse.ExpireToken,
                    loginResponse.AuthExpire,
                    number
                ).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        isLoading.value=false
                        if (it.Status) {
                            cardLength.set(it.Result.cvv_length)
                            val (type,isMaestroCardType)=getCardImage(it.Result.card_type)

                            cardValidImage.set(type)
                            isMestroCard.set(isMaestroCardType)
                            isCardValid.set(it.Result.valid)

                            if (it.Result.valid and isSubmit) {
                                navigatorAct.goForPay()
                            }else{
                                if (!it.Result.valid)
                                    navigatorAct.onInvalidCardNumber()
                            }
                        } else {
                            cardValidImage.set(0)
                            isCardValid.set(false)
                            isMestroCard.set(false)
                        }
                    }, {
                        isLoading.value=false
                        isCardValid.set(false)
                        isMestroCard.set(false)
                    })
            )
        }
    }

    fun saveCard(number: String, name: String) {
        isLoading.value=true
        val json = JsonObject()
        json.addProperty("CardNumber",number)
        json.addProperty("Name",name)
        json.addProperty("Year",selectedYear)
        json.addProperty("NickName","")
        json.addProperty("Month",selectedMonth)
        loginResponse = gson.fromJson(prefs.loginResponse, LoginResponseRummy::class.java)
        compositeDisposable.add(
            apis.saveDebitCard(
                loginResponse.UserId,
                loginResponse.ExpireToken,
                loginResponse.AuthExpire,
                json
            ).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    isLoading.value=false
                    if (it.Status) {
                        isNewCardSaved = true
                        navigatorAct.startPayment()
                    } else {
                        cardValidImage.set(0)
                        isCardValid.set(false)
                        isMestroCard.set(false)
                    }
                }, {
                    isLoading.value=false
                    isCardValid.set(false)
                    isMestroCard.set(false)
                })
        )
    }


}