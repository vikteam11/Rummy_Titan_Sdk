package com.rummytitans.sdk.cardgame.ui.profile.info.viewmodel

import com.rummytitans.sdk.cardgame.R
import com.rummytitans.sdk.cardgame.analytics.AnalyticsHelper
import com.rummytitans.sdk.cardgame.analytics.AnalyticsKey
import com.rummytitans.sdk.cardgame.api.APIInterface
import com.rummytitans.sdk.cardgame.data.SharedPreferenceStorageRummy
import com.rummytitans.sdk.cardgame.models.LevelModel
import com.rummytitans.sdk.cardgame.models.LoginResponse
import com.rummytitans.sdk.cardgame.models.ProfileInfoModel
import com.rummytitans.sdk.cardgame.ui.BaseViewModel
import com.rummytitans.sdk.cardgame.ui.profile.info.DBUpdateNavigortor
import com.rummytitans.sdk.cardgame.utils.ConnectionDetector
import com.rummytitans.sdk.cardgame.utils.locationservices.utils.emptyJson
import com.rummytitans.sdk.cardgame.widget.MyDialog
import android.text.TextUtils
import androidx.core.os.bundleOf
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.rummytitans.sdk.cardgame.RummyTitanSDK
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
@HiltViewModel
class ProfileInfoViewModel
@Inject constructor(
    val prefs: SharedPreferenceStorageRummy,
    val gson: Gson,
    val apiInterface: APIInterface,
    val connectionDetector: ConnectionDetector, var analyticsHelper: AnalyticsHelper
) : BaseViewModel<DBUpdateNavigortor>() {
    var isLoading = ObservableBoolean(false)
    var myDialog: MyDialog? = null
    var isSwipeLoading = ObservableBoolean(false)

    var updateData = ObservableBoolean(false)
    var isEditable = MutableLiveData(true)
    val loginResponse: LoginResponse = gson.fromJson(prefs.loginResponse, LoginResponse::class.java)

    val data = MutableLiveData<ProfileInfoModel>()
    val profileInfo: LiveData<ProfileInfoModel> get() = data


    val levelData = MutableLiveData<LevelModel>()
    val levelInfo: LiveData<LevelModel> get() = levelData

    var state = ArrayList<String>()
    var stateList = ObservableField(state)
    var selectedState = ""


    var selectedStatePosition = MutableLiveData(0)

    val loginType: String = prefs.loginType ?: "EMAIL"


    val regularColor = prefs.regularColor
    val safeColor = prefs.safeColor
    val selectedColor = ObservableField(if (prefs.onSafePlay) safeColor else regularColor)

    var isSuggestionAvailable = ObservableBoolean(false)
    var isValidTeamName = ObservableBoolean(false)
    var isTeamAvailable = MutableLiveData<Boolean>()
    var teamName = ObservableField<String>("")
    val teamNameSuggestions = MutableLiveData<String>()
    var isDOBEditable = ObservableBoolean(true)
    var isAddressEditable = ObservableBoolean(true)
    var isStateEditable = ObservableBoolean(true)
    var isEmailEditable = ObservableBoolean(true)

    fun checkTeamName(s: CharSequence) {
        teamName.set(s.toString())
        if (s.length < 4)
            return
        compositeDisposable.add(
            apiInterface.checkTeamnameAvailability(
                loginResponse.UserId,
                loginResponse.ExpireToken,
                loginResponse.AuthExpire,
                s.toString()
            ).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (it.Status) {
                        val isOkTeamName = it.Response.toString().toInt() == 0
                        isValidTeamName.set(isOkTeamName)
                        isTeamAvailable.value = isOkTeamName
                    }
                }, {
                    navigator.handleError(it)
                })
        )
    }

    fun saveTeamName() {

        analyticsHelper.fireEvent(
            AnalyticsKey.Names.ButtonClick, bundleOf(
                AnalyticsKey.Keys.ButtonName to AnalyticsKey.Values.TeamNameUpdateRequested,
                AnalyticsKey.Keys.Screen to AnalyticsKey.Screens.Profile
            )
        )

        if (!connectionDetector.isConnected) {
            myDialog?.noInternetDialog {
                isLoading.set(true)
                saveTeamName()
            }
            isLoading.set(false)
            return
        }
        val newTeamName =  teamName.get().toString()
        val json = JsonObject()
        json.addProperty(APIInterface.TEAMNAME,newTeamName)
        json.addProperty(APIInterface.DEVICE_ID_2,prefs.androidId.toString())

        compositeDisposable.add(
            apiInterface.updateProfileData(
                loginResponse.UserId.toString(),
                loginResponse.ExpireToken,
                loginResponse.AuthExpire,
                json
       ).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (it.Status) {
                        navigatorAct.onSuccesTeamUpdate(it.Message)
                    }
                    isLoading.set(false)
                    if (it.TokenExpire) {
                        logoutStatus(apiInterface, loginResponse.UserId, prefs.androidId ?: "", "0")
                        prefs.loginResponse = gson.toJson(LoginResponse())
                        prefs.loginCompleted = false
                        navigator.logoutUser()
                    }
                }, {
                    isLoading.set(false)
                    navigator.handleError(it)
                })
        )
    }

    fun getState() {
        if (!connectionDetector.isConnected) {
            myDialog?.noInternetDialog {
                isLoading.set(true)
                getState()
            }
            isLoading.set(false)
            isSwipeLoading.set(false)
            return
        }
        compositeDisposable.add(
            apiInterface.getStateList().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (it.Status) {
                        val states = ArrayList<String>()
                        states.add(navigator.getStringResource(R.string.please_select_your_state))
                        for (item in it.Response) states.add(item.StateName)
                        stateList.set(states)
                        fetchProfileData()
                    }
                }, {
                    navigator.handleError(it)
                })

        )
    }

    fun fetchProfileData() {

        if (!connectionDetector.isConnected) {
            myDialog?.noInternetDialog {
                isLoading.set(true)
                fetchProfileData()
            }
            isLoading.set(false)
            isSwipeLoading.set(false)
            return
        }

        compositeDisposable.add(
            apiInterface.getProfileIno(
                loginResponse.UserId.toString(),
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
                        getStatePosition(it.Response.StateName)
                        isDOBEditable.set(TextUtils.isEmpty(it.Response.DOB))
                        isAddressEditable.set(TextUtils.isEmpty(it.Response.Address))
                        isStateEditable.set(TextUtils.isEmpty(it.Response.StateName))
                        isEmailEditable.set(TextUtils.isEmpty(it.Response.Email))
                        analyticsHelper.setUserProperty(AnalyticsKey.Properties.TeamName,it.Response.TeamNmae)
                        analyticsHelper.setJsonUserProperty(emptyJson().apply {
                            put(AnalyticsKey.Properties.Mobile, it.Response?.Mobile)
                            put(AnalyticsKey.Properties.Email, it.Response?.Email)
                            put(AnalyticsKey.Properties.UserID, it.Response?.UserId)
                            put(AnalyticsKey.Properties.FullName, it.Response?.Name)
                            put(AnalyticsKey.Properties.Gender, it.Response?.Gender)
                            put(AnalyticsKey.Properties.DOB, it.Response?.DOB)
                            put(AnalyticsKey.Properties.State, it.Response?.StateName)
                            it.Response?.Name?.split(" ")?.let {list->
                                if (list.isEmpty()) return@let
                                list.elementAtOrNull(0)?.let {fName->
                                    put(AnalyticsKey.Properties.FirstName, fName)
                                }
                                list.elementAtOrNull(1)?.let {lName->
                                    put(AnalyticsKey.Properties.LastName, lName)
                                }
                            }
                        })
                    }
                    navigator.showMessage(it.Message)
                }), ({
                    isLoading.set(false)
                    isSwipeLoading.set(false)
                    navigator.handleError(it)
                }))
        )
    }


    var progress = ObservableField(0)

    fun fetchLevelData() {

        if (!connectionDetector.isConnected) {
            myDialog?.noInternetDialog {
                isLoading.set(true)
                fetchLevelData()
            }
            isLoading.set(false)
            isSwipeLoading.set(false)
            return
        }

        compositeDisposable.add(
            apiInterface.getLevels(
                loginResponse.UserId.toString(),
                loginResponse.ExpireToken,
                loginResponse.AuthExpire
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(({
                    isLoading.set(false)
                    isSwipeLoading.set(false)
                    if (it.Status) {
                        it.Response?.let {
                            levelData.value = it

                            if (it.taskLists.isNotEmpty()) {
                                val completetask = it.taskLists.filter { it.IsCompleted }
                                progress.set((completetask.size * 100) / it.taskLists.size)
                            }

                            analyticsHelper.setUserProperty(
                                AnalyticsKey.Properties.Level, it.CurrentLevels.toString()
                            )
                        }
                    }
                    navigator.showMessage(it.Message)
                }), ({
                    isLoading.set(false)
                    isSwipeLoading.set(false)
                    navigator.handleError(it)
                }))
        )
    }

    fun getStatePosition(s: String) {
        for (i in stateList.get()!!.indices) {
            if (stateList.get()?.get(i).equals(s,true)) {
                selectedStatePosition.value = i
            }
        }
    }

    fun goToEditable() {
        isEditable.value = true
    }

    fun goToNonEditable() {
        isEditable.value = false
    }

    fun updateProfile(name: String, dob: String, gender: String, address: String, pincode: String,email:String,phone:String) {

        if (!connectionDetector.isConnected) {
            myDialog?.noInternetDialog {
                updateProfile(name, dob, gender, address, pincode,email,phone)
            }
            isLoading.set(false)
            isSwipeLoading.set(false)
            return
        }

        if (!validation(gender)) return

        val json = JsonObject()

        json.addProperty("Name",name)
        json.addProperty("DOB",dob)
        json.addProperty("Gender",gender)
        json.addProperty("Address",address)
        json.addProperty(APIInterface.EMAIL,email)
        json.addProperty("State",selectedState)

        isLoading.set(true)
        compositeDisposable.add(
            apiInterface.updateProfileData(
                loginResponse.UserId.toString(),
                loginResponse.ExpireToken,
                loginResponse.AuthExpire,
                json
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(({
                    isLoading.set(false)
                    isStateEditable.set(false)
                    isDOBEditable.set(false)
                    isAddressEditable.set(false)
                    isEmailEditable.set(false)

                    if (it.TokenExpire) {
                        logoutStatus(apiInterface, loginResponse.UserId, prefs.androidId ?: "", "0")
                        prefs.loginResponse = gson.toJson(LoginResponse())
                        prefs.loginCompleted = false
                        navigator.logoutUser()
                    }

                    if (it.Status) {
                        navigator.showMessage(it.Message)
                        val loginResponse: LoginResponse =
                            gson.fromJson(prefs.loginResponse, LoginResponse::class.java)
                        loginResponse.Name = name
                        prefs.loginResponse = gson.toJson(loginResponse)

                        analyticsHelper.fireEvent(
                            AnalyticsKey.Names.ButtonClick, bundleOf(
                                AnalyticsKey.Keys.ButtonName to AnalyticsKey.Values.ProfileUpdateNowDone,
                                AnalyticsKey.Keys.Screen to AnalyticsKey.Screens.EditProfile
                            )
                        )

                        val gender = when (gender) {
                            "M" -> "Male"
                            "F" -> "Female"
                            else -> "Other"
                        }

                        RummyTitanSDK.analytiCallback?.setJsonUserPropertySDK(emptyJson().apply {
                            put(AnalyticsKey.Properties.FullName, name)
                            put(AnalyticsKey.Properties.Gender, gender)
                            put(AnalyticsKey.Properties.Mobile, phone)
                            put(AnalyticsKey.Properties.DOB, modifyDobForSmartech(dob))
                            put(AnalyticsKey.Properties.State, selectedState)
                            name.split(" ").let {list->
                                if (list.isEmpty()) return@let
                                list.elementAtOrNull(0)?.let {fName->
                                    put(AnalyticsKey.Properties.FirstName, fName)
                                }
                                list.elementAtOrNull(1)?.let {lName->
                                    put(AnalyticsKey.Properties.LastName, lName)
                                }
                            }
                        },true)
                        navigatorAct.updateProfileDataSuccess(it.Message)
                    } else navigator.showError(it.Message)

                }), ({
                    isLoading.set(false)
                    navigator.handleError(it)
                }))
        )
    }

    private fun validation(gender: String): Boolean {
        when {
            TextUtils.isEmpty(gender) -> {
                navigator.showError(R.string.select_your_gender)
                return false
            }
            else -> return true
        }

    }

    fun modifyDobForSmartech(date: String): String {
        var newdate = ""
        if (date.contains("/")) {
            val d = date.split("/")
            d.reversed().let {
                it.forEach {

                    if (it.length == 1) {
                        newdate = if (newdate.isEmpty()) "0$it" else "$newdate-0$it"
                    } else {
                        newdate = if (newdate.isEmpty()) it else "$newdate-$it"
                    }
                }
            }
        }
//           newdate=date.replace("/","-")
        return newdate
    }


}
