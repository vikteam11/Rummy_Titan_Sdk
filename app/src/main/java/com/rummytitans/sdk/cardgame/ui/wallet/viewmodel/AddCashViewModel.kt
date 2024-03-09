package com.rummytitans.sdk.cardgame.ui.wallet.viewmodel

import com.rummytitans.sdk.cardgame.analytics.AnalyticsHelper
import com.rummytitans.sdk.cardgame.api.APIInterface
import com.rummytitans.sdk.cardgame.api.APIInterface.Companion.LOCATION_COORDINATE
import com.rummytitans.sdk.cardgame.data.SharedPreferenceStorageRummy
import com.rummytitans.sdk.cardgame.models.*
import com.rummytitans.sdk.cardgame.ui.BaseViewModel
import com.rummytitans.sdk.cardgame.utils.ConnectionDetector
import com.rummytitans.sdk.cardgame.utils.MyConstants
import com.rummytitans.sdk.cardgame.widget.MyDialog
import android.text.TextUtils
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.rummytitans.sdk.cardgame.R
import com.rummytitans.sdk.cardgame.utils.setAddMoreGatewayItem
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
@HiltViewModel
class AddCashViewModel @Inject constructor(
    val prefs: SharedPreferenceStorageRummy,
    val apis: APIInterface,
    val gson: Gson,
    val connectionDetector: ConnectionDetector, val analyticsHelper: AnalyticsHelper
) :

    BaseViewModel<AddCashNavigator>() {
    val currentBalance = ObservableField(0.0)
    val addCashAmmount = ObservableField<Double>(MyConstants.DEFAULT_ADD_CASH_AMOUNT)
    var loginResponse: LoginResponseRummy = gson.fromJson(prefs.loginResponse, LoginResponseRummy::class.java)
    var isLoading = ObservableBoolean(false)
    var myDialog: MyDialog? = null
    var isRedeemCoupon = ObservableBoolean(false)
    var isCouponsListEmpty = ObservableBoolean(true)
    var isOfferListEmpty = ObservableBoolean(true)
    var ticketsAvailable=ObservableBoolean(false)
    var offers = MutableLiveData(AddCashOfferModel())
    var couponApplied = MutableLiveData<Boolean>()

    val couponAppliedlivedata: LiveData<Boolean> get() = couponApplied

    var balanceModel: WalletInfoModel.Balance? = null

    val offerTitle = ObservableField<String>("")
    val offerDescription = ObservableField<String>("")

    val regularColor = prefs.regularColor
    val safeColor = prefs.safeColor
    val selectedColor = ObservableField(if (prefs.onSafePlay) safeColor else regularColor)

    val isBottomSheetVisible = ObservableBoolean(false)
    var couponId: Int = 0
    var offerIds: String = ""
    var coupon = ""
    var isAddressVerified=true
    var addressVerificationRejectMsg=""
    val cardListCount = ObservableInt(0)

    var isAllOfferSelected = ObservableBoolean(false)

    var _mAvailableCoupons = MutableLiveData<ArrayList<AvailableCouponModel>>()
    val mAvailableCoupons: LiveData<ArrayList<AvailableCouponModel>>
        get() = _mAvailableCoupons

    private val _mAddCashOffer = MutableLiveData<ArrayList<AddCashOfferModel.AddCash>>()
    val mAddCashOffer : LiveData<ArrayList<AddCashOfferModel.AddCash>> = _mAddCashOffer

    private val _mBannerOffer = MutableLiveData<ArrayList<WalletInfoModel.Offer>>(ArrayList())
    val mBannerOffer: LiveData<ArrayList<WalletInfoModel.Offer>> get() = _mBannerOffer

    var userCurrentState=""
    var userCurrentStateLatLog=""

    var locationModel: UserCurrentLocationModel?=null

    var allowAutoScrolling = true
    var viewScrollingTime:Long = 5

    val isGstCardVisible = ObservableBoolean(false)
    val isGstCalculating = ObservableBoolean(false)
    private val _gstList = MutableLiveData<List<GstCalculationModel.GSTBifurcationItem>>()
    val gstList: LiveData<List<GstCalculationModel.GSTBifurcationItem>> = _gstList
    val gstModel = MutableLiveData<GstCalculationModel>()
    var enableGstCalculation = ObservableBoolean(false)
    fun hideSheet() {
        isBottomSheetVisible.set(false)
    }

    fun getStringFromDouble(amount: Double?) = "%.2f".format(amount ?: "0")

    fun selectAllOffer(){
        isAllOfferSelected.set(!isAllOfferSelected.get())
        navigatorAct.selectAllOffer(isAllOfferSelected.get())
    }
    fun showSheet() {
        isBottomSheetVisible.set(true)
    }

    fun minusAmount() {
        navigatorAct.addAmount(false)
    }

    fun addAmount() {
        navigatorAct.addAmount(true)
    }

    fun addCustomAmount(amount: Int) {
        val anyOfferSelected = mAddCashOffer.value?.any { it.isSelected }?:false
        if(!anyOfferSelected){
            navigatorAct.addAmount(amount)
        }
    }
    fun getCurentBalance() {
        if (!connectionDetector.isConnected) {
            myDialog?.noInternetDialog {
                isLoading.set(true)
                getCurentBalance()
            }
            isLoading.set(false)
            return
        }
        isLoading.set(true)
        compositeDisposable.add(
            apis.getWalletIno(
                loginResponse.UserId.toString(),
                loginResponse.ExpireToken,
                loginResponse.AuthExpire,
                prefs.seletedLanguage ?: "en"
            ).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    isLoading.set(false)
                    if (it.TokenExpire) {
                        logoutStatus(apis, loginResponse.UserId, prefs.androidId ?: "", "0")
                        prefs.loginResponse = gson.toJson(LoginResponseRummy())
                        prefs.loginCompleted = false
                        navigator.logoutUser()
                    }
                    if (it.isUpdate) {
                        navigatorAct.onUpdateRequired(it.updateMessage)
                    }
                    isAddressVerified=it.IsAddressVerified
                    addressVerificationRejectMsg=it.Message?:""
                    navigatorAct.collectPlayStoreRequiredData()
                    if (it.Status) {
                        if(addCashAmmount.get() == MyConstants.DEFAULT_ADD_CASH_AMOUNT){
                            addCashAmmount.set(it.Response.AddCashAmount)
                            navigatorAct.setAmount(it.Response.AddCashAmount)
                        }
                        currentBalance.set(it.Response.Balance.TotalAmount)
                    } else {
                        navigator.showError(it.Message)
                    }
                }, {
                    isLoading.set(false)
                    navigator.handleError(it)
                })
        )
    }

    fun checkCouponCodeAvailability(couponId: Int) {
        if (!connectionDetector.isConnected) {
            myDialog?.noInternetDialog {
                isLoading.set(true)
                checkCouponCodeAvailability(couponId)
            }
            isLoading.set(false)
            return
        }
        val json = JsonObject()
        json.addProperty("CoupanId",couponId)
        json.addProperty("Amount",addCashAmmount.get())
        compositeDisposable.add(
            apis.verifyAppliedCoupon(
                loginResponse.UserId.toString(),
                loginResponse.ExpireToken,
                loginResponse.AuthExpire,
                json
            ).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    if (it.TokenExpire) {
                        logoutStatus(apis, loginResponse.UserId, prefs.androidId ?: "", "0")
                        prefs.loginResponse = gson.toJson(LoginResponseRummy())
                        prefs.loginCompleted = false
                        navigator.logoutUser()
                    }
                    isLoading.set(false)
                    couponApplied.value = it.Status
                    if (it.Status) {
                        this.couponId = couponId
                        myDialog?.showCouponAppliedDialog(
                            it.Message,it.Response.Message,selectedColor.get()?:""
                        )
                    } else {
                        navigator.showError(it.Message)
                    }
                }, {
                    isLoading.set(false)
                    couponApplied.value = false
                    navigator.handleError(it)
                })
        )
    }

    fun redeemCode(coupon: String) {
        isRedeemCoupon.set(true)
        if (TextUtils.isEmpty(coupon)) {
            navigator.showError("Please Enter Coupon Code")
            return
        } else if (coupon.length < 4) {
            navigator.showError("Please Enter Valid Coupon Code")
            return
        }

        if (!connectionDetector.isConnected) {
            myDialog?.noInternetDialog {
                isLoading.set(true)
                redeemCode(coupon)
            }
            isLoading.set(false)
            return
        }

        isLoading.set(true)

        compositeDisposable.add(
            apis.redeemCode(
                loginResponse.UserId.toString(),
                loginResponse.ExpireToken,
                loginResponse.AuthExpire, coupon, prefs.androidId ?: ""
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(({
                    isLoading.set(false)
                    if (it.TokenExpire) {
                        logoutStatus(apis, loginResponse.UserId, prefs.androidId ?: "", "0")
                        prefs.loginResponse = gson.toJson(LoginResponseRummy())
                        prefs.loginCompleted = false
                        navigator.logoutUser()
                    }
                    if (it.Status) {
                        navigator.showMessage(it.Message)
                        hideSheet()
                    } else {
                        navigator.showError(it.Message)
                    }
                }), ({
                    isLoading.set(false)
                    navigator.handleError(it)
                }))
        )
    }

    fun getPaymentMethods(amount: Double) {
        if (!connectionDetector.isConnected) {
            myDialog?.noInternetDialog {
                isLoading.set(true)
                getPaymentMethods(amount)
            }
            isLoading.set(false)
            return
        }

        val json = JsonObject()
        json.addProperty("Amount",amount)
        json.addProperty("OfferIds",offerIds)
        json.addProperty("PassId",0)
        json.addProperty("CoupanId",couponId)
        json.addProperty(LOCATION_COORDINATE,userCurrentStateLatLog)
        isLoading.set(true)
        val apiInterface = getApiEndPointObject(prefs.appUrl2?:"")

        compositeDisposable.add(
            apiInterface.getPaymentGateWay(
                loginResponse.UserId,
                loginResponse.ExpireToken,
                loginResponse.AuthExpire,
                json
            )
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    if (loginResponse.UserId!= MyConstants.TEST_ACCOUNT_USER_ID && it.IsUserFromBannedState){
                        isLoading.set(false)
                        saveLocationDisableTime(resetTime = true)
                        navigatorAct.showDialog(MyConstants.RESTRICT_LOC_MESSAGE)
                        return@subscribe
                    }
                    if (it.TokenExpire) {
                        logoutStatus(apis, loginResponse.UserId, prefs.androidId ?: "", "0")
                        prefs.loginResponse = gson.toJson(LoginResponseRummy())
                        prefs.loginCompleted = false
                        navigator.logoutUser()
                    }
                    if (it.Status) {
                        isLoading.set(false)
                        //cardListCount.set(it.AddCard.size)
                        saveLocationDisableTime(it.delayMinutes)
                        it.GatewayList.map {model->
                            if (model.Type!=4 || it.IsVPAAllow)
                                setAddMoreGatewayItem(model)
                        }
                        navigatorAct.goForPayment(it, amount)
                        if (it.isUpdate) navigatorAct.onUpdateRequired(it.updateMessage)
                    } else {
                        navigator.showError(it.Message)
                    }
                    isLoading.set(false)
                }, {
                    isLoading.set(false)
                    navigator.showError(R.string.something_went_wrong)
                    it.printStackTrace()
                })
        )
    }

    fun getOfferList(){
        if (!connectionDetector.isConnected) {
            myDialog?.noInternetDialog {
                isLoading.set(true)
                getOfferList()
            }
            isLoading.set(false)
            return
        }
        isLoading.set(true)
        val apiInterface = getApiEndPointObject(prefs.appUrl2?:"")
        compositeDisposable.add(
            apiInterface.getAddCashOfferList(
                loginResponse.UserId,
                loginResponse.ExpireToken,
                loginResponse.AuthExpire,
            )
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    isLoading.set(false)
                    if (it.TokenExpire) {
                        logoutStatus(apis, loginResponse.UserId, prefs.androidId ?: "", "0")
                        prefs.loginResponse = gson.toJson(LoginResponseRummy())
                        prefs.loginCompleted = false
                        navigator.logoutUser()
                    }
                    if (it.Status) {
                        ticketsAvailable.set(
                            it.Response?.Offers?.any {offerModel->
                                offerModel.Tickets.isNotEmpty()
                            }?:false
                        )
                        enableGstCalculation.set(it.Response.IsGST)
                        offerTitle.set(it.Title)
                        offerDescription.set(it.Description)
                        _mAddCashOffer.value = it.Response?.Offers
                        _mAvailableCoupons.value = it.Response?.Coupans
                        _mBannerOffer.value = it.Response.Headers

                        isOfferListEmpty.set(it.Response?.Offers?.isEmpty()==true)
                        isCouponsListEmpty.set(it.Response?.Coupans?.isEmpty()==true)
                    } else {
                        navigator.showError(it.Message)
                    }
                }, {
                    isLoading.set(false)
                    navigator.showError(R.string.something_went_wrong)
                    it.printStackTrace()
                })
        )
    }

    fun getGstData(amount : String,isJoin : Boolean) {
        if (!connectionDetector.isConnected) {
            myDialog?.noInternetDialog {
                getGstData(amount,isJoin)
            }
            return
        }
        isGstCalculating.set(true)
        val json = JsonObject()
        json.addProperty("Amount",amount)
        json.addProperty("isJoin",isJoin)

        compositeDisposable.add(
            apis.getGstCalcualtion(
                loginResponse.UserId.toString(),
                loginResponse.ExpireToken,
                loginResponse.AuthExpire,
                json
            ).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    isGstCalculating.set(false)
                    if (it.Status) {
                        it.Response.gSTBifurcation?.iterator()?.let { iterator ->
                            while (iterator.hasNext()) {
                                iterator.next().let { bonus ->
                                    bonus.note = it.Response.note
                                }
                            }
                        }
                        if((addCashAmmount.get() ?: 0.0) > 0.0){
                            _gstList.value = it.Response.gSTBifurcation
                            gstModel.value = it.Response
                            isGstCardVisible.set(true)
                        }
                    } else {
                        isGstCardVisible.set(false)
                        navigator.showError(it.Message)
                    }
                }, {
                    isGstCalculating.set(false)
                    isGstCardVisible.set(false)
                    navigator.handleError(it)
                })
        )
    }

    fun getHeaders(){
        if (!connectionDetector.isConnected) {
            myDialog?.noInternetDialog {
                isLoading.set(true)
                getHeaders()
            }
            isLoading.set(false)
            return
        }
        isLoading.set(true)
        val api=getApiEndPointObject("https://rummy.myteam11.games")
        compositeDisposable.add(
            api.getHeaders()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    isLoading.set(false)
                    allowAutoScrolling = it.IsAutoScrollHeader
                    if (it.Status) {
                        //_mBannerOffer.value = it.Response
                    } else {
                        //navigator.showError(it.Message)
                    }
                }, {
                    isLoading.set(false)
                    //navigator.showError(R.string.something_went_wrong)
                    it.printStackTrace()
                })
        )
    }

    /*Current Location */
    fun saveCurrentTime(currentState:String="",userLatLog:String="",userCountry:String="") {
        locationModel?.updateData(currentState,userLatLog,userCountry)
        fetchUserStateName(userLatLog)
    }

    fun isLocationRequired():Boolean{
        return if (!locationModel?.latLog.isNullOrEmpty())
            false
        else if (prefs.locationApiTimeLimit <= Date().time) {
            prefs.locationApiTimeLimit = 0L
            prefs.locationApiPendingMinutes = 0
            if (locationModel==null)
                locationModel=
                    UserCurrentLocationModel()
            else
                locationModel?.resetModel()
            return true
        } else {
            return try{
                locationModel = gson.fromJson(prefs.userCurrentLocationRes,
                    UserCurrentLocationModel::class.java)
                false
            }catch (e:Exception) {
                locationModel =
                    UserCurrentLocationModel()
                true
            }
        }
    }

    fun saveLocationDisableTime(minute: Int=5,resetTime:Boolean=false) {
        if (resetTime){
            prefs.locationApiTimeLimit = 0L
            prefs.locationApiPendingMinutes =0
            locationModel?.resetModel()
            prefs.userCurrentLocationRes=""
            return
        }
        if (prefs.locationApiPendingMinutes!=minute){
            val df = SimpleDateFormat("HH:mm")
            val d = Date()
            val cal = Calendar.getInstance()
            cal.time = d
            cal.add(Calendar.MINUTE, minute)
            prefs.locationApiTimeLimit = cal.time.time
            prefs.locationApiPendingMinutes =minute
            println(" ${df.format(cal.time)}")
        }
    }

    /*Check user belongs to Restrict state or not */
    fun fetchUserStateName(userLatLog:String){
        if (!connectionDetector.isConnected) {
            myDialog?.noInternetDialog {
                isLoading.set(true)
                fetchUserStateName(userLatLog)
            }
            isLoading.set(false)
            return
        }
        isLoading.set(true)

        val json = JsonObject()
        json.addProperty(APIInterface.COORDINATES,userLatLog)
        compositeDisposable.add(
            apis.checkUserIsBanned(
                loginResponse.UserId.toString(),
                loginResponse.ExpireToken,
                loginResponse.AuthExpire,
                json)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    isLoading.set(false)
                    if (loginResponse.UserId!= MyConstants.TEST_ACCOUNT_USER_ID && !it.AllowRummyGame){
                        saveLocationDisableTime(resetTime = true)
                        navigatorAct.showDialog(MyConstants.RESTRICT_LOC_MESSAGE)
                        return@subscribe
                    }
                    prefs.userCurrentLocationRes=gson.toJson(locationModel)
                    saveLocationDisableTime(it.DelayMinutes)
                    navigatorAct.onValidLocationFound()
                },{
                    isLoading.set(false)
                    saveCurrentTime("",userLatLog,"")
                }))
    }
}

interface AddCashNavigator {
    fun showDialog(descriptionMsg:String)
    fun onAddressNotVerified()
    fun addAmount(isPlus: Boolean)
    fun collectPlayStoreRequiredData(){}
    fun onValidLocationFound()
    fun selectAllOffer(select:Boolean)
    fun addAmount(ammount: Int)
    fun setAmount(amount: Double?=0.0)
    fun minusAmount(ammount: Int)
    fun onUpdateRequired(message: String)
    fun goForPayment(response: NewPaymentGateWayModel, amount: Double)
}