package com.rummytitans.sdk.cardgame.ui.settings

import com.rummytitans.sdk.cardgame.analytics.AnalyticsHelper
import com.rummytitans.sdk.cardgame.api.APIInterface
import com.rummytitans.sdk.cardgame.data.SharedPreferenceStorageRummy
import com.rummytitans.sdk.cardgame.models.LoginResponse
import com.rummytitans.sdk.cardgame.models.SubscriptionItemModel
import com.rummytitans.sdk.cardgame.models.VersionModel
import com.rummytitans.sdk.cardgame.ui.BaseViewModel
import com.rummytitans.sdk.cardgame.utils.ConnectionDetector
import com.rummytitans.sdk.cardgame.widget.MyDialog
import android.os.Build
import android.text.TextUtils
import android.widget.CompoundButton
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
//import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.rummytitans.sdk.cardgame.ui.base.BaseNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
@HiltViewModel
class SettingsViewModel @Inject constructor(
    val prefs: SharedPreferenceStorageRummy,
    val gson: Gson,
    val apiInterface: APIInterface,
    val connectionDetector: ConnectionDetector, val analyticsHelper: AnalyticsHelper
) : BaseViewModel<BaseNavigator>() {

    data class ThemeCodeModel(var safeColor: String, var regularcode: String, val themePos: Int)

    val isLanguageOpen = ObservableBoolean(false)
    val isQuizOpen = ObservableBoolean(false)
    val isThemeOpen = ObservableBoolean(false)
    val isTimeTypeOpen = ObservableBoolean(false)
    val timeTypeText = ObservableField<String>("")
    var mSelectedThemeCode = ObservableField<String>("")
    val isNotificaitonOpen = ObservableBoolean()
    val isLoading = ObservableBoolean(false)
    val isUserSubcribe = ObservableBoolean(false)
    val isHindiLanguage = ObservableBoolean(false)
    val quizReminder = ObservableBoolean(prefs.isQuizReminder)
    val quizLanguage = ObservableBoolean(prefs.quizLanguage == "hi")
    val quizSound = ObservableBoolean(prefs.isQuizSoundOn)
    val mSelectedTopics = MutableLiveData<HashSet<String>>()
    val mThemeCode = MutableLiveData<ThemeCodeModel>()
    var selectedcolor = prefs.safeColor
    var myDialog: MyDialog? = null
    var fragmentNavigator: SettingFragmentNavigator? = null

    var isLanguageChange = false
    var isThemeUpdated = false

    init {
        selectedcolor = if (prefs.onSafePlay) prefs.safeColor else prefs.regularColor
        mSelectedThemeCode.set(selectedcolor)
    }

    val subscriptionListModel = MutableLiveData<ArrayList<SubscriptionItemModel>>()


    var loginModel: LoginResponse = gson.fromJson(prefs.loginResponse, LoginResponse::class.java)
    var splashModel: VersionModel = gson.fromJson(prefs.splashResponse, VersionModel::class.java)


    fun toggleQuiz() {
        isQuizOpen.set(!isQuizOpen.get())
    }

    fun toggleLanguage() {
        isLanguageOpen.set(!isLanguageOpen.get())
    }

    fun toggleTheme() {
        isThemeOpen.set(!isThemeOpen.get())
    }

    fun toggleMatchTime() {
        isTimeTypeOpen.set(!isTimeTypeOpen.get())
    }

    fun toggleNotificaiton() {
        if (!connectionDetector.isConnected) {
            myDialog?.noInternetDialog { toggleNotificaiton() }
        } else {
            val isAllowed = fragmentNavigator?.isNotificationsAllow() ?: false
            if (isAllowed) {
                isNotificaitonOpen.set(!isNotificaitonOpen.get())
            }
        }
    }

    fun onQuizLanguageCheckedChange(button: CompoundButton?, check: Boolean) {
        prefs.quizLanguage = if (check) "hi" else "en"
        quizLanguage.set(check)
    }

    fun onQuizReminderCheckedChange(button: CompoundButton?, check: Boolean) {
        prefs.isQuizReminder = check
        quizReminder.set(check)
    }

    fun onQuizSoundCheckedChange(button: CompoundButton?, check: Boolean) {
        prefs.isQuizSoundOn = check
        quizSound.set(check)
    }

    fun hideNotifications() {
        isNotificaitonOpen.set(false)
    }

    fun checkInternetConnection(state: Boolean, from: Int) {
        if (!connectionDetector.isConnected) {
            myDialog?.noInternetDialogWithCancelListener({
                checkInternetConnection(state, from)
            }, {
                fragmentNavigator?.onCancelInternetDialog(1)
            })
        } else {
            fragmentNavigator?.onInternetConncted(state)
        }
    }


    fun getSubscriptionsList() {
        if (!connectionDetector.isConnected) {
            myDialog?.noInternetDialog {
                isLoading.set(true)
                getSubscriptionsList()
            }
            return
        }
        compositeDisposable.add(
            apiInterface.getSubscriptionList(
                    loginModel.UserId,
                    loginModel.ExpireToken,
                    loginModel.AuthExpire,
                    false
                )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (it.Status)
                        subscriptionListModel.value = it.Response
                    else
                        navigator.showError(it.Message)
                }, {
                    it.printStackTrace()
                })
        )
    }

    fun fetchTopicsAndSubscribe() {
        if (!connectionDetector.isConnected) {
            myDialog?.noInternetDialog {
                isLoading.set(true)
                fetchTopicsAndSubscribe()
            }
            return
        }
        compositeDisposable.add(
            apiInterface.getSubscriptionList(
                    loginModel.UserId,
                    loginModel.ExpireToken,
                    loginModel.AuthExpire,
                    true
                )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (it.Status) {
                        it.Response.forEach { subscriptionListModel ->
                            if (subscriptionListModel.Status) {
                               /* FirebaseMessaging.getInstance()
                                    .subscribeToTopic(subscriptionListModel.mTopicName)*/
                            }
                        }
                    } else navigator.showError(it.Message)
                }, {
                    navigator.showError(it.message)
                })
        )
    }

    fun saveSubscriptionsList() {
        if (!connectionDetector.isConnected) {
            myDialog?.noInternetDialog {
                isLoading.set(true)
                saveSubscriptionsList()
            }
            return
        }

        var topicsList = ""

        mSelectedTopics.value?.forEach {
           /* FirebaseMessaging.getInstance().subscribeToTopic(it)
            topicsList += "$it,"*/
        }

        if (!TextUtils.isEmpty(topicsList) && topicsList.contains(","))
            topicsList = topicsList.substring(0, topicsList.length - 1)

        compositeDisposable.add(
            apiInterface.saveSubscriptionList(
                    loginModel.UserId,
                    loginModel.ExpireToken,
                    loginModel.AuthExpire,
                    topicsList
                )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (!it.status)
                        navigator.showError(it.message)
                }, {
                    navigator.showError(it.message)
                })
        )
    }

    fun saveLanguage(languageCode: String) {
        isLoading.set(true)
        if (!connectionDetector.isConnected) {
            myDialog?.noInternetDialog {
                isLoading.set(true)
                saveLanguage(languageCode)
            }
            return
        }
        compositeDisposable.add(
            apiInterface.saveLanguage(
                    loginModel.UserId, prefs.androidId ?: "", languageCode
                    , Build.MODEL
                )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(({
                    fragmentNavigator?.onLanguageChange(it.Message)
                }), ({
                    navigator.showError(it.message)
                }))
        )
    }

}

