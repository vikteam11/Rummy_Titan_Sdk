package com.rummytitans.playcashrummyonline.cardgame.ui.wallet.viewmodel

import com.rummytitans.playcashrummyonline.cardgame.analytics.AnalyticsHelper
import com.rummytitans.playcashrummyonline.cardgame.api.APIInterface
import com.rummytitans.playcashrummyonline.cardgame.data.SharedPreferenceStorage
import com.rummytitans.playcashrummyonline.cardgame.models.LoginResponse
import com.rummytitans.playcashrummyonline.cardgame.models.TransactionModel
import com.rummytitans.playcashrummyonline.cardgame.ui.BaseViewModel
import com.rummytitans.playcashrummyonline.cardgame.utils.ConnectionDetector
import com.rummytitans.playcashrummyonline.cardgame.widget.MyDialog
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
@HiltViewModel
class RecentTransactionViewModel @Inject constructor(
    val prefs: SharedPreferenceStorage,
    val gson: Gson,
    val apiInterface: APIInterface,
    val connectionDetector: ConnectionDetector, val analyticsHelper: AnalyticsHelper
) : BaseViewModel<AddCashNavigator>() {

    val loginResponse: LoginResponse = gson.fromJson(prefs.loginResponse, LoginResponse::class.java)

    private val data = MutableLiveData<ArrayList<TransactionModel.TransactionListModel>>()
    val transactionDetail = MutableLiveData<TransactionModel.TransactionListModel>()

    val sortedTransactions= MutableLiveData<List<TransactionModel.TransactionListModel>>()
    var isLoading = ObservableBoolean(false)
    var myDialog: MyDialog? = null
    var isSwipeLoading = ObservableBoolean(false)

    val regularColor = prefs.regularColor
    val safeColor = prefs.safeColor
    val selectedColor = ObservableField(if (prefs.onSafePlay) safeColor else regularColor)

    var isToolbarShow = ObservableBoolean(false)
    var tabName: String = ""
    val currentBalance = ObservableField(0.0)
    var showSortView = ObservableBoolean(false)
    var isDataSorted = ObservableBoolean(false)
    val isLoadingMoreData = ObservableBoolean(false)
    var currentSortType=0
    var loadNextPage = false
    var pageNo = 1

    fun sortViewToggle(){
        if (isLoading.get()) return
        showSortView.set(!showSortView.get())
    }

    fun updateDateInList(list:List<TransactionModel.TransactionListModel>){
        val map = list.groupBy { it.date }

        map.keys.forEach { key ->
            map[key]?.elementAtOrNull(0)?.isDateShow = true
        }
    }

    fun fetchRecentTransaction() {
        if (!connectionDetector.isConnected) {
            myDialog?.noInternetDialog {
                isLoading.set(true)
                fetchRecentTransaction()
            }
            isLoading.set(false)
            isLoadingMoreData.set(false)
            isSwipeLoading.set(false)
            return
        }

        isLoadingMoreData.set(pageNo > 1)
        loadNextPage = false
        isLoading.set(!isLoadingMoreData.get())
        isSwipeLoading.set(false)
        compositeDisposable.add(
            apiInterface.getRecentTransactions(
                loginResponse.UserId.toString(),
                loginResponse.ExpireToken,
                loginResponse.AuthExpire,
                pageNo.toString(),
                currentSortType
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(({
                    if (it.Status) {
                        val list = ArrayList<TransactionModel.TransactionListModel>()
                        for (data in it.Response) {
                            val model = data
                            for (trans in model.Transaction) {
                                trans.date = model.Date
                                list.add(trans)
                            }
                        }

                        list.forEach { model ->
                            val inputFormatter = SimpleDateFormat(
                                "MM/dd/yyyy",Locale.getDefault())
                            val outputFormatter =
                                SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                            try {
                                val date = inputFormatter.parse(model.Dateonadd)
                                model.date = outputFormatter.format(date)
                            } catch (e: Exception) {
                            }
                        }

                        if(pageNo ==1){
                           data.value = list
                        }else{
                            data.value?.addAll(list)
                        }
                        pageNo++
                        loadNextPage = list.size > 19
                        updateDateInList(data.value?:ArrayList())
                        sortedTransactions.value = data.value
                    } else {
                        navigator.showError(it.Message)
                    }
                    isLoading.set(false)
                    isLoadingMoreData.set(false)
                    isSwipeLoading.set(false)
                    navigator.showMessage(it.Message)
                }), ({
                    isLoading.set(false)
                    isSwipeLoading.set(false)
                    isLoadingMoreData.set(false)
                    navigator.handleError(it)

                }))
        )
    }

    fun fetchTransactionDetails(model: TransactionModel.TransactionListModel) {
        isLoading.set(true)
        if (!connectionDetector.isConnected) {
            myDialog?.noInternetDialog {
                isLoading.set(true)
                fetchTransactionDetails(model)
            }
            isLoading.set(false)
            isSwipeLoading.set(false)
            return
        }

        compositeDisposable.add(
            apiInterface.getTransectionDetails(
                loginResponse.UserId.toString(),
                loginResponse.ExpireToken,
                loginResponse.AuthExpire,
                model.TranType.toString(),
                model.TxnId,
                model.UserJoinedMatchId,
                model.LeagueId
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(({
                    isLoading.set(false)
                    isSwipeLoading.set(false)
                    if (it.Status) {
                        transactionDetail.value = it.Response
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

    fun fetchCurrentBalance() {
        if (!connectionDetector.isConnected) {
            myDialog?.noInternetDialog {
                isLoading.set(true)
                fetchCurrentBalance()
            }
            isLoading.set(false)
            return
        }
        compositeDisposable.add(
            apiInterface.getUsableJoinAmount(
                loginResponse.UserId,
                loginResponse.ExpireToken,
                loginResponse.AuthExpire,
                0, 0, 0,
                prefs.seletedLanguage ?: "en", 1, 0
            ).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    isLoading.set(false)

                    if (it.isUpdate) {
                        navigatorAct.onUpdateRequired(it.updateMessage)
                    }

                    if (it.Status) {
                        currentBalance.set(it.Response.Unutilized + it.Response.Winning)
                    } else {
                        navigator.showError(it.Message)
                    }
                }, {

                    isLoading.set(false)
                    navigator.handleError(it)
                })
        )
    }

}
