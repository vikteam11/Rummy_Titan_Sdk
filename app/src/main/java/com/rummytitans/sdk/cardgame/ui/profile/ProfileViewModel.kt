package com.rummytitans.sdk.cardgame.ui.profile

import com.rummytitans.sdk.cardgame.R
import com.rummytitans.sdk.cardgame.analytics.AnalyticsHelper
import com.rummytitans.sdk.cardgame.analytics.AnalyticsKey
import com.rummytitans.sdk.cardgame.api.APIInterface
import com.rummytitans.sdk.cardgame.data.SharedPreferenceStorageRummy
import com.rummytitans.sdk.cardgame.models.AvatarModel
import com.rummytitans.sdk.cardgame.models.LoginResponse
import com.rummytitans.sdk.cardgame.models.WalletInfoModel
import com.rummytitans.sdk.cardgame.ui.BaseViewModel
import com.rummytitans.sdk.cardgame.utils.ConnectionDetector
import com.rummytitans.sdk.cardgame.utils.alertDialog.AlertdialogModel
import com.rummytitans.sdk.cardgame.widget.MyDialog
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.gson.Gson
import com.rummytitans.sdk.cardgame.models.SportTabs
import com.rummytitans.sdk.cardgame.ui.base.BaseNavigator
import com.rummytitans.sdk.cardgame.ui.profile.avtaar.ProfileAvtarModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    val prefs: SharedPreferenceStorageRummy,
    val gson: Gson,
    val apiInterface: APIInterface,
    val connectionDetector: ConnectionDetector, val analyticsHelper: AnalyticsHelper
) : BaseViewModel<BaseNavigator>() {
    val loginResponse: LoginResponse = gson.fromJson(prefs.loginResponse, LoginResponse::class.java)

    var avatars = ArrayList<ProfileAvtarModel>()
    var avatarList = ObservableField<ArrayList<AvatarModel>>(arrayListOf())
    var selectedAvatar = ObservableInt(1)
    var myDialog: MyDialog? = null

    var profileData = MutableLiveData<WalletInfoModel>()

    val getData: LiveData<WalletInfoModel> get() = profileData

    val safeSelected = MutableLiveData(false)

    val regularColor = prefs.regularColor
    val safeColor = prefs.safeColor
    val selectedColor = ObservableField(if (prefs.onSafePlay) safeColor else regularColor)



    val sportResponse = gson.fromJson(prefs.sportResponse, Array<SportTabs>::class.java)
    val isAvtarBottomSheetVisible = ObservableBoolean(false)


    init {
        safeSelected.value = prefs.onSafePlay
    }

    val isBottomSheetVisible = ObservableBoolean(false)

    var isRankListAvailable = false

    val _bottomSheetStateEvent = MutableLiveData<Int>(BottomSheetBehavior.STATE_HIDDEN)
    val bottomSheetStateEvent: LiveData<Int>
        get() = _bottomSheetStateEvent

    val alertDialogModel = ObservableField<AlertdialogModel>()

    fun logoutFromOneDevice(){
        navigator.let {
            alertDialogModel.set(
                AlertdialogModel(it.getStringResource(R.string.app_name),
                    it.getStringResource(R.string.log_out_device_msg),
                    it.getStringResource(R.string.no),
                    it.getStringResource(R.string.yes),
                    { toggleBottomSheet() },
                    {
                        toggleBottomSheet()
                        logoutUser()
                    })
            )
        }
        toggleBottomSheet()
    }

    fun logoutFromAllDevice(){
        navigator.let {
            alertDialogModel.set(
                AlertdialogModel(it.getStringResource(R.string.app_name),
                    it.getStringResource(R.string.logout_all_device),
                    it.getStringResource(R.string.no),
                    it.getStringResource(R.string.yes),
                    { toggleBottomSheet() },
                    {
                        toggleBottomSheet()
                        logoutUserFromAllDevice()
                    })
            )
        }
        toggleBottomSheet()
    }

    fun toggleBottomSheet() {
        _bottomSheetStateEvent.value =
            if (bottomSheetStateEvent.value == BottomSheetBehavior.STATE_EXPANDED) {
                BottomSheetBehavior.STATE_HIDDEN
            } else {

                BottomSheetBehavior.STATE_EXPANDED
            }
    }

    fun toggelBottomSheet() {
        isBottomSheetVisible.set(!isBottomSheetVisible.get())
    }

    fun showSheet(type: Int) {
        if (type == 0) {
            if (isRankListAvailable)
                isBottomSheetVisible.set(!isBottomSheetVisible.get())
            else
                navigator.showError("Rank Not Available")
        } else if (type == 2)
            isAvtarBottomSheetVisible.set(!isAvtarBottomSheetVisible.get())
    }

    fun hideAllSheet() {
        isBottomSheetVisible.set(false)
        isAvtarBottomSheetVisible.set(false)
    }

    fun logoutUser() {
        analyticsHelper.fireEvent(AnalyticsKey.Names.LogOut)
        prefs.referCode = ""
        logout()
        navigator.logoutUser()
        prefs.userName = ""
        prefs.userTeamName = ""
        prefs.avtarId = -1
        prefs.sportSelected = 1
    }

    fun logoutUserFromAllDevice(){
        analyticsHelper.fireEvent(AnalyticsKey.Names.LogOutFromAllDevices)
        requestLogoutFromAllDevice(
            apiInterface,loginResponse.UserId,
            loginResponse.ExpireToken.toString(),
            loginResponse.AuthExpire.toString()){
            logoutUser()
        }
        navigator.logoutUser()
        prefs.userName = ""
        prefs.userTeamName = ""
        prefs.avtarId = -1
        prefs.sportSelected=1
    }

    fun updateAvtar(avtaarId: Int) {

        if (!connectionDetector.isConnected) {
            myDialog?.noInternetDialog {
                updateAvtar(avtaarId)
            }
            return
        }

        compositeDisposable.add(
            apiInterface.changeUserAvtaar(
                loginResponse.UserId.toString(),
                loginResponse.ExpireToken,
                loginResponse.AuthExpire,
                avtaarId.toString()
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(({
                    isAvtarBottomSheetVisible.set(false)
                    if (it.Status) {
                        prefs.avtarId=avtaarId
                        navigator.showMessage(it.Message)
                    } else {
                        navigator.showError(it.Message)
                    }
                }), ({
                    isAvtarBottomSheetVisible.set(false)
                    navigator.handleError(it)
                }))
        )
    }

    fun logout() {
        prefs.introductionCompleted=false
        prefs.loginCompleted = false
        prefs.loginResponse = gson.toJson(LoginResponse())
        logoutStatus(apiInterface, loginResponse.UserId, prefs.androidId ?: "", "0")
    }

}
