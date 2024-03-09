package com.rummytitans.sdk.cardgame.ui.wallet.viewmodel

import android.os.Environment
import com.rummytitans.sdk.cardgame.analytics.AnalyticsHelper
import com.rummytitans.sdk.cardgame.api.APIInterface
import com.rummytitans.sdk.cardgame.data.SharedPreferenceStorageRummy
import com.rummytitans.sdk.cardgame.models.LoginResponseRummy
import com.rummytitans.sdk.cardgame.models.TransactionModel
import com.rummytitans.sdk.cardgame.ui.BaseViewModel
import com.rummytitans.sdk.cardgame.utils.ConnectionDetector
import com.rummytitans.sdk.cardgame.widget.MyDialog
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.rummytitans.sdk.cardgame.BuildConfig
import com.rummytitans.sdk.cardgame.R
import com.rummytitans.sdk.cardgame.ui.wallet.TransactionItemNavigator
import com.rummytitans.sdk.cardgame.utils.MyConstants
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
@HiltViewModel
class RecentTransactionViewModel @Inject constructor(
    val prefs: SharedPreferenceStorageRummy,
    val gson: Gson,
    val apiInterface: APIInterface,
    val connectionDetector: ConnectionDetector, val analyticsHelper: AnalyticsHelper
) : BaseViewModel<TransactionItemNavigator>() {

    val loginResponse: LoginResponseRummy = gson.fromJson(prefs.loginResponse, LoginResponseRummy::class.java)

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
    var transaction: TransactionModel.TransactionListModel? = null

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
                model.gameId
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
    fun downLoadInvoice() {
        if (!connectionDetector.isConnected) {
            myDialog?.noInternetDialog {
                isLoading.set(true)
                downLoadInvoice()
            }
            isLoading.set(false)
            return
        }
        isLoading.set(true)
        navigator.showMessage(navigator.getStringResource(R.string.msg_downloading_started))
        viewModelScope.launch(Dispatchers.IO) {
            val txnId = transaction?.TxnId?:""
            val gameId = transaction?.gameId?:""
            try {
                val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "RummyTitan_InVoice_$txnId.pdf")
                if(file.exists()){
                    withContext(Dispatchers.Main){
                        isLoading.set(false)
                        navigatorAct.openInvoicePdfFile(file)
                    }
                }else{
                    val fileDownLoadUrl = MyConstants.APP_CURRENT_URL+"transaction/v1/tax-invoice/download/$txnId/$gameId"
                    val client = OkHttpClient().newBuilder().build()
                    val request = Request.Builder()
                        .url(fileDownLoadUrl)
                        .addHeader("apptype", MyConstants.APP_TYPE.toString())
                        .addHeader("appversion", BuildConfig.VERSION_CODE.toString())
                        .addHeader("authexpire", loginResponse.AuthExpire)
                        .addHeader("expiretoken", loginResponse.ExpireToken)
                        .addHeader("isplaystore", "${BuildConfig.isPlayStoreApk}")
                        .addHeader("userid", "${loginResponse.UserId}")
                        .build()
                    val response = client.newCall(request).execute()

                    if (response.isSuccessful) {
                        val responseBody = response.body
                        if (responseBody != null) {
                            val fos = FileOutputStream(file)
                            fos.write(responseBody.bytes())
                            fos.close()
                            withContext(Dispatchers.Main){
                                isLoading.set(false)
                                navigator.showMessage("Invoice Downloaded Successfully")
                                navigatorAct.openInvoicePdfFile(file)
                            }
                        }
                    }
                }

            } catch (e: IOException) {
                e.printStackTrace()
                withContext(Dispatchers.Main){
                    isLoading.set(false)
                }
            }
        }
    }


}
