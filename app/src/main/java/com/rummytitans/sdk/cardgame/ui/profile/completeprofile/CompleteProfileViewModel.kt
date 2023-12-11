package com.rummytitans.sdk.cardgame.ui.completeprofile

import com.rummytitans.sdk.cardgame.R
import com.rummytitans.sdk.cardgame.analytics.AnalyticsHelper
import com.rummytitans.sdk.cardgame.api.APIInterface
import com.rummytitans.sdk.cardgame.data.SharedPreferenceStorageRummy
import com.rummytitans.sdk.cardgame.models.AvatarModel
import com.rummytitans.sdk.cardgame.models.LoginResponse
import com.rummytitans.sdk.cardgame.ui.BaseViewModel
import com.rummytitans.sdk.cardgame.utils.ConnectionDetector
import com.rummytitans.sdk.cardgame.widget.MyDialog
import android.text.TextUtils
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
@HiltViewModel
class CompleteProfileViewModel @Inject constructor(
    val prefs: SharedPreferenceStorageRummy,
    val connectionDetector: ConnectionDetector,
    val apiInterface: APIInterface,
    val gson: Gson, val analyticsHelper: AnalyticsHelper
) : BaseViewModel<CompleteProfileNavigator>() {

    var myDialog: MyDialog? = null
    var createTeamFromLogin = false

    lateinit var loginResponse: LoginResponse
    var checkTeamObservable: Disposable? = null
    var txtTeamName = ObservableField<String>("")
    var txtTeamNameColor = ObservableInt(R.color.rummy_colorPrimary)
    var flagTeamName = ObservableBoolean(false)
    var flagAvatar = ObservableBoolean(false)
    var isLoading = ObservableBoolean(false)
    var flagFullName = ObservableBoolean(false)
    var isSuggestionAvailable = ObservableBoolean(false)
    var flagState = ObservableBoolean(false)
    var selectedState = ""
    var txtTeamNameIcon = ObservableInt(R.drawable.ic_check_incorrect)
    var state = ArrayList<String>()
    var avatars = ArrayList<AvatarModel>()
    var stateList = ObservableField<MutableList<String>>(state)
    var avatarList = ObservableField<ArrayList<AvatarModel>>(avatars)
    var selectedAvatar = ObservableInt(-1)
    var isSuggestionsUse = false

    var selectedTeamNameFromSuggestion = ""

    fun onFullnameChanged(s: CharSequence, i: Int, j: Int, k: Int) {
        flagFullName.set(s.isNotEmpty())
    }

    fun onTeamnameChanged(s: CharSequence, i: Int, j: Int, k: Int) {
        teamNameAvailable(false)
    }

    fun checkTeamName(s: CharSequence) {
        if (s.length < 3) {
            teamNameAvailable(false)
            return
        }
        checkTeamObservable?.dispose()
        checkTeamObservable = apiInterface.checkTeamnameAvailability(
            loginResponse.UserId,
            loginResponse.ExpireToken,
            loginResponse.AuthExpire,
            s.toString()
        ).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (it.Status) {
                    teamNameAvailable(it.Response.toString().toInt() == 0)
                    if (it.Response.toString().toInt() == 0) {
                        isSuggestionAvailable.set(false)

                    }
                }

            }, {
                navigator.handleError(it)
            })
    }

    val teamNameSuggestions = MutableLiveData<String>()

    fun teamNameSuggestions() {

        checkTeamObservable?.dispose()
        checkTeamObservable = apiInterface.checkTeamnameSuggestions(
            loginResponse.UserId,
            loginResponse.ExpireToken,
            loginResponse.AuthExpire
        ).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                // navigator.showMessage(it.Message)
                if (it.Status)
                    teamNameSuggestions.value = it.Response
                isSuggestionAvailable.set(!TextUtils.isEmpty(it.Response))

            }, {
                navigator.handleError(it)
            })
    }

    private fun teamNameAvailable(b: Boolean) {
        flagTeamName.set(b)
        txtTeamName.set(
            navigator.getStringResource(R.string.txt_team_name) + " " + if (b) navigator.getStringResource(R.string.txt_available) else navigator.getStringResource(
                R.string.txt_not_available
            )
        )
        txtTeamNameColor.set(if (b) R.color.rummy_colorPrimary else R.color.rummy_colorAccent)
        txtTeamNameIcon.set(if (b) R.drawable.ic_check_correct else R.drawable.ic_check_incorrect)
    }


    fun getState() {
        if (!connectionDetector.isConnected) {
            myDialog?.noInternetDialog {
                getState()
            }
            return
        }
        compositeDisposable.add(
            apiInterface.getStateList().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({

                    if (it.Status) {
                        val states = ArrayList<String>()
                        states.add(navigator.getStringResource(R.string.err_select_your_state))
                        for (item in it.Response) states.add(item.StateName)
                        stateList.set(states as MutableList<String>)
                    }
                }, {
                    navigator.handleError(it)
                })
        )
    }

    fun saveProfile() {
        when {
            !flagFullName.get() -> navigator.showError("Please enter your full name to continue")
            !flagAvatar.get() -> navigator.showError("Please select your Avatar Image to continue")
            !flagState.get() -> navigator.showError("Please enter your State to continue")
            !flagTeamName.get() -> navigator.showError("Please enter your valid team name to continue")
            else -> navigatorAct.saveProfile()
        }
    }

    fun saveProfile(teamName: String, fullName: String) {

        if (!connectionDetector.isConnected) {
            myDialog?.noInternetDialog {
                saveProfile(teamName, fullName)
            }
            return
        }

        isLoading.set(true)
        compositeDisposable.add(
            apiInterface.updateCompleteProfile(
                loginResponse.UserId,
                loginResponse.ExpireToken,
                loginResponse.AuthExpire,
                fullName,
                teamName,
                selectedState,
                selectedAvatar.get()
            ).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    isLoading.set(false)
                    navigator.showMessage(it.Message)
                    if (it.Status) {
                        //    val loginResponse: LoginResponse = gson.fromJson(prefs.loginResponse, LoginResponse::class.java)
                        loginResponse.Name=fullName
                        prefs.loginResponse = gson.toJson(loginResponse)

                        logoutStatus(apiInterface, loginResponse.UserId, prefs.androidId ?: "", "1")
                        loginResponse.IsFirstTime = false
                        prefs.loginResponse = gson.toJson(loginResponse)
                        prefs.loginCompleted = true
                        navigatorAct.completeProfileSuccess()
                    }
                }, {
                    navigator.handleError(it)
                    isLoading.set(false)
                })
        )

    }
}