package com.rummytitans.playcashrummyonline.cardgame.ui.contectList.viewModel

import com.rummytitans.playcashrummyonline.cardgame.api.APIInterface
import com.rummytitans.playcashrummyonline.cardgame.data.SharedPreferenceStorage
import com.rummytitans.playcashrummyonline.cardgame.models.ContactModel
import com.rummytitans.playcashrummyonline.cardgame.models.LoginResponse
import com.rummytitans.playcashrummyonline.cardgame.models.MyContactsResponseModel
import com.rummytitans.playcashrummyonline.cardgame.models.TempContactModel
import com.rummytitans.playcashrummyonline.cardgame.ui.BaseViewModel
import com.rummytitans.playcashrummyonline.cardgame.ui.contectList.ContactListActivity
import com.rummytitans.playcashrummyonline.cardgame.utils.ConnectionDetector
import com.rummytitans.playcashrummyonline.cardgame.widget.MyDialog
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.rummytitans.playcashrummyonline.cardgame.ui.base.BaseNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

@HiltViewModel
class ContactsViewModel @Inject constructor(
    val prefs: SharedPreferenceStorage,
    val gson: Gson,
    val apiInterface: APIInterface,
    val connectionDetector: ConnectionDetector
) : BaseViewModel<BaseNavigator>() {

    var isAlertContactOpen = false
    val loginResponse: LoginResponse = gson.fromJson(prefs.loginResponse, LoginResponse::class.java)
    var isLoading = ObservableBoolean(false)
    var myDialog: MyDialog? = null
    val regularColor = prefs.regularColor
    val safeColor = prefs.safeColor
    val selectedColor = ObservableField(if (prefs.onSafePlay) safeColor else regularColor)

    var searchList = MutableLiveData<ArrayList<ContactModel>>()

    var invitesCount = ObservableField<String>()
    val mResponseData = MutableLiveData<MyContactsResponseModel>()
    var selectedContacts = ArrayList<ContactModel>()
    var referCode = ""

    init {
        invitesCount.set("")
    }

    fun getContactsApi() {
        isLoading.set(true)
        if (!connectionDetector.isConnected) {
            myDialog?.noInternetDialog {
                isLoading.set(true)
                getContactsApi()
            }
            isLoading.set(false)
            return
        }

        compositeDisposable.add(
            apiInterface.getContacts(
                loginResponse.UserId.toString(),
                loginResponse.ExpireToken,
                loginResponse.AuthExpire,
                prefs.androidId ?: ""
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(({
                    if (it.status) {
                        mResponseData.value = it
                        isLoading.set(false)
                    }else navigator.showError(it.message)
                    /* if (it.TokenExpire) {
                         logoutStatus(apiInterface, loginResponse.UserId, prefs.androidId ?: "", "0")
                         prefs.loginResponse = gson.toJson(LoginResponse())
                         prefs.loginCompleted = false
                         navigator.logoutUser()
                     }*/
                    /* if (it.Status) {
                         data.value = it
                         calculateSums()
                         isDataAvailable.set(it.Response.isNullOrEmpty())

                     } else {
                         navigator.showError(it.Message)
                     }
                     navigator.showMessage(it.Message)*/
                }), ({
                    navigator.handleError(it)
                    isLoading.set(false)
                }))
        )
    }

    fun saveContectsApi(tempList: List<TempContactModel>) {
        isLoading.set(true)
        val str = gson.toJson(tempList)
        println(str)
        if (!connectionDetector.isConnected) {
            myDialog?.noInternetDialog {
                isLoading.set(true)
                saveContectsApi(tempList)
            }
            isLoading.set(false)
            return
        }

        compositeDisposable.add(
            apiInterface.saveContacts(
                loginResponse.UserId.toString(),
                loginResponse.ExpireToken,
                loginResponse.AuthExpire,
                str,
                prefs.androidId ?: ""
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
                    if (it?.Status == true) {
                        getContactsApi()
                    } else {
                        navigator.showError(it.Message)
                    }
                    /* if (it.TokenExpire) {
                         logoutStatus(apiInterface, loginResponse.UserId, prefs.androidId ?: "", "0")
                         prefs.loginResponse = gson.toJson(LoginResponse())
                         prefs.loginCompleted = false
                         navigator.logoutUser()
                     }*/
                    /* if (it.Status) {
                         data.value = it
                         calculateSums()
                         isDataAvailable.set(it.Response.isNullOrEmpty())

                     } else {
                         navigator.showError(it.Message)
                     }
                     navigator.showMessage(it.Message)*/
                }), ({
                    navigator.handleError(it)
                    isLoading.set(false)
                }))
        )
    }

    fun filterList(s: CharSequence) {
        var list:ArrayList<ContactModel>? = ArrayList()
        if (s.isNotEmpty()) {
            val data: MyContactsResponseModel? = mResponseData.value
            list = data?.list?.filter {
                it.name.contains(s, true)
            } as? ArrayList<ContactModel>
        }
        searchList.value = list
    }

    fun onInviteBtn() {
        (navigator as ContactListActivity).sendMsg()
    }

    fun onBackPressed() {
        navigator.goBack()
    }

}