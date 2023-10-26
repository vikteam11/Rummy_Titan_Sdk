package com.rummytitans.playcashrummyonline.cardgame.ui.home

import androidx.core.os.bundleOf
import com.rummytitans.playcashrummyonline.cardgame.analytics.AnalyticsHelper
import com.rummytitans.playcashrummyonline.cardgame.api.APIInterface
import com.rummytitans.playcashrummyonline.cardgame.data.SharedPreferenceStorageRummy
import com.rummytitans.playcashrummyonline.cardgame.models.*
import com.rummytitans.playcashrummyonline.cardgame.ui.BaseViewModel
import com.rummytitans.playcashrummyonline.cardgame.ui.home.adapter.RummyCategoryNavigator
import com.rummytitans.playcashrummyonline.cardgame.utils.ConnectionDetector
import com.rummytitans.playcashrummyonline.cardgame.utils.MyConstants
import com.rummytitans.playcashrummyonline.cardgame.widget.MyDialog
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.rummytitans.playcashrummyonline.cardgame.analytics.AnalyticsKey
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
@HiltViewModel
class HomeViewModel @Inject constructor(
    val prefs: SharedPreferenceStorageRummy,
    val gson: Gson,
    val apiInterface: APIInterface,
    val connectionDetector: ConnectionDetector, val analyticsHelper: AnalyticsHelper
) : BaseViewModel<RummyCategoryNavigator>(){

    var locationModel: UserCurrentLocationModel?=null
    var loginResponse: LoginResponse = gson.fromJson(prefs.loginResponse, LoginResponse::class.java)
    var isLoading = ObservableBoolean(false)
    var lobbyLoading = ObservableBoolean(false)
    val selectedCategory = ObservableField(RummyCategoryModel())

    var currentLobby: RummyLobbyModel? = null
    var regularColor = prefs.regularColor
    var safeColor = prefs.safeColor
    val selectedColor = ObservableField(if (prefs.onSafePlay) safeColor else regularColor)

    private val _headerList = MutableLiveData<ArrayList<HeaderItemModel>>(ArrayList())
    val headerList: LiveData<ArrayList<HeaderItemModel>> get() = _headerList

    private val _categoryList = MutableLiveData<ArrayList<RummyCategoryModel>>(ArrayList())
    val categoryList: LiveData<ArrayList<RummyCategoryModel>> get() = _categoryList

    private val _lobbies = MutableLiveData<ArrayList<RummyLobbyModel>>(ArrayList())
    val lobbies: LiveData<ArrayList<RummyLobbyModel>> get() = _lobbies

    private val _filterLobbies = MutableLiveData<ArrayList<RummyLobbyModel>>(ArrayList())
    val filterLobbies: LiveData<ArrayList<RummyLobbyModel>> get() = _filterLobbies

    val playerType = ObservableInt(2)
    val selectedVariant = ObservableInt(13)
    val sortDescending = ObservableBoolean(true)
    val lobbyAvailable = ObservableBoolean(true)
    var allowAutoScrolling = true
    var viewScrollingTime:Long = 5
    var isAddressVerified=true
    var needToSortLobby=false

    var myDialog: MyDialog? = null

    fun changeGamePlayer(){
        playerType.set(
            if(playerType.get() == 2)6 else 2
        )
        filterLobby()

        analyticsHelper.fireEvent(
            AnalyticsKey.Names.ButtonClick, bundleOf(
                    AnalyticsKey.Keys.ButtonName to "${playerType.get()} Player" ,
                AnalyticsKey.Keys.Screen to AnalyticsKey.Values.Home
            )
        )
    }

    fun getBanner() {
        if (!connectionDetector.isConnected) {
            return
        }
        val apis = getApiEndPointObject(MyConstants.GAME_PLAY_URL)
        compositeDisposable.add(
            apis.getBanner(
                loginResponse.UserId,
                loginResponse.ExpireToken,
                loginResponse.AuthExpire,
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
                    //viewScrollingTime = it.ScrollTime?:5
                    allowAutoScrolling = it.IsAutoScrollHeader
                    if (it.Status) {
                        _headerList.value = it.Response
                    }
                }), ({
                    isLoading.set(false)
                    navigator.handleError(it)
                }))
        )
    }

    fun getCategories() {
        if (!connectionDetector.isConnected) {
            myDialog?.noInternetDialog {
                getBanner()
                getCategories()
            }
            return
        }
        isLoading.set(true)
        val apis = getApiEndPointObject(prefs.gamePlayUrl ?:"")
        compositeDisposable.add(
            apis.getCategories(
                loginResponse.UserId,
                loginResponse.ExpireToken,
                loginResponse.AuthExpire,
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
                        if(it.Response.isNotEmpty()){
                            val category =  it.Response.singleOrNull {cat-> cat.Selected }?:it.Response[0]
                            category.Selected =true
                            selectedCategory.set(category)
                            getCategoryLobbies()
                        }
                        _categoryList.value = it.Response
                    } else {
                        navigator.showError(it.Message)
                    }
                }), ({
                    isLoading.set(false)
                    navigator.handleError(it)
                }))
        )
    }

    fun refreshData(){
        getBanner()
        getCategoryLobbies()
        navigatorAct.onSwipeWallet()
    }

    fun getCategoryLobbies() {
        if(selectedCategory.get()?.CategoryId.isNullOrEmpty()){
            return
        }
        if (!connectionDetector.isConnected) {
            myDialog?.noInternetDialog {
                getCategoryLobbies()

            }
            return
        }
        lobbyLoading.set(true)
        val apis = getApiEndPointObject(prefs.gamePlayUrl ?:"")
        compositeDisposable.add(
            apis.getLobbies(
                loginResponse.UserId,
                loginResponse.ExpireToken,
                loginResponse.AuthExpire,
                selectedCategory.get()?.CategoryId?:"",
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(({
                    lobbyLoading.set(false)
                    if (it.TokenExpire) {
                        logoutStatus(apiInterface, loginResponse.UserId, prefs.androidId ?: "", "0")
                        prefs.loginResponse = gson.toJson(LoginResponse())
                        prefs.loginCompleted = false
                        navigator.logoutUser()
                    }

                    if (it.Status) {
                        it.Response.map {
                            it.CatName = selectedCategory.get()?.Name?:""
                            it.Type = selectedCategory.get()?.Type?:""
                        }
                        _lobbies.value = it.Response
                        filterLobby()
                    } else {
                        lobbyAvailable.set(false)
                        navigator.showError(it.Message)
                    }
                }), ({
                    lobbyLoading.set(false)
                    lobbyAvailable.set(false)
                    navigator.showError(it.message)
                }))
        )
    }

    fun filterLobby() {
        val sortList = _lobbies.value?.filter { it.MaxPlayer == playerType.get() }
            ?.filter { it.CardVariant == selectedVariant.get() }?.toMutableList()

        if(sortList?.isNotEmpty() == true){
             if(needToSortLobby){
                 if(sortDescending.get()){
                     sortList.sortBy { it.MaxWin }
                 }else{
                     sortList.sortByDescending { it.MaxWin }
                 }
             }
            _filterLobbies.value = sortList as ArrayList
            lobbyAvailable.set(true)
        }else{
            _filterLobbies.value = arrayListOf()
            lobbyAvailable.set(false)
        }
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
            apiInterface.checkUserIsBanned(
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

    fun cleraLobby() {
        _filterLobbies.value = arrayListOf()
        _lobbies.value = arrayListOf()
       // lobbyAvailable.set(false)
    }
}