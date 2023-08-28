package com.rummytitans.playcashrummyonline.cardgame.ui

import com.rummytitans.playcashrummyonline.cardgame.BuildConfig
import com.rummytitans.playcashrummyonline.cardgame.api.APIInterface
import com.rummytitans.playcashrummyonline.cardgame.models.BaseModel
import com.rummytitans.playcashrummyonline.cardgame.utils.ConnectionDetector
import com.rummytitans.playcashrummyonline.cardgame.utils.MyConstants
import com.rummytitans.playcashrummyonline.cardgame.utils.MyConstants.FULL_TIME_TYPE
import com.rummytitans.playcashrummyonline.cardgame.utils.startTimerCountdown
import com.rummytitans.playcashrummyonline.cardgame.utils.startTimerCountdownForQuiz
import com.rummytitans.playcashrummyonline.cardgame.widget.MyDialog
import android.os.CountDownTimer
import android.text.TextUtils
import androidx.databinding.ObservableBoolean
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.rummytitans.playcashrummyonline.cardgame.RummyTitanSDK
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.ref.WeakReference
import java.net.NetworkInterface
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

abstract class BaseViewModel<N>(val conn: ConnectionDetector? = null) : ViewModel() {

    var languageIsEnglish=false
    val _timerText = MutableLiveData<String>()
    val timerText: LiveData<String>
        get() = _timerText

    //use for same xml
    var mListIsEmpty = ObservableBoolean(false)

    //use for child View's xml if api call in activity and data
    // pass to fragment, if error arise provide nodata to fragment.
    var mEmptyResponse = MutableLiveData(false)

    var noDataResponse = MutableLiveData<Byte>(-1)

    private val _timerFinished = MutableLiveData(false)
    val timerFinished: LiveData<Boolean>
        get() = _timerFinished


    val compositeDisposable: CompositeDisposable = CompositeDisposable()
    val reCreatableCompositeDisposable: CompositeDisposable = CompositeDisposable()

    private var timerDisposable: Disposable? = null

    companion object {
        var timerObserver: Disposable? = null
    }

    private var mNavigator: WeakReference<com.rummytitans.playcashrummyonline.cardgame.ui.base.BaseNavigator>? = null
    var navigator: com.rummytitans.playcashrummyonline.cardgame.ui.base.BaseNavigator
        get() = mNavigator?.get()!!
        set(navigator) {
            this.mNavigator = WeakReference(navigator)
        }

    private var mNavigator2: WeakReference<N>? = null
    var navigatorAct: N
        get() = mNavigator2?.get()!!
        set(navigator) {
            mNavigator2 = WeakReference(navigator)
        }
    var mMatchTimerType = FULL_TIME_TYPE


    override fun onCleared() {
        compositeDisposable.dispose()
        reCreatableCompositeDisposable.dispose()
        super.onCleared()
    }

    fun clearDisposable() {
        reCreatableCompositeDisposable.clear()
    }

    fun onMatchTimesUp(){
        timeUpLiveFantasy.value=true
    }


    //used only for live fantasy matches where we don't have remaining time.
    val timeUpLiveFantasy=MutableLiveData(false)

    private var countDownTimer: CountDownTimer? = null

    fun startTimer(startTime: String, currentTime: String = "") {
        if (TextUtils.isEmpty(startTime)) return
        countDownTimer?.cancel()
        countDownTimer = if (TextUtils.isEmpty(currentTime))
            startTimerCountdown(
                startTime,
                _timerText = _timerText,
                _timerFinished = _timerFinished,
                matchTimeType = mMatchTimerType
            )
        else {
            val dateFormat = SimpleDateFormat(
                "dd-MM-yyyy H:m:s",
                Locale.getDefault()
            )
            val timerCount = dateFormat.parse(currentTime)?.time
            startTimerCountdown(
                startTime,
                timerCount,
                _timerText,
                _timerFinished,
                matchTimeType = mMatchTimerType
            )
        }

    }


    fun startTimerForQuiz(
        startTime: String, currentTime: String = "", remainingSeconds: MutableLiveData<Long>
    ) {
        if (TextUtils.isEmpty(startTime)) return
        countDownTimer?.cancel()
        countDownTimer = if (TextUtils.isEmpty(currentTime))
            startTimerCountdown(
                startTime,
                _timerText = _timerText,
                _timerFinished = _timerFinished,
                matchTimeType = mMatchTimerType
            )
        else {
            val dateFormat = SimpleDateFormat("dd-MM-yyyy H:m:s", Locale.getDefault())
            val timerCount = dateFormat.parse(currentTime)?.time
            startTimerCountdownForQuiz(
                startTime,
                timerCount,
                _timerText,
                _timerFinished,
                matchTimeType = mMatchTimerType, remainingSeconds = remainingSeconds
            )
        }

    }

    fun startTimer(
        startTime: String,
        currentTime: String = "",
        _timerMatchCheck: MutableLiveData<Boolean>? = null,
        deadLine: Long? = null
    ) {
        countDownTimer?.cancel()
        countDownTimer = if (TextUtils.isEmpty(currentTime))
            startTimerCountdown(
                startTime,
                _timerText = _timerText,
                _timerFinished = _timerFinished,
                matchTimeType = mMatchTimerType
            )
        else {
            val dateFormat = SimpleDateFormat(
                "dd-MM-yyyy H:m:s",
                Locale.getDefault()
            )
            val timerCount = dateFormat.parse(currentTime)?.time
            startTimerCountdown(
                startTime,
                timerCount,
                _timerText,
                _timerFinished,
                _timerMatchCheck,
                deadLine,
                matchTimeType = mMatchTimerType
            )
        }

    }


    fun startCurrentTimeObserver(currentTime: String) {
        timerObserver?.dispose()
        val dateFormat = SimpleDateFormat(
            "dd-MM-yyyy H:m:s",
            Locale.getDefault()
        )
        try {
            val dateStartTime = dateFormat.parse(currentTime)
            MyConstants.CURRENT_TIME.value = dateStartTime?.time
            timerObserver = Observable.interval(1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe {
                    MyConstants.CURRENT_TIME.value = (MyConstants.CURRENT_TIME.value ?: 0) + 1000
                }
        } catch (e: ParseException) {
            e.printStackTrace()
        }
    }

    var isParentLoading = ObservableBoolean(false)
    var myParentDialog: MyDialog? = null


    @Deprecated("not fully managed.")
    fun <S> apiCall(
        api: Single<BaseModel<S>>,
        success: (it: BaseModel<S>) -> Unit = {},
        failed: (it: Throwable) -> Unit = {},
        expireTokan: (it: BaseModel<S>) -> Unit = {},
        hideMessage: Boolean = false,
        unSuccess: (it: BaseModel<S>) -> Unit = {}
    ) {
        if (conn?.isConnected == false) {
            myParentDialog?.noInternetDialog {
                isParentLoading.set(true)
                apiCall(api, success, failed, expireTokan, hideMessage)
            }
            isParentLoading.set(false)
            return
        }
        isParentLoading.set(true)

        compositeDisposable.add(
            api.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    isParentLoading.set(false)
                    if (it.Status || it.status) {
                        success(it)
                    } else {
                        if (!hideMessage)
                            navigator.showError(it.Message)
                        unSuccess(it)
                        mListIsEmpty.set(true)
                        mEmptyResponse.value = true
                    }
                    if (it.TokenExpire)
                        expireTokan(it)
                }, {
                    failed(it)
                    mListIsEmpty.set(true)
                    mEmptyResponse.value = true
                    isParentLoading.set(false)
                    navigator.handleError(it)
                })
        )
    }


    fun getIpAddress(): String {
        try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (intf in interfaces) {
                val addrs = Collections.list(intf.inetAddresses)
                for (addr in addrs) {
                    if (!addr.isLoopbackAddress) return addr.hostAddress
                }
            }
        } catch (e: Exception) {
        }
        return ""
    }

    fun logoutStatus(apiInterface: APIInterface, userId: Int, androidId: String, type: String) {
        compositeDisposable.add(
            apiInterface.logout(userId.toString(), getIpAddress(), androidId, type)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(({
                    print(it)
                }), ({
                    navigator.handleError(it)
                }))
        )
    }

    fun requestLogoutFromAllDevice(
        apiInterface: APIInterface, userId: Int, expire: String, auth: String, success: () -> Unit
    ) {
        apiCall(apiInterface.logoutFromAllDevice(userId.toString(), expire, auth), { success() })
    }

    protected fun getApiEndPointObject(url: String) =
        getCustomRetrofitObject(url).create(APIInterface::class.java)

    private fun getCustomRetrofitObject(url: String) =
        OkHttpClient.Builder()
            .addInterceptor(getInterceptor())
            .build().let { okHttpClient ->
            Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(okHttpClient)
                .build()
        }

    private fun getInterceptor(): Interceptor {
        return Interceptor { chain: Interceptor.Chain ->
            val request = chain.request()
            val httpUrl = request.url
            val url = httpUrl.newBuilder().build()
            val requestBuilder =
                request.newBuilder().url(url)
                    .addHeader("AppVersion", BuildConfig.VERSION_CODE.toString())
                    .addHeader("AppType", "${RummyTitanSDK.getOption().currentAppType}")
                    .addHeader("GameType", "1")
                    .addHeader("IsPlayStore",BuildConfig.isPlayStoreApk.toString()).build()
            return@Interceptor chain.proceed(requestBuilder)
        }
    }

}