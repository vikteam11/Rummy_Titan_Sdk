package com.rummytitans.playcashrummyonline.cardgame.ui.refer.viewmodel

import com.rummytitans.playcashrummyonline.cardgame.R
import com.rummytitans.playcashrummyonline.cardgame.analytics.AnalyticsHelper
import com.rummytitans.playcashrummyonline.cardgame.api.APIInterface
import com.rummytitans.playcashrummyonline.cardgame.data.SharedPreferenceStorageRummy
import com.rummytitans.playcashrummyonline.cardgame.models.LoginResponse
import com.rummytitans.playcashrummyonline.cardgame.models.ReferModel
import com.rummytitans.playcashrummyonline.cardgame.models.TempContactModel
import com.rummytitans.playcashrummyonline.cardgame.ui.BaseViewModel
import com.rummytitans.playcashrummyonline.cardgame.utils.ConnectionDetector
import com.rummytitans.playcashrummyonline.cardgame.utils.copyCode
import com.rummytitans.playcashrummyonline.cardgame.widget.MyDialog
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.rummytitans.playcashrummyonline.cardgame.models.ReferContentModel
import com.rummytitans.playcashrummyonline.cardgame.ui.base.BaseNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class ReferViewModel @Inject constructor(
    val prefs: SharedPreferenceStorageRummy,
    val gson: Gson,
    val apiInterface: APIInterface,
    val connectionDetector: ConnectionDetector, val analyticsHelper: AnalyticsHelper
) : BaseViewModel<BaseNavigator>() {
    var contactLoading = ObservableBoolean(false)
    var isLoading = ObservableBoolean(false)
    var myDialog: MyDialog? = null
    var isSwipeLoading = ObservableBoolean(false)
    val isContactSave = MutableLiveData<Boolean>()
    //show status bar if coming from home
    val showToolBar=ObservableBoolean(false)
    val data = MutableLiveData<ReferModel>()
    val loginResponse: LoginResponse = gson.fromJson(prefs.loginResponse, LoginResponse::class.java)

    val listModel: LiveData<ReferModel>
        get() = data

    var isDataAvailable= ObservableBoolean(false)
    var sharingData= ObservableBoolean(false)

    val regularColor = prefs.regularColor
    val safeColor = prefs.safeColor
    val selectedColor = ObservableField(if (prefs.onSafePlay) safeColor else regularColor)


    var referCode = ObservableField("")
    var referModel = ObservableField<ReferModel>()
    val receivedAmount = MutableLiveData(0)
    val totalAmount = MutableLiveData(0)
    val progress = MutableLiveData(0)

    val isBottomSheetVisible = ObservableBoolean(false)

    private val _referContent = MutableLiveData<ReferContentModel>()
    val referContent:LiveData<ReferContentModel> = _referContent

    fun toggleBottomSheet() {
        isBottomSheetVisible.set(!isBottomSheetVisible.get())
    }

    fun saveContactsApi(list: List<TempContactModel>) {
        val str = Gson().toJson(list)
        val listData = str.replace(" ", "")
        if (!connectionDetector.isConnected) {
            myDialog?.noInternetDialog {
                saveContactsApi(list)
            }
            contactLoading.set(false)
            return
        }
        isLoading.set(false)
        contactLoading.set(true)
        compositeDisposable.add(
            apiInterface.saveContacts(
                loginResponse.UserId.toString(),
                loginResponse.ExpireToken,
                loginResponse.AuthExpire,
                listData,
                prefs.androidId?:""
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(({
                    contactLoading.set(false)
                    if (it.TokenExpire) {
                        logoutStatus(apiInterface, loginResponse.UserId, prefs.androidId ?: "", "0")
                        prefs.loginResponse = gson.toJson(LoginResponse())
                        prefs.loginCompleted = false
                        navigator.logoutUser()
                    }
                    if (it.Status) {
                        isContactSave.value = true
                    } else {
                        navigator.showError(it.Message)
                    }
                }), ({
                    navigator.handleError(it)
                    contactLoading.set(false)
                }))
        )
    }


    fun fetchReferList() {
        if (!connectionDetector.isConnected) {
            myDialog?.noInternetDialog {
                isLoading.set(true)
                fetchReferList()
            }
            isLoading.set(false)
            isSwipeLoading.set(false)
            return
        }

        compositeDisposable.add(
            apiInterface.getReferList(
                loginResponse.UserId.toString(),
                loginResponse.ExpireToken,
                loginResponse.AuthExpire
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(({
                    isSwipeLoading.set(false)
                    isLoading.set(false)
                    if (it.TokenExpire) {
                        logoutStatus(apiInterface, loginResponse.UserId, prefs.androidId ?: "", "0")
                        prefs.loginResponse = gson.toJson(LoginResponse())
                        prefs.loginCompleted = false
                        navigator.logoutUser()
                    }
                    if (it.Status) {
                        data.value = it

                        isDataAvailable.set(it.Response.isNullOrEmpty())
                        if(isDataAvailable.get()){
                            receivedAmount.value = 0
                            totalAmount.value = 0
                            progress.value = 100
                        }else{
                            calculateSums()
                        }
                    } else {
                        navigator.showError(it.Message)
                    }
                    navigator.showMessage(it.Message)
                }), ({
                    isSwipeLoading.set(false)
                    navigator.handleError(it)
                    isLoading.set(false)
                }))
        )
    }

    fun fetchReferContent() {

        if (!connectionDetector.isConnected) {
            myDialog?.noInternetDialog {
                isLoading.set(true)
                fetchReferContent()
            }
            isLoading.set(false)
            return
        }
        val apis = getApiEndPointObject(prefs.appUrl2 ?:"")
        isLoading.set(true)
        compositeDisposable.add(
            apis.getReferContent(
                loginResponse.UserId.toString(),
                loginResponse.ExpireToken,
                loginResponse.AuthExpire
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(({
                    isLoading.set(false)
                    if (it.TokenExpire) {
                        logoutStatus(apiInterface, loginResponse.UserId, prefs.androidId ?: "", "0")
                        prefs.loginResponse = gson.toJson(LoginResponse())
                        prefs.loginCompleted = false
                        navigator.logoutUser()
                    }
                    if (it.Status) {
                        prefs.referShareMessage = it.Response.referralShareMessage
                        referCode.set(it.Response.referCode)
                        _referContent.value = it.Response
                    } else {
                        navigator.showError(it.Message)
                    }
                }), ({
                    navigator.handleError(it)
                    isLoading.set(false)
                }))
        )
    }

    private fun calculateSums() {
        receivedAmount.value = 0
        totalAmount.value = 0
        progress.value = 0

        data.value?.let {
            if (!it.Response.isNullOrEmpty())
                for (values in it.Response) {
                    receivedAmount.value = receivedAmount.value?.plus(values.Amount)
                    totalAmount.value = totalAmount.value?.plus(values.MaxAmount)
                }
            if (totalAmount.value != 0) {
                progress.value = ((receivedAmount.value!!.toFloat() / totalAmount.value!!.toFloat()) * 100).roundToInt()
            }
        }
    }

}
