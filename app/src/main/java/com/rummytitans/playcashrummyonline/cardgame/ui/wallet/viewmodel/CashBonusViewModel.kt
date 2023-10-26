package com.rummytitans.playcashrummyonline.cardgame.ui.wallet.viewmodel

import com.rummytitans.playcashrummyonline.cardgame.api.APIInterface
import com.rummytitans.playcashrummyonline.cardgame.data.SharedPreferenceStorageRummy
import com.rummytitans.playcashrummyonline.cardgame.models.CashBonusModel
import com.rummytitans.playcashrummyonline.cardgame.models.LoginResponse
import com.rummytitans.playcashrummyonline.cardgame.ui.BaseViewModel
import com.rummytitans.playcashrummyonline.cardgame.utils.ConnectionDetector
import com.rummytitans.playcashrummyonline.cardgame.widget.MyDialog
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.rummytitans.playcashrummyonline.cardgame.ui.base.BaseNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
@HiltViewModel
class CashBonusViewModel @Inject constructor(
    val prefs: SharedPreferenceStorageRummy,
    val gson: Gson,
    val apiInterface: APIInterface,
    val connectionDetector: ConnectionDetector
) : BaseViewModel<BaseNavigator>() {

    val loginResponse: LoginResponse = gson.fromJson(prefs.loginResponse, LoginResponse::class.java)

    private val data = MutableLiveData<ArrayList<CashBonusModel>>()
    val listmodel: LiveData<ArrayList<CashBonusModel>>
        get() = data
    var isLoading = ObservableBoolean(false)
    var myDialog: MyDialog? = null
    var isSwipeLoading = ObservableBoolean(false)
    var isDataEmpty = ObservableBoolean(false)
    var isAvtivity = ObservableBoolean(false)
    val regularColor = prefs.regularColor
    val safeColor = prefs.safeColor
    val selectedColor = ObservableField(if (prefs.onSafePlay) safeColor else regularColor)
    var title = ""

    fun fetchCashBonus() {
        if (!connectionDetector.isConnected) {
            myDialog?.noInternetDialog {
                isLoading.set(true)
                fetchCashBonus()
            }
            isLoading.set(false)
            isSwipeLoading.set(false)
            return
        }

        compositeDisposable.add(
            apiInterface.getCashBonusList(
                loginResponse.UserId,
                loginResponse.ExpireToken,
                loginResponse.AuthExpire
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(({
                    isLoading.set(false)
                    isSwipeLoading.set(false)
                    if (it.Status) {
                        data.value = it.Response
                        isDataEmpty.set(it.Response.isNullOrEmpty())
                    } else {
                        navigator.showError(it.Message)
                    }

                    navigator.showMessage(it.Message)
                }), ({
                    isLoading.set(false)
                    isSwipeLoading.set(false)
                    navigator.handleError(it)
                }))
        )
    }
}
